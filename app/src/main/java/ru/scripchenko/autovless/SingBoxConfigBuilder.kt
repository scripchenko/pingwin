package ru.scripchenko.autovless

object SingBoxConfigBuilder {

    fun build(
        profile: VlessProfile,
        routing: RoutingSettings = RoutingSettings()
    ): String {
        require(profile.security == "reality") {
            "Пока поддерживается только security=reality"
        }

        require(profile.network == "tcp") {
            "Пока поддерживается только type=tcp"
        }

        require(!profile.publicKey.isNullOrBlank()) {
            "Для Reality отсутствует public key (pbk)"
        }

        require(!profile.serverName.isNullOrBlank()) {
            "Для Reality отсутствует server name (sni)"
        }

        return """
            {
              "log": {
                "level": "info",
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
                  "uuid": "${jsonEscape(profile.uuid)}",
                  "flow": "${jsonEscape(profile.flow ?: "")}",
                  "tls": {
                    "enabled": true,
                    "server_name": "${jsonEscape(profile.serverName)}",
                    "utls": {
                      "enabled": true,
                      "fingerprint": "${jsonEscape(profile.fingerprint ?: "chrome")}"
                    },
                    "reality": {
                      "enabled": true,
                      "public_key": "${jsonEscape(profile.publicKey)}",
                      "short_id": "${jsonEscape(profile.shortId ?: "")}"
                    }
                  }
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

    private fun buildRoute(
        routing: RoutingSettings
    ): String {
        val domains =
            routing.domains
                .asSequence()
                .map(::normalizeDomain)
                .filter(String::isNotEmpty)
                .distinct()
                .sorted()
                .toList()

        val siteRulePackages =
            routing.siteRulePackages
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
                .sorted()
                .toList()

        /*
         * During migration, preserve the previous global site-routing
         * behavior when no application is explicitly assigned SITE_RULES.
         */
        if (siteRulePackages.isEmpty()) {
            return buildLegacyGlobalSiteRoute(
                routing.siteMode,
                domains
            )
        }

        /*
         * All applications that reach the Android VPN are proxied by default.
         * DIRECT applications have already been excluded from the TUN by
         * AppRoutingConfigurator.
         *
         * Only SITE_RULES applications are allowed to use the direct outbound
         * according to domain rules.
         */
        if (routing.siteMode == SiteRoutingMode.ALL_VIA_VPN) {
            return """
                {
                  "auto_detect_interface": true,
                  "final": "proxy"
                }
            """.trimIndent()
        }

        val packageJson =
            jsonArrayLines(
                siteRulePackages,
                18
            )

        val selectedOutbound: String
        val fallbackOutbound: String

        when (routing.siteMode) {
            SiteRoutingMode.ONLY_SELECTED_VIA_VPN -> {
                selectedOutbound = "proxy"
                fallbackOutbound = "direct"
            }

            SiteRoutingMode.EXCLUDE_SELECTED_FROM_VPN -> {
                selectedOutbound = "direct"
                fallbackOutbound = "proxy"
            }

            SiteRoutingMode.ALL_VIA_VPN ->
                error("Unexpected site routing mode")
        }

        val rules =
            mutableListOf<String>()

        rules +=
            """
                {
                  "action": "sniff"
                }
            """.trimIndent()

        if (domains.isNotEmpty()) {
            val domainJson =
                jsonArrayLines(
                    domains,
                    18
                )

            rules +=
                """
                    {
                      "package_name": [
$packageJson
                      ],
                      "domain": [
$domainJson
                      ],
                      "domain_suffix": [
$domainJson
                      ],
                      "action": "route",
                      "outbound": "$selectedOutbound"
                    }
                """.trimIndent()
        }

        rules +=
            """
                {
                  "package_name": [
$packageJson
                  ],
                  "action": "route",
                  "outbound": "$fallbackOutbound"
                }
            """.trimIndent()

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
              "final": "proxy"
            }
        """.trimIndent()
    }

    private fun buildLegacyGlobalSiteRoute(
        siteMode: SiteRoutingMode,
        domains: List<String>
    ): String {
        val finalOutbound =
            when (siteMode) {
                SiteRoutingMode.ALL_VIA_VPN -> "proxy"
                SiteRoutingMode.ONLY_SELECTED_VIA_VPN -> "direct"
                SiteRoutingMode.EXCLUDE_SELECTED_FROM_VPN -> "proxy"
            }

        if (
            siteMode == SiteRoutingMode.ALL_VIA_VPN ||
            domains.isEmpty()
        ) {
            return """
                {
                  "auto_detect_interface": true,
                  "final": "$finalOutbound"
                }
            """.trimIndent()
        }

        val selectedOutbound =
            when (siteMode) {
                SiteRoutingMode.ONLY_SELECTED_VIA_VPN -> "proxy"
                SiteRoutingMode.EXCLUDE_SELECTED_FROM_VPN -> "direct"
                SiteRoutingMode.ALL_VIA_VPN ->
                    error("Unexpected site routing mode")
            }

        val domainJson =
            jsonArrayLines(
                domains,
                18
            )

        return """
            {
              "auto_detect_interface": true,
              "rules": [
                {
                  "action": "sniff"
                },
                {
                  "domain": [
$domainJson
                  ],
                  "domain_suffix": [
$domainJson
                  ],
                  "action": "route",
                  "outbound": "$selectedOutbound"
                }
              ],
              "final": "$finalOutbound"
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
