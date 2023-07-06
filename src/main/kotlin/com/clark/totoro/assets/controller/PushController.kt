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
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import java.time.Duration

@RestController
class PushController(val fileRep: FileRepository) {
    @Autowired
    private lateinit var utils: Utils
    @CrossOrigin
    @PostMapping("api/likes")
    suspend fun createLike(@RequestBody body: CreateLike): ResponseEntity<Unit> {
        val key = body.key
        val user = body.user
        fileRep.createLike(key, user)
        return ResponseEntity<Unit>(HttpStatus.OK)
    }
    @CrossOrigin
    @DeleteMapping("api/likes")
    fun deleteLike(@RequestBody body: CreateLike): ResponseEntity<Unit> {
        fileRep.deleteLike(body.key, body.user)
        return ResponseEntity<Unit>(HttpStatus.OK)
    }
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
    @CrossOrigin
    @GetMapping("api/likes/{user}")
    suspend fun getLikeData(@PathVariable user: String): LikeModel {
        return fileRep.getLikeData(user)
    }
}