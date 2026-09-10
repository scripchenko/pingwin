package com.pingwin.vpn

object PortValidator {

    const val MIN_PORT =
        1

    const val MAX_PORT =
        65535

    fun isValid(
        port: Int
    ): Boolean =
        port in MIN_PORT..MAX_PORT

    fun parse(
        value: String
    ): Int? =
        value
            .toIntOrNull()
            ?.takeIf(
                ::isValid
            )
}
