package com.clark.totoro.assets.config

import com.clark.totoro.assets.controller.AuthController
import com.clark.totoro.assets.controller.FilesController
import com.clark.totoro.assets.controller.PushController
import com.clark.totoro.assets.controller.UserController
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import java.net.URI

@Configuration
class RouterConfig(
    private val authHandler: AuthController,
    private val fileHandler: FilesController,
    private val pushHandler: PushController,
    private val userHandler: UserController
) {
    @Bean
    fun apiRouter(@Value("classpath:/static/index.html") html: Resource) = coRouter {
        accept(MediaType.TEXT_HTML).nest {
            GET("/") { ServerResponse.temporaryRedirect(URI("/cn")).bodyValueAndAwait(html) }
            GET("/en") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/cn") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/jp") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/callback") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/en/upload") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/cn/upload") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/jp/upload") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/en/profile") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/cn/profile") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/jp/profile") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/en/account") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/cn/account") { ServerResponse.ok().bodyValueAndAwait(html) }
            GET("/jp/account") { ServerResponse.ok().bodyValueAndAwait(html) }
        }
        accept(MediaType.APPLICATION_JSON).nest {
            "/api/signin".nest {
                GET("", authHandler::signin)
                "/user".nest {
                    GET("", authHandler::userInfo)
                }
            }
            "/api/files".nest {
                GET("/{names}", fileHandler::createFiles)
                POST("", fileHandler::updateFileCache)
                DELETE("", fileHandler::deleteOwnFile)
                "/count".nest {
                    GET("", fileHandler::countUploaded)
                }
                PUT("/{user}", fileHandler::correctFilesAndDb)
            }
            "/api/likes".nest {
                POST("", pushHandler::createLike)
                DELETE("", pushHandler::deleteLike)
                GET("/{user}", pushHandler::getLikeData)
            }
            "/api/user".nest {
                PUT("", userHandler::updateUserAuthor)
            }
        }
    }
}