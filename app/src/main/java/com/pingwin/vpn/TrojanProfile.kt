package com.pingwin.vpn

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

enum class TrojanParseError {
    INVALID_SCHEME,
    MISSING_PASSWORD,
    MISSING_HOST
}

class TrojanParseException(
    val error: TrojanParseError
) : IllegalArgumentException()

data class TrojanProfile(
    val password: String,
    override val host: String,
    override val port: Int,
    val security: String?,
    val fingerprint: String?,
    val publicKey: String?,
    val shortId: String?,
    val serverName: String?,
    val network: String?,
    val transportHost: String?,
    val path: String?,
    val serviceName: String?,
    val authority: String?,
    val alpn: String?,
    val insecure: Boolean,
    override val name: String?
) : ConnectionProfile {

    override val protocol =
        ConnectionProtocol.TROJAN

    companion object {

        fun parse(
            link: String
        ): TrojanProfile {
            val trimmed =
                link.trim()

            if (
                !trimmed.startsWith(
                    "trojan://",
                    ignoreCase = true
                )
            ) {
                throw TrojanParseException(
                    TrojanParseError.INVALID_SCHEME
                )
            }

            val normalized =
                "trojan://" +
                    trimmed.substringAfter("://")

            val uri =
                URI(normalized)

            val password =
                uri.rawUserInfo
                    ?.takeIf(String::isNotBlank)
                    ?.let(::decode)
                    ?: throw TrojanParseException(
                        TrojanParseError.MISSING_PASSWORD
                    )

            val host =
                uri.host
                    ?.takeIf(String::isNotBlank)
                    ?: throw TrojanParseException(
                        TrojanParseError.MISSING_HOST
                    )

            val port =
                if (uri.port != -1) {
                    uri.port
                } else {
                    443
                }

            val params =
                parseQuery(
                    uri.rawQuery
                )

            return TrojanProfile(
                password = password,
                host = host,
                port = port,
                security =
                    params["security"],
                fingerprint =
                    params["fp"],
                publicKey =
                    params["pbk"],
                shortId =
                    params["sid"],
                serverName =
                    params["sni"],
                network =
                    params["type"],
                transportHost =
                    params["host"],
                path =
                    params["path"],
                serviceName =
                    params["serviceName"],
                authority =
                    params["authority"],
                alpn =
                    params["alpn"],
                insecure =
                    when (
                        (
                            params["allowInsecure"]
                                ?: params["insecure"]
                        )
                            ?.lowercase()
                    ) {
                        "1",
                        "true" -> true

                        else -> false
                    },
                name =
                    uri.rawFragment
                        ?.takeIf(String::isNotBlank)
                        ?.let(::decode)
            )
        }

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

                        val key =
                            decode(parts[0])

                        val value =
                            if (parts.size == 2) {
                                decode(parts[1])
                            } else {
                                ""
                            }

                        key to value
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
