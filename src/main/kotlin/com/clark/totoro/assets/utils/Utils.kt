package com.clark.totoro.assets.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Component
class Utils {
    @Value("\${db.jwt.secret}")
    val secret: String = ""
    fun base64Encoding(id: String): String {
        val userIdBytes = id.toByteArray(StandardCharsets.UTF_8)
        val encodedBytes = Base64.getEncoder().encode(userIdBytes)
        val encodedUserId = String(encodedBytes, StandardCharsets.UTF_8)
        return encodedUserId
    }

    fun resolveToken(token: String): String {
        val prefix = "Bearer "
        return if (token.startsWith(prefix)) token.split(" ")[1]
        else throw Exception("Authorization failed")
    }

    fun createScopeToken(sc: String,tk: String,id: String, expirationMillis: Long): String {
        val nowMillis = System.currentTimeMillis()
        val expiration = Date(nowMillis + expirationMillis)
        val key = Keys.hmacShaKeyFor(secret.toByteArray())
        return Jwts.builder()
            .signWith(key, SignatureAlgorithm.HS512)
            .claim("ns", "test")
            .claim("db", "test")
            .claim("sc", sc)
            .claim("tk", tk)
            .claim("id", id)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(expiration)
            .compact()
    }
    fun createDbToken(expirationMillis: Long): String {
        val nowMillis = System.currentTimeMillis()
        val expiration = Date(nowMillis + expirationMillis)
        val key = Keys.hmacShaKeyFor(secret.toByteArray())
        return Jwts.builder()
            .signWith(key, SignatureAlgorithm.HS512)
            .claim("ns", "test")
            .claim("db", "test")
            .claim("tk", "test_db_token")
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(expiration)
            .compact()
    }

    fun verifyToken(jwt: String): Claims {
        val key = Keys.hmacShaKeyFor(secret.toByteArray())
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(jwt)
            .body
    }
    fun currentDatetime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return "${currentDateTime.format(formatter)}Z"
    }
    fun expirtedDatetime(): String {
        val currentDateTime = LocalDateTime.now()
        val futureDateTime = currentDateTime.plusDays(7)
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return "${futureDateTime.format(formatter)}Z"
    }
}