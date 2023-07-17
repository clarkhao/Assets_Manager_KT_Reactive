package com.clark.totoro.assets.controller

import com.clark.totoro.assets.model.ErrorMessage
import org.apache.logging.log4j.core.tools.picocli.CommandLine.MissingParameterException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException

//对exceptions进行分类，给出更多的类型
@ControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleRuntimeException(e: Exception): ResponseEntity<ErrorMessage> {
        println(e.message)
        return when (e.message) {
            "Authorization failed" -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorMessage(403, e.message.toString()))

            "out of limit" -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorMessage(403, e.message.toString()))

            "like exist" -> ResponseEntity.status(HttpStatus.OK)
                .body(ErrorMessage(200, e.message.toString()))

            "like not exist" -> ResponseEntity.status(HttpStatus.OK)
                .body(ErrorMessage(200, e.message.toString()))

            else -> buildErrorResponse(e)
        }
    }

    private fun buildErrorResponse(e: Exception): ResponseEntity<ErrorMessage> {
        if (e is ResponseStatusException) {
            return ResponseEntity.status(e.statusCode)
                .body(ErrorMessage(e.statusCode.toString().toInt(), e.reason ?: ""))
        }
        val badResponse = e.message.toString().split(" ")
        val statusCode = badResponse.first().toInt()
        val resMsg = badResponse.drop(1).joinToString(separator = " ")
        return ResponseEntity.status(statusCode ?: 500)
            .body(ErrorMessage(statusCode ?: 500, resMsg))
    }
}