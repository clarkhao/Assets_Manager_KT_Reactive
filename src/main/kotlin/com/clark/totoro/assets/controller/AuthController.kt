package com.clark.totoro.assets.controller

import com.clark.totoro.assets.config.RbacConfig
import com.clark.totoro.assets.model.Token
import com.clark.totoro.assets.model.User
import com.clark.totoro.assets.repository.UserRepository
import com.clark.totoro.assets.service.AuthService
import com.clark.totoro.assets.utils.Utils
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.kotlin.core.publisher.toMono
import kotlin.random.Random

@RestController
class AuthController(val auth: AuthService, val rbac: RbacConfig, val userRep: UserRepository) {
    @Autowired
    private lateinit var utils: Utils

    @CrossOrigin
    @PostMapping("api/signin")
    suspend fun signin(@RequestParam code: String, @RequestParam state: String): Token {
        return try {
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
            val limitQuery = userRep.getUploadLimit(userId)
            val limit = if (limitQuery == 0) (if (roles.any {
                    it in listOf<String>(
                        "admin",
                        "root"
                    )
                }) 10000 else 5) else limitQuery
            val publicToken = if (roles.any { it in listOf<String>("admin", "root") }) utils.createScopeToken(
                "admin",
                "admin_token",
                "publicUser:$userId",
                604800000
            ) else utils.createScopeToken("user", "user_token", "publicUser:$userId", 604800000)
            val account =
                User(
                    user.owner, user.name, user.createdTime,
                    user.avatar,
                    user.email,
                    user.signupApplication,
                    limit,
                    userId,
                    roles
                )
            //set for initiate uploading records, key: ${userId}_upload async
            val scope = CoroutineScope(Dispatchers.IO)
            try {
                scope.async {
                    try {
                        userRep.createUser(account)
                    } catch (e: Exception) {
                        throw e
                    }
                }.await()
            } catch (e: Exception) {
                println("scope error")
            } finally {
                scope.cancel()
            }
            Token(
                token, account, publicToken
            )
        } catch (e: Exception) {
            println(e.message)
            throw Exception(e.message)
        }
    }

    @PostMapping("api/signout")
    fun signout(
        @RequestParam id_token_hint: String,
        @RequestParam post_logout_redirect_uri: String,
        @RequestParam state: String
    ) {

    }
}