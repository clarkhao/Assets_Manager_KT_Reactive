package com.clark.totoro.assets.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VerifyConfig {
    @Value(("\${files.type.jpg}"))
    val jpg: String = ""
    @Value("\${files.type.png}")
    val png: String = ""

    @Bean
    fun getContentType(): Map<String, String> {
        return mapOf<String, String>(
            "jpeg" to jpg,
            "png" to png
        )
    }
}