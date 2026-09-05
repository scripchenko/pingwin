package com.pingwin.vpn

enum class TuicConfigError {
    UNSUPPORTED_CONGESTION_CONTROL,
    UNSUPPORTED_UDP_RELAY_MODE
}

class TuicConfigException(
    val error: TuicConfigError
) : IllegalArgumentException()

object TuicConfigBuilder {

    fun build(
        profile: TuicProfile,
        routing: RoutingSettings = RoutingSettings(),
        detailedLogging: Boolean = false
    ): String {
        val congestionControl =
            profile.congestionControl
                ?.trim()
                ?.lowercase()
                ?.takeIf(String::isNotEmpty)

        if (
            congestionControl != null &&
            congestionControl !in
                setOf(
                    "cubic",
                    "new_reno",
                    "bbr"
                )
        ) {
            throw TuicConfigException(
                TuicConfigError.UNSUPPORTED_CONGESTION_CONTROL
            )
        }

        val udpRelayMode =
            profile.udpRelayMode
                ?.trim()
                ?.lowercase()
                ?.takeIf(String::isNotEmpty)

        if (
            udpRelayMode != null &&
            udpRelayMode !in
                setOf(
                    "native",
                    "quic"
                )
        ) {
            throw TuicConfigException(
                TuicConfigError.UNSUPPORTED_UDP_RELAY_MODE
            )
        }

        val fields =
            mutableListOf<String>()

        fields +=
            """"type": "tuic""""

        fields +=
            """"tag": "proxy""""

        fields +=
            """"server": "${jsonEscape(profile.host)}""""

        fields +=
            """"server_port": ${profile.port}"""

        fields +=
            """"uuid": "${jsonEscape(profile.uuid)}""""

        profile.password
            ?.takeIf(String::isNotBlank)
            ?.let {
                fields +=
                    """"password": "${jsonEscape(it)}""""
            }

        congestionControl
            ?.let {
                fields +=
                    """"congestion_control": "${jsonEscape(it)}""""
            }

        udpRelayMode
            ?.let {
                fields +=
                    """"udp_relay_mode": "${jsonEscape(it)}""""
            }

        if (profile.zeroRttHandshake) {
            fields +=
                """"zero_rtt_handshake": true"""
        }

        profile.heartbeat
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let {
                fields +=
                    """"heartbeat": "${jsonEscape(it)}""""
            }

        fields +=
            SingBoxTlsBuilder.build(
                security = "tls",
                host = profile.host,
                serverName = profile.serverName,
                alpn = profile.alpn,
                fingerprint = null,
                insecure = profile.insecure
            )!!

        val body =
            fields.joinToString(",\n") {
                indentBlock(
                    it,
                    2
                )
            }

        val proxyOutbound =
            """
            {
$body
            }
            """.trimIndent()

        return SingBoxBaseConfigBuilder.build(
            proxyOutbound = proxyOutbound,
            routing = routing,
            detailedLogging = detailedLogging
        )
    }

    private fun indentBlock(
        value: String,
        spaces: Int
    ): String {
        val indent =
            " ".repeat(spaces)

        return value
            .lineSequence()
            .joinToString("\n") {
                indent + it
            }
    }

    private fun jsonEscape(
        value: String
    ): String =
        buildString {
            value.forEach { character ->
                when (character) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (character.code < 0x20) {
                            append(
                                "\\u%04x".format(
                                    character.code
                                )
                            )
                        } else {
                            append(character)
                        }
                    }
                }
            }
        }
}
