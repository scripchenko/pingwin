package com.pingwin.vpn

enum class ShadowsocksConfigError {
    UNSUPPORTED_PLUGIN
}

class ShadowsocksConfigException(
    val error: ShadowsocksConfigError
) : IllegalArgumentException()

object ShadowsocksConfigBuilder {

    fun build(
        profile: ShadowsocksProfile,
        routing: RoutingSettings = RoutingSettings(),
        detailedLogging: Boolean = false
    ): String {
        if (!profile.plugin.isNullOrBlank()) {
            throw ShadowsocksConfigException(
                ShadowsocksConfigError.UNSUPPORTED_PLUGIN
            )
        }

        val proxyOutbound =
            """
            {
              "type": "shadowsocks",
              "tag": "proxy",
              "server": "${jsonEscape(profile.host)}",
              "server_port": ${profile.port},
              "method": "${jsonEscape(profile.method)}",
              "password": "${jsonEscape(profile.password)}"
            }
            """.trimIndent()

        return SingBoxBaseConfigBuilder.build(
            proxyOutbound = proxyOutbound,
            routing = routing,
            detailedLogging = detailedLogging
        )
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
