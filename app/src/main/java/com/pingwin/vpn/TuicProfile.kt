package com.pingwin.vpn

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

enum class TuicParseError {
    INVALID_SCHEME,
    MISSING_UUID,
    MISSING_HOST
}

class TuicParseException(
    val error: TuicParseError
) : IllegalArgumentException()

data class TuicProfile(
    val uuid: String,
    val password: String?,
    override val host: String,
    override val port: Int,
    val congestionControl: String?,
    val udpRelayMode: String?,
    val serverName: String?,
    val alpn: String?,
    val insecure: Boolean,
    val zeroRttHandshake: Boolean,
    val heartbeat: String?,
    override val name: String?
) : ConnectionProfile {

    override val protocol =
        ConnectionProtocol.TUIC

    companion object {

        fun parse(
            link: String
        ): TuicProfile {
            val trimmed =
                link.trim()

            if (
                !trimmed.startsWith(
                    "tuic://",
                    ignoreCase = true
                )
            ) {
                throw TuicParseException(
                    TuicParseError.INVALID_SCHEME
                )
            }

            val normalized =
                "tuic://" +
                    trimmed.substringAfter("://")

            val uri =
                URI(normalized)

            val rawUserInfo =
                uri.rawUserInfo
                    ?.takeIf(String::isNotBlank)
                    ?: throw TuicParseException(
                        TuicParseError.MISSING_UUID
                    )

            val credentials =
                rawUserInfo.split(
                    ":",
                    limit = 2
                )

            val uuid =
                decode(credentials[0])
                    .takeIf(String::isNotBlank)
                    ?: throw TuicParseException(
                        TuicParseError.MISSING_UUID
                    )

            val password =
                credentials
                    .getOrNull(1)
                    ?.let(::decode)
                    ?.takeIf(String::isNotEmpty)

            val host =
                uri.host
                    ?.takeIf(String::isNotBlank)
                    ?: throw TuicParseException(
                        TuicParseError.MISSING_HOST
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

            return TuicProfile(
                uuid = uuid,
                password = password,
                host = host,
                port = port,
                congestionControl =
                    params["congestion_control"],
                udpRelayMode =
                    params["udp_relay_mode"],
                serverName =
                    params["sni"],
                alpn =
                    params["alpn"],
                insecure =
                    booleanParam(
                        params["allow_insecure"]
                            ?: params["insecure"]
                    ),
                zeroRttHandshake =
                    booleanParam(
                        params["zero_rtt_handshake"]
                    ),
                heartbeat =
                    params["heartbeat"],
                name =
                    uri.rawFragment
                        ?.takeIf(String::isNotBlank)
                        ?.let(::decode)
            )
        }

        private fun booleanParam(
            value: String?
        ): Boolean =
            when (
                value
                    ?.trim()
                    ?.lowercase()
            ) {
                "1",
                "true" -> true

                else -> false
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
