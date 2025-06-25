package dev.limebeck.openconf.domain.admins

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateRequest(
    val login: String,
    val password: String
)

@Serializable
data class AdminUpdateRequest(
    val id: String,
    val login: String,
    val password: String
)