package dev.limebeck.openconf.domain

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val uid: String
)