package com.clark.totoro.assets.controller

import com.clark.totoro.assets.config.RbacConfig
import com.clark.totoro.assets.model.UserAuthor
import com.clark.totoro.assets.repository.UserRepository
import com.clark.totoro.assets.service.AuthService
import com.clark.totoro.assets.service.RbacService
import com.clark.totoro.assets.utils.Utils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.server.ResponseStatusException

@Component
class UserController(
    val auth: AuthService,
    val rbac: RbacConfig,
    val userRep: UserRepository,
    val rbacSer: RbacService
) {
    @Autowired
    private lateinit var utils: Utils

    suspend fun updateUserAuthor(request: ServerRequest): ServerResponse {
        val body = request.awaitBody(UserAuthor::class)
        val token = request.headers().header("Authorization").first() ?: throw ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "authorization header missing"
        )
        //limit and roles
        val resolvedToken = utils.resolveToken(token)
        val user = auth.authService().parseJwtToken(resolvedToken)
        val id = user.id
        val owner = user.owner
        val userId = id.let { utils.base64Encoding(it) }
        val pass = userId.let { rbac.cas().enforce(it, "${owner}_user", "write") }
        if (!pass) throw Exception("Authorization failed")
        println(body.role)

        val originalRoles = rbac.cas().getRolesForUser(body.id.split(":").last())
        println(originalRoles)
        val addRole = "admin" in body.role && "admin" !in originalRoles
        val deleteRole = "admin" !in body.role && "admin" in originalRoles
        val currentRoles = if (addRole) {
            println("add role")
            rbacSer.addAdminRole(body.id.split(":").last())
        } else if (deleteRole) {
            println("delete role")
            rbacSer.deleteAdminRole(body.id.split(":").last())
        } else {
            originalRoles
        }
        val userRes = userRep.updateUserAdmin(body.id.split(":").last(), body.copy(role = currentRoles))
        println(userRes)
        return ServerResponse.ok().bodyValueAndAwait(userRes[0])
    }
}