package com.clark.totoro.assets.controller

import com.clark.totoro.assets.service.RbacService
import com.clark.totoro.assets.utils.Utils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PreController(val pre: RbacService) {
    @Autowired
    private lateinit var utils: Utils

    @GetMapping("api/preset/roles")
    fun setRoles(): List<String> {
        return pre.preSetRoles()
    }
    @GetMapping("api/preset/policies")
    fun setPolicies(): List<List<String>> {
        return pre.preSetPolicies()
    }
    @DeleteMapping("api/preset/{user}")
    fun deleteUser(@PathVariable user: String): Boolean {
        return pre.deleteUser(user)
    }
}