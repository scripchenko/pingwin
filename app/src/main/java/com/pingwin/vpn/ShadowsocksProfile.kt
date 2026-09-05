package com.pingwin.vpn

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Base64

enum class ShadowsocksParseError {
    INVALID_SCHEME,
    INVALID_CREDENTIALS,
    MISSING_HOST,
    INVALID_PORT
}

class ShadowsocksParseException(
    val error: ShadowsocksParseError
) : IllegalArgumentException()

data class ShadowsocksProfile(
    val method: String,
    val password: String,
    override val host: String,
    override val port: Int,
    val plugin: String?,
    override val name: String?
) : ConnectionProfile {

    override val protocol =
        ConnectionProtocol.SHADOWSOCKS

    companion object {

        fun parse(
            link: String
        ): ShadowsocksProfile {
            val trimmed =
                link.trim()

            if (
                !trimmed.startsWith(
                    "ss://",
                    ignoreCase = true
                )
            ) {
                throw ShadowsocksParseException(
                    ShadowsocksParseError.INVALID_SCHEME
                )
            }

            val normalized =
                "ss://" +
                    trimmed.substringAfter("://")

            return if (
                normalized
                    .substringAfter("ss://")
                    .substringBefore("#")
                    .contains("@")
            ) {
                parseSip002(normalized)
            } else {
                parseLegacy(normalized)
            }
        }

        private fun parseSip002(
            link: String
        ): ShadowsocksProfile {
            val uri =
                URI(link)

            val rawUserInfo =
                uri.rawUserInfo
                    ?.takeIf(String::isNotBlank)
                    ?: throw ShadowsocksParseException(
                        ShadowsocksParseError.INVALID_CREDENTIALS
                    )

            val credentials =
                decodeCredentials(rawUserInfo)

            val host =
                uri.host
                    ?.takeIf(String::isNotBlank)
                    ?: throw ShadowsocksParseException(
                        ShadowsocksParseError.MISSING_HOST
                    )

            val port =
                uri.port
                    .takeIf { it in 1..65535 }
                    ?: throw ShadowsocksParseException(
                        ShadowsocksParseError.INVALID_PORT
                    )

            val params =
                parseQuery(
                    uri.rawQuery
                )

            return ShadowsocksProfile(
                method = credentials.first,
                password = credentials.second,
                host = host,
                port = port,
                plugin = params["plugin"],
                name =
                    uri.rawFragment
                        ?.takeIf(String::isNotBlank)
                        ?.let(::decode)
            )
        }

        private fun parseLegacy(
            link: String
        ): ShadowsocksProfile {
            val payload =
                link
                    .substringAfter("ss://")
                    .substringBefore("#")
                    .substringBefore("?")
                    .trimEnd('/')

            val decoded =
                decodeBase64(payload)
                    ?: throw ShadowsocksParseException(
                        ShadowsocksParseError.INVALID_CREDENTIALS
                    )

            val at =
                decoded.lastIndexOf("@")

            if (at <= 0) {
                throw ShadowsocksParseException(
                    ShadowsocksParseError.INVALID_CREDENTIALS
                )
            }

            val credentials =
                parsePlainCredentials(
                    decoded.substring(
                        0,
                        at
                    )
                )

            val server =
                decoded.substring(
                    at + 1
                )

            val colon =
                server.lastIndexOf(":")

            if (colon <= 0) {
                throw ShadowsocksParseException(
                    ShadowsocksParseError.INVALID_PORT
                )
            }

            val host =
                server.substring(
                    0,
                    colon
                )
                    .removePrefix("[")
                    .removeSuffix("]")
                    .takeIf(String::isNotBlank)
                    ?: throw ShadowsocksParseException(
                        ShadowsocksParseError.MISSING_HOST
                    )

            val port =
                server.substring(
                    colon + 1
                )
                    .toIntOrNull()
                    ?.takeIf { it in 1..65535 }
                    ?: throw ShadowsocksParseException(
                        ShadowsocksParseError.INVALID_PORT
                    )

            val rawFragment =
                link.substringAfter(
                    "#",
                    ""
                )

            return ShadowsocksProfile(
                method = credentials.first,
                password = credentials.second,
                host = host,
                port = port,
                plugin = null,
                name =
                    rawFragment
                        .takeIf(String::isNotBlank)
                        ?.let(::decode)
            )
        }

        private fun decodeCredentials(
            value: String
        ): Pair<String, String> {
            val plain =
                if (":" in value) {
                    decode(value)
                } else {
                    decodeBase64(value)
                        ?: throw ShadowsocksParseException(
                            ShadowsocksParseError.INVALID_CREDENTIALS
                        )
                }

            return parsePlainCredentials(
                plain
            )
        }

        private fun parsePlainCredentials(
            value: String
        ): Pair<String, String> {
            val colon =
                value.indexOf(":")

            if (
                colon <= 0 ||
                colon == value.lastIndex
            ) {
                throw ShadowsocksParseException(
                    ShadowsocksParseError.INVALID_CREDENTIALS
                )
            }

            return value.substring(
                0,
                colon
            ) to value.substring(
                colon + 1
            )
        }

        private fun decodeBase64(
            value: String
        ): String? =
            runCatching {
                val padded =
                    value +
                        "=".repeat(
                            (4 - value.length % 4) % 4
                        )

                String(
                    Base64
                        .getUrlDecoder()
                        .decode(padded),
                    StandardCharsets.UTF_8
                )
            }.recoverCatching {
                val padded =
                    value +
                        "=".repeat(
                            (4 - value.length % 4) % 4
                        )

                String(
                    Base64
                        .getDecoder()
                        .decode(padded),
                    StandardCharsets.UTF_8
                )
            }.getOrNull()

        private fun parseQuery(
            query: String?
        ): Map<String, String> {
            if (query.isNullOrBlank()) {
                return emptyMap()
            }

            return query
                .split("&")
                .mapNotNull { parameter ->
                    if (parameter.isBlank()) {
                        null
                    } else {
                        val parts =
                            parameter.split(
                                "=",
                                limit = 2
                            )

                        decode(parts[0]) to
                            if (parts.size == 2) {
                                decode(parts[1])
                            } else {
                                ""
                            }
                    }
                }
                .toMap()
        }

        private fun decode(
            value: String
        ): String =
            URLDecoder.decode(
                value.replace(
                    "+",
                    "%2B"
                ),
                StandardCharsets.UTF_8.name()
            )
    }
}
