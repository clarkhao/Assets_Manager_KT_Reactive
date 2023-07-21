package com.clark.totoro.assets.model

data class Token(val token: String, val user: User, val publicToken: String, val locale: String)
data class Limit(val limit: Int, val uploaded: Int)