package com.clark.totoro.assets.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String,
    val role: List<String>
)

data class PublicUser(
    val name: String,
    val avatar: String,
    val email: String,
    val limit: Int,
    val uploaded: Int,
    val role: List<String>
)
data class PublicUserWithId(
    val id: String,
    val name: String,
    val avatar: String,
    val email: String,
    val limit: Int,
    val uploaded: Int,
    val role: List<String>
)

data class UserAuthor(val id: String, val limit: Int, val role: List<String>)