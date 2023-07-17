package com.clark.totoro.assets.controller

import com.clark.totoro.assets.config.I18nConfig
import com.clark.totoro.assets.config.RbacConfig
import com.clark.totoro.assets.model.Limit
import com.clark.totoro.assets.model.PublicUserWithId
import com.clark.totoro.assets.model.Token
import com.clark.totoro.assets.model.User
import com.clark.totoro.assets.repository.UserRepository
import com.clark.totoro.assets.service.AuthService
import com.clark.totoro.assets.service.FileService
import com.clark.totoro.assets.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull
import org.springframework.web.server.ResponseStatusException
import kotlin.random.Random

@Component
class AuthController(
    val auth: AuthService,
    val rbac: RbacConfig,
    val userRep: UserRepository,
    val fileServe: FileService,
    val i18n: I18nConfig
) {
    @Autowired
    private lateinit var utils: Utils

    suspend fun signin(request: ServerRequest): ServerResponse {
        return try {
            val code = request.queryParamOrNull("code") ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "missing parameter code"
            )
            val state = request.queryParamOrNull("state") ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "missing parameter state"
            )
            //get token and verify the token with pem
            val token = auth.authService().getOAuthToken(code, state)
            val user = token.let { auth.authService().parseJwtToken(it) }
                .let {
                    if (it.avatar === "" || it.avatar.endsWith("casbin.svg")) it.avatar =
                        "https://api.dicebear.com/6.x/pixel-art/svg?seed=${
                            ('a'..'z').toList()[Random.nextInt(
                                26
                            )]
                        }"
                    it
                }
            val userId = user.let { utils.base64Encoding(it.id) }

            //encode the user id
            //get the roles of user id
            val roles = rbac.cas().getRolesForUser(userId).let {
                if (it.isEmpty()) {
                    rbac.cas().addRoleForUser(userId, "user")
                    listOf("user")
                } else {
                    it
                }
            }
            val account =
                User(
                    userId,
                    user.name,
                    user.email,
                    user.avatar,
                    roles
                )
            val publicToken = if (roles.any { it in listOf<String>("admin", "root") }) utils.createScopeToken(
                "admin",
                "admin_token",
                "publicUser:$userId",
                604800000
            ) else utils.createScopeToken("user", "user_token", "publicUser:$userId", 604800000)
            val locale = LocaleContextHolder.getLocale()
            ServerResponse.ok().bodyValueAndAwait(
                Token(token, account, publicToken, locale.toString())
            )
        } catch (e: Exception) {
            println(e.message)
            throw Exception(e.message)
        }
    }

    suspend fun userInfo(request: ServerRequest): ServerResponse {
        val token = request.headers().header("Authorization").first() ?: throw ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "authorization header missing"
        )
        val resolvedToken = utils.resolveToken(token)
        val user = resolvedToken.let { auth.authService().parseJwtToken(it) }
            .let {
                if (it.avatar === "" || it.avatar.endsWith("casbin.svg")) it.avatar =
                    "https://api.dicebear.com/6.x/pixel-art/svg?seed=${
                        ('a'..'z').toList()[Random.nextInt(
                            26
                        )]
                    }"
                it
            }
        val userId = user.let { utils.base64Encoding(it.id) }
        val roles = rbac.cas().getRolesForUser(userId)

        val limitQuery = userRep.getUploadLimit(userId)
        val limit = if (limitQuery == -1) (if (roles.any {
                it in listOf<String>(
                    "admin",
                    "root"
                )
            }) 10000 else 5) else limitQuery
        val uploaded = fileServe.getUploadCurrent(userId)
        val account =
            PublicUserWithId(
                userId,
                user.name,
                user.avatar,
                user.email,
                limit,
                uploaded,
                roles
            )
        //setforinitiateuploadingrecords,key:${userId}_uploadasync
        userRep.createUser(account)

        return ServerResponse.ok().bodyValueAndAwait(Limit(limit, uploaded))
    }
}