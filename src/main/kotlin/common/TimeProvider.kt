package dev.limebeck.openconf.common

import java.time.Instant

interface TimeProvider {
    fun getCurrent(): Instant
}

object RealTimeProvider : TimeProvider {
    override fun getCurrent(): Instant = Instant.now()
}

class MockTimeProvider(
    var currentTime: Instant
) : TimeProvider {
    override fun getCurrent(): Instant = currentTime
}