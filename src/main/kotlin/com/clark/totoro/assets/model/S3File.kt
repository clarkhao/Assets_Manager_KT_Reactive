package com.clark.totoro.assets.model

data class S3File(val key: String, val date: String, val contentType: String)

data class S3FileUrl(val url: String)

data class FileMerge(val url: String, val key: String, val date: String)
data class ErrorMessage(val code: Int, val message: String)