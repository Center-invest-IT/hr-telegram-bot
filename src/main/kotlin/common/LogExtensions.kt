package dev.limebeck.openconf.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

val KClass<*>.logger: Logger
    get() = LoggerFactory.getLogger(this.java)

fun Logger.error(throwable: Throwable? = null, message: () -> String) {
    if (this.isErrorEnabled) {
        error(message(), throwable)
    }
}

fun Logger.info(message: () -> String) {
    if (this.isInfoEnabled) {
        info(message())
    }
}

fun Logger.debug(message: () -> String) {
    if (this.isDebugEnabled) {
        debug(message())
    }
}
