package com.clark.totoro.assets.controller

import com.clark.totoro.assets.annotation.ToLog
import com.clark.totoro.assets.config.S3Config
import com.clark.totoro.assets.model.FileMerge
import com.clark.totoro.assets.model.S3FileUrl
import com.clark.totoro.assets.service.FileService
import jakarta.validation.constraints.Min
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import java.net.URI


@RestController
@Validated
class FilesController(var s3: S3Config, var service: FileService) {
    @Value("\${files.limit}")
    @field:Min(value = 1, message = "items per page at lease 1")
    val limit: Int = 0

    @PostMapping("file/{names}")
    suspend fun createFiles(@PathVariable names: List<String>): Flux<S3FileUrl> {
        return service.clientUpload(names)
    }

    @GetMapping("files")
    @ToLog
    suspend fun listFiles(@RequestParam page: Int, exchange: ServerWebExchange): Flux<FileMerge> {
        val files = service.getFileList(page, limit)
        return service.getPresignedFileList(files)
    }

    @GetMapping("files/{name}")
    suspend fun getFile(@PathVariable name: String, exchange: ServerWebExchange): Mono<ResponseEntity<Void>> {
        val url = service.getPresignedFile(name)
        val headers = HttpHeaders()
        headers.location = URI.create(url)
        return Mono.just(ResponseEntity<Void>(headers, HttpStatus.MOVED_PERMANENTLY))
        /*
        exchange.response.setStatusCode(HttpStatus.PERMANENT_REDIRECT)
        exchange.response.headers.set("Location", url)
        return exchange.response.setComplete()
         */
    }

    @DeleteMapping("files/{names}")
    fun deleteFileGroup(@PathVariable names: List<String>): List<String> {
        val keys = names.map { ObjectIdentifier.builder().key("assets/users/$it").build() }
        val dels = Delete.builder()
            .objects(keys)
            .build()
        val delRequest = DeleteObjectsRequest.builder()
            .bucket("doggycatty")
            .delete(dels)
            .build()
        val res = s3.s3Client().deleteObjects(delRequest).deleted()
        return res.map { it.key() }
    }
}
