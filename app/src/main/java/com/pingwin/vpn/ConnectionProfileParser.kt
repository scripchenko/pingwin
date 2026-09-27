package com.pingwin.vpn

enum class ConnectionParseError {
    UNSUPPORTED_PROTOCOL,
    INPUT_TOO_LARGE,
    INVALID_CHARACTERS
}

class ConnectionParseException(
    val error: ConnectionParseError
) : IllegalArgumentException()

object ConnectionProfileParser {

    internal const val MAX_LINK_LENGTH = 65_536

    fun parse(
        link: String
    ): ConnectionProfile {
        val trimmed =
            link.trim()

        if (trimmed.length > MAX_LINK_LENGTH) {
            throw ConnectionParseException(
                ConnectionParseError.INPUT_TOO_LARGE
            )
        }

        if (
            trimmed.any { character ->
                character.code < 0x20 ||
                    character.code == 0x7F
            }
        ) {
            throw ConnectionParseException(
                ConnectionParseError.INVALID_CHARACTERS
            )
        }

        return when {
            trimmed.startsWith(
                "vless://",
                ignoreCase = true
            ) ->
                VlessProfile.parse(
                    trimmed
                )

            trimmed.startsWith(
                "hysteria2://",
                ignoreCase = true
            ) ||
                trimmed.startsWith(
                    "hy2://",
                    ignoreCase = true
                ) ->
                Hysteria2Profile.parse(
                    trimmed
                )

            trimmed.startsWith(
                "trojan://",
                ignoreCase = true
            ) ->
                TrojanProfile.parse(
                    trimmed
                )

            trimmed.startsWith(
                "tuic://",
                ignoreCase = true
            ) ->
                TuicProfile.parse(
                    trimmed
                )

            trimmed.startsWith(
                "ss://",
                ignoreCase = true
            ) ->
                ShadowsocksProfile.parse(
                    trimmed
                )

            trimmed.startsWith(
                "vmess://",
                ignoreCase = true
            ) ->
                VmessProfile.parse(
                    trimmed
                )

            else ->
                throw ConnectionParseException(
                    ConnectionParseError.UNSUPPORTED_PROTOCOL
                )
        }
    }
}