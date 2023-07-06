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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import java.net.URI

@RestController
@Validated
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

    @CrossOrigin()
    @GetMapping("api/files/upload/{names}")
    suspend fun createFiles(
        @PathVariable names: Array<String>,
        @RequestHeader("Authorization") token: String
    ): List<S3FileUrl> {
        val resolvedToken = utils.resolveToken(token)
        val user = auth.authService().parseJwtToken(resolvedToken)
        val id = user.id
        val owner = user.owner
        val userId = id.let { utils.base64Encoding(it) }
        val count = names.size
        //coroutines here concurrently
        val scope = CoroutineScope(Dispatchers.IO)
        try {
            val combined = scope.async {
                try {
                    //current.await() + size > limit.await()
                    val uploaded = async { aws.isOutLimit(userId) }
                    val pass = async { userId.let { rbac.cas().enforce(it, "${owner}_file", "write") } }
                    val isLimited = uploaded.await().uploaded + count > uploaded.await().limit
                    if (isLimited) throw Exception("out of limit")
                    else pass.await()
                } catch (e: Exception) {
                    println(e.message)
                    throw Exception(e.message)
                }
            }
            return if (combined.await()) aws.clientUpload(
                names,
                userId
            ) else throw Exception("Authorization failed")
        } catch (e: Exception) {
            throw e
        } finally {
            scope.cancel()
        }
    }

    /**
     * 成功后记录上传文件数据, 写入image和presignedImage tables
     */
    @CrossOrigin
    @PostMapping("api/files/upload")
    suspend fun updateFileCache(
        @RequestBody body: FileList,
        @RequestHeader("Authorization") token: String
    ): Int {
        val resolvedToken = utils.resolveToken(token)
        val user = auth.authService().parseJwtToken(resolvedToken)
        val id = user.id
        val owner = user.owner
        val userId = id.let { utils.base64Encoding(it) }
        val pass = userId.let { rbac.cas().enforce(it, "${owner}_file", "write") }
        if (!pass) throw Exception("Authorization failed")
        //get the last {success} objects from s3
        val scope = CoroutineScope(Dispatchers.IO)
        try {
            scope.async {
                val files = body.files
                println("files:$files")
                files.map {
                    async {
                        fileRep.createImage(userId, it)
                    }.await()
                }
            }.await()
            return body.files.size
        } catch (e: Exception) {
            throw e
        } finally {
            scope.cancel()
        }
    }

    /**
     * more results when 201, no more when 200
     */
    @CrossOrigin
    @GetMapping("api/files/update")
    suspend fun uploadExpiredFiles(): ResponseEntity<Unit> {
        val files = fileRep.getExpiredFiles()
        val scope = CoroutineScope(Dispatchers.IO)
        try {
            scope.async {
                files.map {
                    async {
                        fileRep.updateFile(it)
                    }.await()
                }
            }.await()
            return if (files.isEmpty()) ResponseEntity<Unit>(HttpStatus.OK)
            else ResponseEntity<Unit>(HttpStatus.CREATED)
        } catch (e: Exception) {

            throw e
        } finally {
            scope.cancel()
        }
    }

    @GetMapping("api/files/{name}")
    suspend fun getFile(@PathVariable name: String): ResponseEntity<Unit> {
        val url = aws.getPresignedFile(name)
        val headers = HttpHeaders()
        headers.location = URI.create(url)
        return ResponseEntity<Unit>(headers, HttpStatus.MOVED_PERMANENTLY)
    }

    @CrossOrigin
    @DeleteMapping("api/files")
    suspend fun deleteOwnFile(
        @RequestBody body: FileList,
        @RequestHeader("Authorization") token: String
    ): List<String> {
        try {
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
            if (!author) throw Exception("Authorization failed")

            val scope = CoroutineScope(Dispatchers.IO)
            try {
                val res = scope.async {
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
                    s3.s3Client().deleteObjects(delRequest).deleted()
                }
                return res.await().map { it.key() }
            } catch (e: Exception) {
                println(e.message)
                throw e
            } finally {
                scope.cancel()
            }
        } catch (e: Exception) {
            println(e.message)
            throw e
        }
    }

    @CrossOrigin
    @GetMapping("api/files/uploaded")
    suspend fun countUploaded(@RequestHeader("Authorization") token: String): Uploaded {
        return try {
            val resolvedToken = utils.resolveToken(token)
            val user = auth.authService().parseJwtToken(resolvedToken)
            val id = user.id
            val userId = id.let { utils.base64Encoding(it) }

            aws.isOutLimit(userId)
        } catch (e: Exception) {
            println(e.message)
            throw e
        }
    }
}