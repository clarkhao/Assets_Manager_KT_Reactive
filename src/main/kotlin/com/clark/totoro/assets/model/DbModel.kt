package com.clark.totoro.assets.model

data class DbInfoResponse(val time: String, val status: String, val result: Any)
data class ScopePayload(val ns: String, val db: String, val sc: String, val tk: String)