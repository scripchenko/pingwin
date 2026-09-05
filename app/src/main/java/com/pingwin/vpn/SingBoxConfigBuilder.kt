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

        val logLevel =
            if (detailedLogging) {
                "debug"
            } else {
                "info"
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
                .joinToString(",`n") {
                    indentBlock(
                        it,
                        18
                    )
                }

        val optionalSuffix =
            if (optionalFields.isBlank()) {
                ""
            } else {
                ",`n$optionalFields"
            }

        return """
            {
              "log": {
                "level": "$logLevel",
                "timestamp": true
              },
              "dns": {
                "servers": [
                  {
                    "type": "local",
                    "tag": "local"
                  }
                ]
              },
              "inbounds": [
                {
                  "type": "tun",
                  "tag": "tun-in",
                  "address": [
                    "172.19.0.1/30"
                  ],
                  "auto_route": true,
                  "strict_route": true,
                  "stack": "gvisor"
                }
              ],
              "outbounds": [
                {
                  "type": "vless",
                  "tag": "proxy",
                  "server": "${jsonEscape(profile.host)}",
                  "server_port": ${profile.port},
                  "uuid": "${jsonEscape(profile.uuid)}"$optionalSuffix
                },
                {
                  "type": "direct",
                  "tag": "direct"
                }
              ],
              "route": ${buildRoute(routing)}
            }
        """.trimIndent()
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
            fields.joinToString(",`n") {
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
            fields.joinToString(",`n") {
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
    private fun buildRoute(
        routing: RoutingSettings
    ): String {
        if (!routing.enabled) {
            return """
                {
                  "auto_detect_interface": true,
                  "final": "proxy"
                }
            """.trimIndent()
        }

        val packages =
            routing.packages
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
                .sorted()
                .toList()

        val domains =
            routing.domains
                .asSequence()
                .map(::normalizeDomain)
                .filter(String::isNotEmpty)
                .distinct()
                .sorted()
                .toList()

        /*
         * If at least one enabled section is a whitelist
         * ("only selected through VPN"), unmatched traffic goes direct.
         *
         * Otherwise enabled sections are exclusion lists and unmatched
         * traffic goes through VPN.
         */
        val hasWhitelist =
            (
                routing.appEnabled &&
                    routing.appMode ==
                    RoutingMode.ONLY_SELECTED_VIA_VPN
            ) ||
                (
                    routing.siteEnabled &&
                        routing.siteMode ==
                        RoutingMode.ONLY_SELECTED_VIA_VPN
                )

        val finalOutbound =
            if (hasWhitelist) {
                "direct"
            } else {
                "proxy"
            }

        val rules =
            mutableListOf<String>()

        if (routing.siteEnabled) {
            rules +=
                """
                    {
                      "action": "sniff"
                    }
                """.trimIndent()
        }

        /*
         * Direct exclusions have priority over positive VPN matches.
         */
        if (
            routing.appEnabled &&
            routing.appMode ==
                RoutingMode.EXCLUDE_SELECTED_FROM_VPN &&
            packages.isNotEmpty()
        ) {
            rules +=
                packageRule(
                    packages = packages,
                    outbound = "direct"
                )
        }

        if (
            routing.siteEnabled &&
            routing.siteMode ==
                RoutingMode.EXCLUDE_SELECTED_FROM_VPN &&
            domains.isNotEmpty()
        ) {
            rules +=
                domainRule(
                    domains = domains,
                    outbound = "direct"
                )
        }

        if (
            routing.appEnabled &&
            routing.appMode ==
                RoutingMode.ONLY_SELECTED_VIA_VPN &&
            packages.isNotEmpty()
        ) {
            rules +=
                packageRule(
                    packages = packages,
                    outbound = "proxy"
                )
        }

        if (
            routing.siteEnabled &&
            routing.siteMode ==
                RoutingMode.ONLY_SELECTED_VIA_VPN &&
            domains.isNotEmpty()
        ) {
            rules +=
                domainRule(
                    domains = domains,
                    outbound = "proxy"
                )
        }

        if (rules.isEmpty()) {
            return """
                {
                  "auto_detect_interface": true,
                  "final": "$finalOutbound"
                }
            """.trimIndent()
        }

        val rulesJson =
            rules.joinToString(",\n") {
                indentBlock(
                    it,
                    8
                )
            }

        return """
            {
              "auto_detect_interface": true,
              "rules": [
$rulesJson
              ],
              "final": "$finalOutbound"
            }
        """.trimIndent()
    }

    private fun packageRule(
        packages: List<String>,
        outbound: String
    ): String {
        val packageJson =
            jsonArrayLines(
                packages,
                18
            )

        return """
            {
              "package_name": [
$packageJson
              ],
              "action": "route",
              "outbound": "$outbound"
            }
        """.trimIndent()
    }

    private fun domainRule(
        domains: List<String>,
        outbound: String
    ): String {
        val domainJson =
            jsonArrayLines(
                domains,
                18
            )

        return """
            {
              "domain": [
$domainJson
              ],
              "domain_suffix": [
$domainJson
              ],
              "action": "route",
              "outbound": "$outbound"
            }
        """.trimIndent()
    }

    private fun jsonArrayLines(
        values: List<String>,
        spaces: Int
    ): String {
        val indent =
            " ".repeat(spaces)

        return values.joinToString(",\n") {
            """$indent"${jsonEscape(it)}""""
        }
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

    private fun normalizeDomain(
        value: String
    ): String =
        value
            .trim()
            .lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore("#")
            .removePrefix("*.")
            .removePrefix(".")
            .removeSuffix(".")

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
