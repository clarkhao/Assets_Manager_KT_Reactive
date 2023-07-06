package com.clark.totoro.assets.service

import com.clark.totoro.assets.config.AuthConfig
import org.casbin.casdoor.service.CasdoorAuthService
import org.casbin.casdoor.service.CasdoorUserService
import org.springframework.stereotype.Service

@Service
class AuthService(val auth: AuthConfig) {
    fun userService(): CasdoorUserService {
        return CasdoorUserService(auth.config())
    }
    fun authService(): CasdoorAuthService {
        return CasdoorAuthService(auth.config())
    }
}