package com.clark.totoro.assets.controller

import com.clark.totoro.assets.model.ErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
//对exceptions进行分类，给出更多的类型
@ControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<ErrorMessage> {
        val errorResponse = buildErrorResponse(e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }

    private fun buildErrorResponse(e: Exception): ErrorMessage {
        val badResponse = e.message.toString().split(" ")
        val statusCode = badResponse.first().toInt()
        val resMsg = badResponse.drop(1).joinToString(separator = " ")
        return ErrorMessage(statusCode ?: 500, resMsg)
    }
}