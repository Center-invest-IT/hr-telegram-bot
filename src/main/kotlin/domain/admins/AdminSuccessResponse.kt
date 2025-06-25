package dev.limebeck.openconf.domain.admins

import kotlinx.serialization.Serializable

@Serializable
data class AdminSuccessResponse(
    val id: String,
    val login: String,
)