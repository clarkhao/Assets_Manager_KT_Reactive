package com.clark.totoro.assets.aspect

import com.clark.totoro.assets.config.LoggerConfig
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange

@Aspect
@Component
class Logger(var log: LoggerConfig) {
    @AfterReturning("@annotation(com.clark.totoro.assets.annotation.ToLog)", returning = "result")
    fun logResponse(joinPoint: JoinPoint) {
        val exchage = joinPoint.args.find { it is ServerWebExchange } as? ServerWebExchange
        log.setupLog().info("${exchage?.request?.id} request ${exchage?.request?.path}::${exchage?.response?.statusCode}")
    }
}