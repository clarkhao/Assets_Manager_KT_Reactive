package com.clark.totoro.assets.controller

import com.clark.totoro.assets.config.S3Config
import com.clark.totoro.assets.model.S3File
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsRequest
import software.amazon.awssdk.services.s3.model.S3Object
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDate

@RestController
class FilesController(var s3: S3Config) {
    @Value("\${files.limit}")
    val limit: Int = 0
    @PostMapping("file")
    suspend fun createFiles() {

    }
    @GetMapping("files")
    suspend fun listFiles(@RequestParam page: Int): Sequence<S3File> {
        val request: ListObjectsRequest = ListObjectsRequest.builder()
            .bucket("doggycatty")
            .build()
        val res = CoroutineScope(Dispatchers.IO).async {
                s3.amazonS3().listObjects(request)
            }
        val seq = res.await().contents().filter { it.key().startsWith("assets/users/") }
            .map { el -> S3File(el.key(), el.lastModified().toString()) }.asSequence()
        return seq.sortedBy { it.date }.filterIndexed { i, el -> i > limit * (page - 1) }.take(limit)
    }

    @GetMapping("files/{name}")
    suspend fun getFile(@PathVariable name: String, response: ServerHttpResponse): ByteArrayInputStream {
        val request: GetObjectRequest = GetObjectRequest.builder()
            .key("assets/users/$name")
            .bucket("doggycatty")
            .build()
        val res = CoroutineScope(Dispatchers.IO).async {
            s3.amazonS3().getObject(request).response()
        }

    }

    @PutMapping("files/{name}")
    fun updateFile(@PathVariable name: String) {
    }

    @DeleteMapping("files/{name}")
    fun deleteFile(@PathVariable name: String) {
    }

    @DeleteMapping("files/{names}")
    fun deleteFileGroup(@PathVariable names: List<String>) {
    }
}