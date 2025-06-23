package dev.limebeck.openconf.domain.bots.web

import kotlinx.serialization.Serializable

@Serializable
data class APIResponse<T>(
    val message: String,
    val data: T?
)