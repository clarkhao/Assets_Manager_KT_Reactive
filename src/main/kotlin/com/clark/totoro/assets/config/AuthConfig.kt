package com.clark.totoro.assets.config

import org.casbin.casdoor.config.CasdoorConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class AuthConfig {
    @Value("\${casdoor.endpoint}")
    val endpoint: String = ""

    @Value("\${casdoor.client-id}")
    val clientId: String = ""

    @Value("\${casdoor.client-secret}")
    val clientSecret: String = ""

    @Value("\${casdoor.certificate}")
    val certificate: String = ""

    @Value("\${casdoor.organization-name}")
    val organizationName: String = ""

    @Value("\${casdoor.application-name}")
    val applicationName: String = ""

    @Bean
    fun config(): CasdoorConfig {
        return CasdoorConfig(endpoint, clientId, clientSecret, certificate, organizationName, applicationName)
    }
    @Bean
    fun authHeader(): String {
        val credentials = "${clientId}:${clientSecret}"
        val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray())
        return "Basic $encodedCredentials"
    }
}