package com.clark.totoro.assets.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LoggerConfig {
    @Bean
    fun setupLog() = LoggerFactory.getLogger("LoggerConfig")
}