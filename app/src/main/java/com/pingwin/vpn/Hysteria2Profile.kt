package com.pingwin.vpn

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

enum class Hysteria2ParseError {
    INVALID_SCHEME,
    MISSING_HOST,
    INVALID_PORT
}

class Hysteria2ParseException(
    val error: Hysteria2ParseError
) : IllegalArgumentException()

data class Hysteria2Profile(
    override val host: String,
    override val port: Int,
    val serverPorts: List<String>,
    val password: String?,
    val obfsType: String?,
    val obfsPassword: String?,
    val serverName: String?,
    val insecure: Boolean,
    val pinSha256: String?,
    val ech: String?,
    override val name: String?
) : ConnectionProfile {

    override val protocol =
        ConnectionProtocol.HYSTERIA2

    companion object {

        fun parse(
            link: String
        ): Hysteria2Profile {
            val trimmed =
                link.trim()

            val schemeEnd =
                trimmed.indexOf("://")

            if (schemeEnd <= 0) {
                throw Hysteria2ParseException(
                    Hysteria2ParseError.INVALID_SCHEME
                )
            }

            val scheme =
                trimmed
                    .substring(0, schemeEnd)
                    .lowercase()

            if (
                scheme != "hysteria2" &&
                scheme != "hy2"
            ) {
                throw Hysteria2ParseException(
                    Hysteria2ParseError.INVALID_SCHEME
                )
            }

            val afterScheme =
                trimmed.substring(
                    schemeEnd + 3
                )

            val fragmentIndex =
                afterScheme.indexOf("#")

            val rawFragment =
                if (fragmentIndex >= 0) {
                    afterScheme.substring(
                        fragmentIndex + 1
                    )
                } else {
                    null
                }

            val withoutFragment =
                if (fragmentIndex >= 0) {
                    afterScheme.substring(
                        0,
                        fragmentIndex
                    )
                } else {
                    afterScheme
                }

            val queryIndex =
                withoutFragment.indexOf("?")

            val rawQuery =
                if (queryIndex >= 0) {
                    withoutFragment.substring(
                        queryIndex + 1
                    )
                } else {
                    null
                }

            val authorityAndPath =
                if (queryIndex >= 0) {
                    withoutFragment.substring(
                        0,
                        queryIndex
                    )
                } else {
                    withoutFragment
                }

            val authority =
                authorityAndPath
                    .substringBefore("/")
                    .trim()

            val atIndex =
                authority.lastIndexOf("@")

            val rawPassword =
                if (atIndex >= 0) {
                    authority.substring(
                        0,
                        atIndex
                    )
                } else {
                    null
                }

            val server =
                if (atIndex >= 0) {
                    authority.substring(
                        atIndex + 1
                    )
                } else {
                    authority
                }

            if (server.isBlank()) {
                throw Hysteria2ParseException(
                    Hysteria2ParseError.MISSING_HOST
                )
            }

            val hostAndPorts =
                parseHostAndPorts(server)

            val params =
                parseQuery(rawQuery)

            return Hysteria2Profile(
                host = hostAndPorts.first,
                port =
                    firstPort(
                        hostAndPorts.second
                    ),
                serverPorts =
                    hostAndPorts.second,
                password =
                    rawPassword
                        ?.takeIf(String::isNotBlank)
                        ?.let(::decode),
                obfsType =
                    params["obfs"],
                obfsPassword =
                    params["obfs-password"],
                serverName =
                    params["sni"],
                insecure =
                    when (
                        params["insecure"]
                            ?.lowercase()
                    ) {
                        "1",
                        "true" -> true

                        else -> false
                    },
                pinSha256 =
                    params["pinSHA256"],
                ech =
                    params["ech"],
                name =
                    rawFragment
                        ?.takeIf(String::isNotBlank)
                        ?.let(::decode)
            )
        }

        private fun parseHostAndPorts(
            value: String
        ): Pair<String, List<String>> {
            val host: String
            val rawPorts: String?

            if (value.startsWith("[")) {
                val closing =
                    value.indexOf("]")

                if (closing <= 1) {
                    throw Hysteria2ParseException(
                        Hysteria2ParseError.MISSING_HOST
                    )
                }

                host =
                    value.substring(
                        1,
                        closing
                    )

                val remainder =
                    value.substring(
                        closing + 1
                    )

                rawPorts =
                    if (remainder.startsWith(":")) {
                        remainder.substring(1)
                    } else {
                        null
                    }
            } else {
                val colon =
                    value.lastIndexOf(":")

                if (colon >= 0) {
                    host =
                        value.substring(
                            0,
                            colon
                        )

                    rawPorts =
                        value.substring(
                            colon + 1
                        )
                } else {
                    host = value
                    rawPorts = null
                }
            }

            if (host.isBlank()) {
                throw Hysteria2ParseException(
                    Hysteria2ParseError.MISSING_HOST
                )
            }

            val ports =
                if (rawPorts.isNullOrBlank()) {
                    listOf("443")
                } else {
                    rawPorts
                        .split(",")
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .map(::normalizePort)
                }

            if (ports.isEmpty()) {
                throw Hysteria2ParseException(
                    Hysteria2ParseError.INVALID_PORT
                )
            }

            return host to ports
        }

        private fun normalizePort(
            value: String
        ): String {
            val parts =
                value.split(
                    "-",
                    limit = 2
                )

            return if (parts.size == 1) {
                val port =
                    validatePort(parts[0])

                port.toString()
            } else {
                val start =
                    validatePort(parts[0])

                val end =
                    validatePort(parts[1])

                if (start > end) {
                    throw Hysteria2ParseException(
                        Hysteria2ParseError.INVALID_PORT
                    )
                }

                "$start:$end"
            }
        }

        private fun firstPort(
            ports: List<String>
        ): Int =
            ports
                .first()
                .substringBefore(":")
                .toInt()

        private fun validatePort(
            value: String
        ): Int {
            val port =
                value.toIntOrNull()
                    ?: throw Hysteria2ParseException(
                        Hysteria2ParseError.INVALID_PORT
                    )

            if (port !in 1..65535) {
                throw Hysteria2ParseException(
                    Hysteria2ParseError.INVALID_PORT
                )
            }

            return port
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
