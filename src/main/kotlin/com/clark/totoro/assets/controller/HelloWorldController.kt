package com.clark.totoro.assets.controller

import com.clark.totoro.assets.repository.UserRepository
import com.clark.totoro.assets.utils.Utils
import com.surrealdb.driver.model.QueryResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController() {
    @Autowired
    private lateinit var utils: Utils
    @GetMapping("api/hello")
    fun hello(): String {
        return utils.createDbToken(1111111)
    }

}