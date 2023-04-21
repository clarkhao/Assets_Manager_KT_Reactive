package com.clark.totoro.assets.service

import com.clark.totoro.assets.config.S3Config
import com.clark.totoro.assets.config.VerifyConfig
import com.clark.totoro.assets.model.FileMerge
import com.clark.totoro.assets.model.S3File
import com.clark.totoro.assets.model.S3FileUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest

@Service
class FileService(var s3: S3Config, var verify: VerifyConfig) {
    fun clientUpload(names: List<String>): Flux<S3FileUrl> {
        try {
            return names.toFlux().map {
                PutObjectRequest.builder()
                    .bucket("doggycatty")
                    .key("assets/users/$it")
                    .contentType(verify.getContentType().get(it.split(".").last()))
                    .build()
            }
                .map {
                    PutObjectPresignRequest.builder()
                        .signatureDuration(java.time.Duration.ofMinutes(10))
                        .putObjectRequest(it)
                        .build()
                }
                .map { s3.presigner().presignPutObject(it).url().toString() }
                .map { S3FileUrl(it) }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getFileList(page: Int, limit: Int): List<S3File> {
        val request: ListObjectsRequest = ListObjectsRequest.builder()
            .bucket("doggycatty")
            .build()
        val res = CoroutineScope(Dispatchers.IO).async {
            s3.s3Client().listObjects(request)
        }
        return res.await().contents()
            .filter { it.key().startsWith("assets/users/") }
            .sortedBy { it.lastModified() }
            .filterIndexed { index, _ -> index > (page - 1) * limit }
            .take(limit)
            .map {
                S3File(it.key(), it.lastModified().toString(), it.key().split(".").last())
            }
    }

    fun getPresignedFileList(files: List<S3File>): Flux<FileMerge> {
        return Flux.fromIterable(files).map {
            GetObjectRequest.builder()
                .bucket("doggycatty")
                .key(it.key)
                .responseContentType(it.contentType)
                .build()
        }.map {
            GetObjectPresignRequest.builder()
                .signatureDuration(java.time.Duration.ofMinutes(10))
                .getObjectRequest(it)
                .build()
        }.map {
            s3.presigner().presignGetObject(it).url().toString()
        }.zipWith(Flux.fromIterable(files)) { a, b ->
            FileMerge(a, b.key, b.date)
        }
    }

    fun getPresignedFile(name: String): String {
        val request =
            GetObjectRequest.builder()
                .bucket("doggycatty")
                .key("assets/users/$name")
                .responseContentType(name.split(".").last())
                .build()
        val presignRequest =
            GetObjectPresignRequest.builder()
                .signatureDuration(java.time.Duration.ofMinutes(10))
                .getObjectRequest(request)
                .build()
        return s3.presigner().presignGetObject(presignRequest).url().toString()
    }

}