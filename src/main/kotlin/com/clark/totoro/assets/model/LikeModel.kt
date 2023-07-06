package com.clark.totoro.assets.model

data class LikeModel(val uploaded: Int, val likes: Int, val liked: Int)
data class CreateLike(val key: String, val user: String)
data class LikeQuery(val id: String, val `in`: String, val out: String, val time: Created)
data class Created(val created: String)
data class LikeFullQuery(val liked: Int, val likes: List<LikeCount>)
data class LikeCount(val count: Int)