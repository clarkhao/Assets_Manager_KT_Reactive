package com.clark.totoro.assets.config

import software.amazon.awssdk.services.s3.S3Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Configuration
class S3Config {
    @Bean
    fun s3Client(): S3Client {
        return S3Client.builder()
            .region(Region.US_EAST_1)
            .build()
    }
    @Bean
    fun presigner(): S3Presigner {
        return S3Presigner.create()
    }
}