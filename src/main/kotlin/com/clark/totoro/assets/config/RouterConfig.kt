package com.clark.totoro.assets.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
@Configuration
class RouterConfig {
    @Bean
    fun customRouter(@Value("classpath:/static/index.html") html: Resource): RouterFunction<ServerResponse> {
        return router {
            GET("/") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/cn") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/jp") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/callback") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/upload") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/cn/upload") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/jp/upload") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/profile") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/cn/profile") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/jp/profile") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/account") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/cn/account") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
            GET("/jp/account") { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
        }
    }
}