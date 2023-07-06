package com.clark.totoro.assets.model

data class User(
    val owner: String, val name: String, val createdTime: String, val avatar: String,
    val email: String, val app: String, val limit: Int, val publicUser: String, val role: List<String>
)

data class PublicUser(val name: String, val avatar: String)
data class UserAuthor(val id: String, val limit: Int, val role: List<String>)