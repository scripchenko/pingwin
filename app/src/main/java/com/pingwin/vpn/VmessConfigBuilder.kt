package com.pingwin.vpn

enum class VmessConfigError {
    UNSUPPORTED_TLS,
    UNSUPPORTED_NETWORK
}

class VmessConfigException(
    val error: VmessConfigError
) : IllegalArgumentException()

object VmessConfigBuilder {

    fun build(
        profile: VmessProfile,
        routing: RoutingSettings = RoutingSettings(),
        detailedLogging: Boolean = false
    ): String {
        val tlsMode =
            profile.tls
                ?.trim()
                ?.lowercase()
                ?.takeIf(String::isNotEmpty)
                ?: "none"

        val security =
            when (tlsMode) {
                "none",
                "" ->
                    "none"

                "tls" ->
                    "tls"

                else ->
                    throw VmessConfigException(
                        VmessConfigError.UNSUPPORTED_TLS
                    )
            }

        val rawNetwork =
            profile.network
                ?.trim()
                ?.lowercase()
                ?.takeIf(String::isNotEmpty)
                ?: "tcp"

        val network =
            when (rawNetwork) {
                "tcp" -> "tcp"
                "ws" -> "ws"
                "grpc" -> "grpc"
                "http",
                "h2" -> "http"
                "httpupgrade" -> "httpupgrade"

                else ->
                    throw VmessConfigException(
                        VmessConfigError.UNSUPPORTED_NETWORK
                    )
            }

        val optionalFields =
            buildList {
                if (profile.alterId != 0) {
                    add(
                        """"alter_id": ${profile.alterId}"""
                    )
                }

                profile.security
                    .trim()
                    .takeIf(String::isNotEmpty)
                    ?.let {
                        add(
                            """"security": "${jsonEscape(it)}""""
                        )
                    }

                SingBoxTlsBuilder.build(
                    security = security,
                    host = profile.host,
                    serverName = profile.serverName,
                    alpn = profile.alpn,
                    fingerprint = profile.fingerprint
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
              "type": "vmess",
              "tag": "proxy",
              "server": "${jsonEscape(profile.host)}",
              "server_port": ${profile.port},
              "uuid": "${jsonEscape(profile.uuid)}"$optionalSuffix
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
