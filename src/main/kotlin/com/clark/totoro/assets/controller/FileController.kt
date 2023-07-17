package com.clark.totoro.assets.controller

import com.clark.totoro.assets.config.RbacConfig
import com.clark.totoro.assets.config.S3Config
import com.clark.totoro.assets.model.*
import com.clark.totoro.assets.repository.FileRepository
import com.clark.totoro.assets.service.AuthService
import com.clark.totoro.assets.service.FileService
import com.clark.totoro.assets.utils.Utils
import jakarta.validation.constraints.Min
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.*
import org.springframework.web.server.ResponseStatusException
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.ObjectIdentifier

@Component
class FilesController(
    val s3: S3Config,
    val aws: FileService,
    val auth: AuthService,
    val rbac: RbacConfig,
    val fileRep: FileRepository
) {
    @Autowired
    private lateinit var utils: Utils

    @Value("\${files.limit}")
    @field:Min(value = 1, message = "items per page at lease 1")
    val limit: Int = 0

    suspend fun createFiles(
        request: ServerRequest
    ): ServerResponse {
        val token = request.headers().header("Authorization").first() ?: throw ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "authorization header missing"
        )
        val names = request.pathVariable("names").split(",")
        val resolvedToken = utils.resolveToken(token)
        val user = auth.authService().parseJwtToken(resolvedToken)
        val id = user.id
        val owner = user.owner
        val userId = id.let { utils.base64Encoding(it) }
        val count = names.size
        //coroutines here concurrently
        val combined = try {
            //current.await() + size > limit.await()
            val uploaded = aws.isOutLimit(userId)
            val pass = userId.let { rbac.cas().enforce(it, "${owner}_file", "write") }
            val isLimited = uploaded.uploaded + count > uploaded.limit
            if (isLimited) throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "out of limit")
            else pass
        } catch (e: Exception) {
            println(e.message)
            throw Exception(e.message)
        }
        return if (combined) ServerResponse.ok().bodyValueAndAwait(
            aws.clientUpload(
                names,
                userId
            )
        ) else throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization failed")
    }

    /**
     * 成功后记录上传文件数据, 写入image和presignedImage tables
     */
    suspend fun updateFileCache(
        request: ServerRequest
    ): ServerResponse {
        return try {
            val body = request.awaitBody(FileList::class)
            val token = request.headers().header("Authorization").first() ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "authorization header missing"
            )
            val resolvedToken = utils.resolveToken(token)
            val user = auth.authService().parseJwtToken(resolvedToken)
            val id = user.id
            val owner = user.owner
            val userId = id.let { utils.base64Encoding(it) }
            val pass = userId.let { rbac.cas().enforce(it, "${owner}_file", "write") }
            if (!pass) throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization failed")
            //get the last {success} objects from s3
            fileRep.createImage(userId, body.files)
            ServerResponse.ok().bodyValueAndAwait(body.files.size)
        } catch (e: Exception) {
            println(e.message)
            throw e
        }
    }

    suspend fun deleteOwnFile(
        request: ServerRequest
    ): ServerResponse {
        try {
            val body = request.awaitBody(FileList::class)
            val token = request.headers().header("Authorization").first() ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "authorization header missing"
            )
            val resolvedToken = utils.resolveToken(token)
            val user = auth.authService().parseJwtToken(resolvedToken)
            val idCas = user.id
            val userId = idCas.let { utils.base64Encoding(it) }

            val names = body.files
            println(names)
            val images = names.map {
                val temp = it.split("⟨").last()
                println(temp)
                val id = temp.split(":").first()
                println("id: $id")
                val name = temp.split(":").last().split("⟩").first()
                Image(name, id)
            }
            val author = images.all { it.userId == userId }
            if (!author) throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization failed")

            val keys = images.map { ObjectIdentifier.builder().key("assets/users/$userId/${it.name}").build() }
            val dels = Delete.builder()
                .objects(keys)
                .build()
            val delRequest = DeleteObjectsRequest.builder()
                .bucket("doggycatty")
                .delete(dels)
                .build()
            //delete database files
            fileRep.deleteFiles(names)
            val res = s3.s3Client().deleteObjects(delRequest).deleted()
            return ServerResponse.ok().bodyValueAndAwait(res.map { it.key() })
        } catch (e: Exception) {
            println(e.message)
            throw e
        }
    }

    suspend fun countUploaded(request: ServerRequest): ServerResponse {
        return try {
            val token = request.headers().header("Authorization").first() ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "authorization header missing"
            )
            val resolvedToken = utils.resolveToken(token)
            val user = auth.authService().parseJwtToken(resolvedToken)
            val id = user.id
            val userId = id.let { utils.base64Encoding(it) }

            ServerResponse.ok().bodyValueAndAwait(aws.isOutLimit(userId))
        } catch (e: Exception) {
            println(e.message)
            throw e
        }
    }

    suspend fun correctFilesAndDb(request: ServerRequest): ServerResponse {
        return try {
            val userId = request.pathVariable("user")
            val fileList = aws.getFileList(userId).toList()
            val fileRecords = fileRep.getFiles(userId)
            val diffs = fileList.filter { file -> !fileRecords.any { it.name == file.split("/").last() } }
                .map { it.split("/").last() }
            if (diffs.isNotEmpty()) {
                fileRep.createImage(userId, diffs)
            }
            ServerResponse.ok().bodyValueAndAwait(diffs)
        } catch (e: Exception) {
            println(e.message)
            throw e
        }
    }
}