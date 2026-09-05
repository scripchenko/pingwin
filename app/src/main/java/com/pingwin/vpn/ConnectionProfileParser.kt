package com.pingwin.vpn

enum class ConnectionParseError {
    UNSUPPORTED_PROTOCOL
}

class ConnectionParseException(
    val error: ConnectionParseError
) : IllegalArgumentException()

object ConnectionProfileParser {

    fun parse(
        link: String
    ): ConnectionProfile {
        val trimmed =
            link.trim()

        return when {
            trimmed.startsWith(
                "vless://",
                ignoreCase = true
            ) ->
                VlessProfile.parse(
                    trimmed
                )

            else ->
                throw ConnectionParseException(
                    ConnectionParseError.UNSUPPORTED_PROTOCOL
                )
        }
    }
}
