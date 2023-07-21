package com.clark.totoro.assets.controller

import com.clark.totoro.assets.model.CreateLike
import com.clark.totoro.assets.model.LikeFullQuery
import com.clark.totoro.assets.model.LikeModel
import com.clark.totoro.assets.repository.FileRepository
import com.clark.totoro.assets.utils.Utils
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import java.time.Duration

@Component
class PushController(val fileRep: FileRepository) {
    @Autowired
    private lateinit var utils: Utils
    suspend fun createLike(request: ServerRequest): ServerResponse {
        val body = request.awaitBody(CreateLike::class)
        val key = body.key
        val user = body.user
        fileRep.createLike(key, user)
        return ServerResponse.ok().bodyValueAndAwait("ok")
    }

    suspend fun deleteLike(request: ServerRequest): ServerResponse {
        val body = request.awaitBody(CreateLike::class)
        fileRep.deleteLike(body.key, body.user)
        return ServerResponse.ok().bodyValueAndAwait("ok")
    }

    suspend fun streamLikes(request: ServerRequest): ServerResponse {
        return try {
            val user = request.queryParamOrNull("user") ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "missing parameter user"
            )
            ServerResponse.ok().sse().bodyValueAndAwait(
                Flux.interval(Duration.ofSeconds(16))
                    .map { fileRep.getLikeData(user) }
            )

        } catch (e: Exception) {
            println(e.message)
            throw e
        }
    }

    suspend fun getLikeData(request: ServerRequest): ServerResponse {
        val user = request.pathVariable("user")
        return ServerResponse.ok().bodyValueAndAwait(
            fileRep.getLikeData(user)
        )
    }
}