package com.clark.totoro.assets.controller

import com.clark.totoro.assets.model.LikeModel
import com.clark.totoro.assets.repository.FileRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.time.Duration

@RestController
class StreamController(val fileRep: FileRepository) {
    @CrossOrigin
    @GetMapping("api/likes/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun streamLikes(@RequestParam user: String): Flux<LikeModel> {
        return try {
            Flux.interval(Duration.ofSeconds(16))
                .map { fileRep.getLikeData(user) }
        } catch (e: Exception) {
            println(e.message)
            throw e
        }
    }
}