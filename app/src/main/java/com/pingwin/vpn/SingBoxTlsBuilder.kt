package com.pingwin.vpn

object SingBoxTlsBuilder {

    fun build(
        security: String,
        host: String,
        serverName: String?,
        alpn: String?,
        fingerprint: String?,
        publicKey: String? = null,
        shortId: String? = null,
        insecure: Boolean = false
    ): String? {
        if (security == "none") {
            return null
        }

        if (
            security == "reality" &&
            publicKey.isNullOrBlank()
        ) {
            throw SingBoxConfigException(
                SingBoxConfigError.MISSING_PUBLIC_KEY
            )
        }

        if (
            security == "reality" &&
            serverName.isNullOrBlank()
        ) {
            throw SingBoxConfigException(
                SingBoxConfigError.MISSING_SERVER_NAME
            )
        }

        val effectiveServerName =
            serverName
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: host

        val fields =
            mutableListOf<String>()

        fields +=
            """"enabled": true"""

        fields +=
            """"server_name": "${jsonEscape(effectiveServerName)}""""

        if (insecure) {
            fields +=
                """"insecure": true"""
        }

        val alpnValues =
            alpn
                ?.split(",")
                ?.map(String::trim)
                ?.filter(String::isNotEmpty)
                ?.distinct()
                .orEmpty()

        if (alpnValues.isNotEmpty()) {
            val values =
                alpnValues.joinToString(", ") {
                    """"${jsonEscape(it)}""""
                }

            fields +=
                """"alpn": [$values]"""
        }

        val effectiveFingerprint =
            fingerprint
                ?.trim()
                ?.takeIf(String::isNotEmpty)

        if (
            security == "reality" ||
            effectiveFingerprint != null
        ) {
            fields +=
                """
                "utls": {
                  "enabled": true,
                  "fingerprint": "${jsonEscape(effectiveFingerprint ?: "chrome")}"
                }
                """.trimIndent()
        }

        if (security == "reality") {
            fields +=
                """
                "reality": {
                  "enabled": true,
                  "public_key": "${jsonEscape(publicKey!!)}",
                  "short_id": "${jsonEscape(shortId ?: "")}"
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
