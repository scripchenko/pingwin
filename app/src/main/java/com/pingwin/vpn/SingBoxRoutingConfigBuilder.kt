package com.pingwin.vpn

object SingBoxRoutingConfigBuilder {

    fun build(
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
