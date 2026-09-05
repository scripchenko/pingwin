package com.pingwin.vpn

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

enum class VlessParseError {
    INVALID_SCHEME,
    MISSING_UUID,
    MISSING_HOST
}

class VlessParseException(
    val error: VlessParseError
) : IllegalArgumentException()

data class VlessProfile(
    val uuid: String,
    override val host: String,
    override val port: Int,
    val encryption: String,
    val flow: String?,
    val fingerprint: String?,
    val publicKey: String?,
    val security: String?,
    val shortId: String?,
    val serverName: String?,
    val spiderX: String?,
    val network: String?,
    val transportHost: String?,
    val path: String?,
    val serviceName: String?,
    val mode: String?,
    val authority: String?,
    val alpn: String?,
    override val name: String?
) : ConnectionProfile {
    override val protocol = ConnectionProtocol.VLESS

    companion object {

        fun parse(link: String): VlessProfile {
            val trimmed = link.trim()

            if (!trimmed.startsWith("vless://", ignoreCase = true)) {
                throw VlessParseException(
                    VlessParseError.INVALID_SCHEME
                )
            }

            val uri = URI(trimmed)

            val uuid =
                uri.userInfo
                    ?: throw VlessParseException(
                        VlessParseError.MISSING_UUID
                    )

            val host =
                uri.host
                    ?: throw VlessParseException(
                        VlessParseError.MISSING_HOST
                    )

            val port =
                if (uri.port != -1) {
                    uri.port
                } else {
                    443
                }

            val params =
                parseQuery(uri.rawQuery)

            return VlessProfile(
                uuid = uuid,
                host = host,
                port = port,
                encryption = params["encryption"] ?: "none",
                flow = params["flow"],
                fingerprint = params["fp"],
                publicKey = params["pbk"],
                security = params["security"],
                shortId = params["sid"],
                serverName = params["sni"],
                spiderX = params["spx"],
                network = params["type"],
                transportHost = params["host"],
                path = params["path"],
                serviceName = params["serviceName"],
                mode = params["mode"],
                authority = params["authority"],
                alpn = params["alpn"],
                name = uri.rawFragment?.let(::decode)
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
                value,
                StandardCharsets.UTF_8.name()
            )
    }
}
