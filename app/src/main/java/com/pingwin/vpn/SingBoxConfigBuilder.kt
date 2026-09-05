package com.pingwin.vpn

enum class SingBoxConfigError {
    UNSUPPORTED_SECURITY,
    UNSUPPORTED_NETWORK,
    MISSING_PUBLIC_KEY,
    MISSING_SERVER_NAME
}

class SingBoxConfigException(
    val error: SingBoxConfigError
) : IllegalArgumentException()

object SingBoxConfigBuilder {

    fun build(
        profile: VlessProfile,
        routing: RoutingSettings = RoutingSettings(),
        detailedLogging: Boolean = false
    ): String {
        val security =
            profile.security
                ?.trim()
                ?.lowercase()
                ?.takeIf(String::isNotEmpty)
                ?: "none"

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

        if (
            security == "reality" &&
            profile.publicKey.isNullOrBlank()
        ) {
            throw SingBoxConfigException(
                SingBoxConfigError.MISSING_PUBLIC_KEY
            )
        }

        if (
            security == "reality" &&
            profile.serverName.isNullOrBlank()
        ) {
            throw SingBoxConfigException(
                SingBoxConfigError.MISSING_SERVER_NAME
            )
        }

        val optionalFields =
            buildList {
                profile.flow
                    ?.trim()
                    ?.takeIf(String::isNotEmpty)
                    ?.let { flow ->
                        add(
                            """"flow": "${jsonEscape(flow)}""""
                        )
                    }

                buildTls(
                    profile,
                    security
                )?.let(::add)

                buildTransport(
                    profile,
                    network
                )?.let(::add)
            }
                .joinToString(",\n") {
                    indentBlock(
                        it,
                        18
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
              "type": "vless",
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
    private fun buildTls(
        profile: VlessProfile,
        security: String
    ): String? {
        if (security == "none") {
            return null
        }

        val serverName =
            profile.serverName
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: profile.host

        val fields =
            mutableListOf<String>()

        fields +=
            """"enabled": true"""

        fields +=
            """"server_name": "${jsonEscape(serverName)}""""

        val alpn =
            profile.alpn
                ?.split(",")
                ?.map(String::trim)
                ?.filter(String::isNotEmpty)
                ?.distinct()
                .orEmpty()

        if (alpn.isNotEmpty()) {
            val values =
                alpn.joinToString(", ") {
                    """"${jsonEscape(it)}""""
                }

            fields +=
                """"alpn": [$values]"""
        }

        val fingerprint =
            profile.fingerprint
                ?.trim()
                ?.takeIf(String::isNotEmpty)

        if (
            security == "reality" ||
            fingerprint != null
        ) {
            fields +=
                """
                "utls": {
                  "enabled": true,
                  "fingerprint": "${jsonEscape(fingerprint ?: "chrome")}"
                }
                """.trimIndent()
        }

        if (security == "reality") {
            fields +=
                """
                "reality": {
                  "enabled": true,
                  "public_key": "${jsonEscape(profile.publicKey!!)}",
                  "short_id": "${jsonEscape(profile.shortId ?: "")}"
                }
                """.trimIndent()
        }

        val body =
            fields.joinToString(",\n") {
                indentBlock(
                    it,
                    2
                )
            }

        return """
            "tls": {
$body
            }
        """.trimIndent()
    }

    private fun buildTransport(
        profile: VlessProfile,
        network: String
    ): String? =
        when (network) {
            "tcp" ->
                null

            "ws" -> {
                val fields =
                    mutableListOf(
                        """"type": "ws""""
                    )

                profile.path
                    ?.takeIf(String::isNotBlank)
                    ?.let {
                        fields +=
                            """"path": "${jsonEscape(it)}""""
                    }

                profile.transportHost
                    ?.takeIf(String::isNotBlank)
                    ?.let {
                        fields +=
                            """
                            "headers": {
                              "Host": "${jsonEscape(it)}"
                            }
                            """.trimIndent()
                    }

                transportObject(fields)
            }

            "grpc" -> {
                val fields =
                    mutableListOf(
                        """"type": "grpc""""
                    )

                profile.serviceName
                    ?.takeIf(String::isNotBlank)
                    ?.let {
                        fields +=
                            """"service_name": "${jsonEscape(it)}""""
                    }

                transportObject(fields)
            }

            "httpupgrade" -> {
                val fields =
                    mutableListOf(
                        """"type": "httpupgrade""""
                    )

                profile.transportHost
                    ?.takeIf(String::isNotBlank)
                    ?.let {
                        fields +=
                            """"host": "${jsonEscape(it)}""""
                    }

                profile.path
                    ?.takeIf(String::isNotBlank)
                    ?.let {
                        fields +=
                            """"path": "${jsonEscape(it)}""""
                    }

                transportObject(fields)
            }

            "http" -> {
                val fields =
                    mutableListOf(
                        """"type": "http""""
                    )

                profile.transportHost
                    ?.takeIf(String::isNotBlank)
                    ?.let {
                        fields +=
                            """"host": ["${jsonEscape(it)}"]"""
                    }

                profile.path
                    ?.takeIf(String::isNotBlank)
                    ?.let {
                        fields +=
                            """"path": "${jsonEscape(it)}""""
                    }

                transportObject(fields)
            }

            else ->
                throw SingBoxConfigException(
                    SingBoxConfigError.UNSUPPORTED_NETWORK
                )
        }

    private fun transportObject(
        fields: List<String>
    ): String {
        val body =
            fields.joinToString(",\n") {
                indentBlock(
                    it,
                    2
                )
            }

        return """
            "transport": {
$body
            }
        """.trimIndent()
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
