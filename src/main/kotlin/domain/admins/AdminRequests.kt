package dev.limebeck.openconf.domain.admins

import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateRequest(
    val login: String,
    val password: String
)

@Serializable
data class AdminLoginRequest(
    val login: String,
    val password: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val details: String? = null
)