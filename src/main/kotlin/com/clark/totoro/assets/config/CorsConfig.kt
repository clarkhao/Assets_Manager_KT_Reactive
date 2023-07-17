package com.clark.totoro.assets.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.util.concurrent.TimeUnit

@Configuration
@EnableWebFlux
class CorsConfig : WebFluxConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173", "https://imageplus.clarkhao.repl.co")
            .allowedMethods("PUT", "GET", "POST", "DELETE")
            .allowedHeaders("Authorization", "Content-Type")
            .allowCredentials(true).maxAge(3600)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler(
            "/assets/**",
            "/avatar.svg",
            "/collection.riv",
            "/folder.riv",
            "/question.svg",
            "/small.riv",
            "/upload_bg.png",
            "/vite.svg",

        )
            .addResourceLocations("classpath:/static/assets/", "classpath:/static/")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
    }
}
