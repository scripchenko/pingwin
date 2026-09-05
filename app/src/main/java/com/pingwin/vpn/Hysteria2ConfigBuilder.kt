package com.pingwin.vpn

enum class Hysteria2ConfigError {
    UNSUPPORTED_OBFS,
    MISSING_OBFS_PASSWORD,
    UNSUPPORTED_CERTIFICATE_PIN,
    UNSUPPORTED_ECH
}

class Hysteria2ConfigException(
    val error: Hysteria2ConfigError
) : IllegalArgumentException()

object Hysteria2ConfigBuilder {

    fun build(
        profile: Hysteria2Profile,
        routing: RoutingSettings = RoutingSettings(),
        detailedLogging: Boolean = false
    ): String {
        if (!profile.pinSha256.isNullOrBlank()) {
            throw Hysteria2ConfigException(
                Hysteria2ConfigError.UNSUPPORTED_CERTIFICATE_PIN
            )
        }

        if (!profile.ech.isNullOrBlank()) {
            throw Hysteria2ConfigException(
                Hysteria2ConfigError.UNSUPPORTED_ECH
            )
        }

        val obfsType =
            profile.obfsType
                ?.trim()
                ?.lowercase()
                ?.takeIf(String::isNotEmpty)

        if (
            obfsType != null &&
            obfsType != "salamander"
        ) {
            throw Hysteria2ConfigException(
                Hysteria2ConfigError.UNSUPPORTED_OBFS
            )
        }

        if (
            obfsType == "salamander" &&
            profile.obfsPassword.isNullOrBlank()
        ) {
            throw Hysteria2ConfigException(
                Hysteria2ConfigError.MISSING_OBFS_PASSWORD
            )
        }

        val fields =
            mutableListOf<String>()

        fields +=
            """"type": "hysteria2""""

        fields +=
            """"tag": "proxy""""

        fields +=
            """"server": "${jsonEscape(profile.host)}""""

        val useServerPorts =
            profile.serverPorts.size > 1 ||
                profile.serverPorts
                    .firstOrNull()
                    ?.contains(":") == true

        if (useServerPorts) {
            val ports =
                profile.serverPorts
                    .joinToString(", ") { value ->
                        val range =
                            if (":" in value) {
                                value
                            } else {
                                "$value:$value"
                            }

                        """"${jsonEscape(range)}""""
                    }

            fields +=
                """"server_ports": [$ports]"""
        } else {
            fields +=
                """"server_port": ${profile.port}"""
        }

        profile.password
            ?.takeIf(String::isNotBlank)
            ?.let {
                fields +=
                    """"password": "${jsonEscape(it)}""""
            }

        if (obfsType == "salamander") {
            fields +=
                """
                "obfs": {
                  "type": "salamander",
                  "password": "${jsonEscape(profile.obfsPassword!!)}"
                }
                """.trimIndent()
        }

        val serverName =
            profile.serverName
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: profile.host

        fields +=
            """
            "tls": {
              "enabled": true,
              "server_name": "${jsonEscape(serverName)}",
              "insecure": ${profile.insecure}
            }
            """.trimIndent()

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
