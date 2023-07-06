package com.clark.totoro.assets.controller

import com.clark.totoro.assets.config.RbacConfig
import com.clark.totoro.assets.model.User
import com.clark.totoro.assets.model.UserAuthor
import com.clark.totoro.assets.repository.UserRepository
import com.clark.totoro.assets.service.AuthService
import com.clark.totoro.assets.service.RbacService
import com.clark.totoro.assets.utils.Utils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    val auth: AuthService,
    val rbac: RbacConfig,
    val userRep: UserRepository,
    val rbacSer: RbacService
) {
    @Autowired
    private lateinit var utils: Utils
    @CrossOrigin
    @PutMapping("api/user")
    fun updateUserAuthor(@RequestBody body: UserAuthor, @RequestHeader("Authorization") token: String): User {
        //limit and roles
        val resolvedToken = utils.resolveToken(token)
        val user = auth.authService().parseJwtToken(resolvedToken)
        val id = user.id
        val owner = user.owner
        val userId = id.let { utils.base64Encoding(it) }
        val pass = userId.let { rbac.cas().enforce(it, "${owner}_user", "write") }
        if (!pass) throw Exception("Authorization failed")

        val originalRoles = rbac.cas().getRolesForUser(body.id.split(":").last())
        val addRole = "admin" in body.role && "admin" !in originalRoles
        val deleteRole = "admin" !in body.role && "admin" in originalRoles
        val currentRoles = if (addRole) {
            rbacSer.addAdminRole(body.id.split(":").last())
        } else if(deleteRole) {
            rbacSer.deleteAdminRole(body.id.split(":").last())
        } else {
            originalRoles
        }
        val userRes = userRep.updateUserAdmin(body.id.split(":").last(), body.copy(role = currentRoles))
        println(userRes)
        return userRes[0]
    }
}