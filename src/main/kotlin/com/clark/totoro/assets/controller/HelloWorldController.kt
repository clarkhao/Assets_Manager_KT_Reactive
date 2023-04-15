package com.clark.totoro.assets.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController {
    @GetMapping("hello")
    fun getGreeting(): String {
        return "hello"
    }
}