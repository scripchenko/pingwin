package com.pingwin.vpn

object SingBoxV2RayTransportBuilder {

    fun build(
        network: String,
        transportHost: String?,
        path: String?,
        serviceName: String?
    ): String? =
        when (network) {
            "tcp" ->
                null

            "ws" -> {
                val fields =
                    mutableListOf(
                        """"type": "ws""""
                    )

                path
                    ?.takeIf(String::isNotBlank)
                    ?.let {
                        fields +=
                            """"path": "${jsonEscape(it)}""""
                    }

                transportHost
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

                serviceName
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

                transportHost
                    ?.takeIf(String::isNotBlank)
                    ?.let {
                        fields +=
                            """"host": "${jsonEscape(it)}""""
                    }

                path
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

                transportHost
                    ?.takeIf(String::isNotBlank)
                    ?.let {
                        fields +=
                            """"host": ["${jsonEscape(it)}"]"""
                    }

                path
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
