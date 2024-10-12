package dev.limebeck.openconf.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Instant.format(formatter: DateTimeFormatter): String = formatter.format(this)

val MSK_ZONE = ZoneId.of("Europe/Moscow")

val DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(MSK_ZONE)