package com.clark.totoro.assets.config

import software.amazon.awssdk.services.s3.S3Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region

@Configuration
class S3Config {
    @Bean
    fun amazonS3(): S3Client {
        return S3Client.builder()
            .region(Region.US_EAST_1)
            .build()
    }
}