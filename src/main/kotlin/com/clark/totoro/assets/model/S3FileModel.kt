package com.clark.totoro.assets.model

data class S3FileUrl(val name: String, val url: String)
data class S3File(val key: String, val date: String, val contentType: String)
data class Image(val name: String, val userId: String)
data class PresignedImage(
    val name: String,
    val createdTime: String,
    val updatedTime: String,
    val expiredTime: String,
    val url: String,
    val publicUser: String
)
data class Uploaded(val uploaded: Int, val limit: Int)
data class FileList(val files: List<String>)
