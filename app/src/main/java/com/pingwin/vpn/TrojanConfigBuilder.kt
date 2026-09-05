package com.pingwin.vpn

object TrojanConfigBuilder {

    fun build(
        profile: TrojanProfile,
        routing: RoutingSettings = RoutingSettings(),
        detailedLogging: Boolean = false
    ): String {
        val security =
            profile.security
                ?.trim()
                ?.lowercase()
                ?.takeIf(String::isNotEmpty)
                ?: "tls"

        if (
            security !in
                setOf(
                    "none",
                    "tls",
                    "reality"
                )
        ) {
            throw SingBoxConfigException(
                SingBoxConfigError.UNSUPPORTED_SECURITY
            )
        }

        val network =
            profile.network
                ?.trim()
                ?.lowercase()
                ?.takeIf(String::isNotEmpty)
                ?: "tcp"

        if (
            network !in
                setOf(
                    "tcp",
                    "ws",
                    "grpc",
                    "httpupgrade",
                    "http"
                )
        ) {
            throw SingBoxConfigException(
                SingBoxConfigError.UNSUPPORTED_NETWORK
            )
        }

        val optionalFields =
            buildList {
                SingBoxTlsBuilder.build(
                    security = security,
                    host = profile.host,
                    serverName = profile.serverName,
                    alpn = profile.alpn,
                    fingerprint = profile.fingerprint,
                    publicKey = profile.publicKey,
                    shortId = profile.shortId,
                    insecure = profile.insecure
                )?.let(::add)

                SingBoxV2RayTransportBuilder.build(
                    network = network,
                    transportHost = profile.transportHost,
                    path = profile.path,
                    serviceName = profile.serviceName
                )?.let(::add)
            }
                .joinToString(",\n") {
                    indentBlock(
                        it,
                        2
                    )
                }

        val optionalSuffix =
            if (optionalFields.isBlank()) {
                ""
            } else {
                ",\n$optionalFields"
            }

        val proxyOutbound =
            """
            {
              "type": "trojan",
              "tag": "proxy",
              "server": "${jsonEscape(profile.host)}",
              "server_port": ${profile.port},
              "password": "${jsonEscape(profile.password)}"$optionalSuffix
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
