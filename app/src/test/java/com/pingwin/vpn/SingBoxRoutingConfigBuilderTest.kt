package com.pingwin.vpn

import org.junit.Assert.assertTrue
import org.junit.Test

class SingBoxRoutingConfigBuilderTest {

    @Test
    fun disabledRoutingUsesProxyAsFinalOutbound() {
        val config =
            SingBoxRoutingConfigBuilder.build(
                RoutingSettings()
            )

        assertTrue(config.contains(""""final": "proxy""""))
    }

    @Test
    fun appAndSiteWhitelistsUseDirectFinalAndProxyEitherSelection() {
        val config =
            SingBoxRoutingConfigBuilder.build(
                RoutingSettings(
                    appEnabled = true,
                    appMode = RoutingMode.ONLY_SELECTED_VIA_VPN,
                    packages = setOf("com.example.app"),
                    siteEnabled = true,
                    siteMode = RoutingMode.ONLY_SELECTED_VIA_VPN,
                    domains = setOf("example.com")
                )
            )

        assertTrue(config.contains(""""final": "direct""""))
        assertTrue(
            ruleFor(config, """"package_name": [""")
                .contains(""""outbound": "proxy"""")
        )
        assertTrue(
            ruleFor(config, """"domain": [""")
                .contains(""""outbound": "proxy"""")
        )
    }

    @Test
    fun appAndSiteExclusionsUseProxyFinalAndDirectEitherSelection() {
        val config =
            SingBoxRoutingConfigBuilder.build(
                RoutingSettings(
                    appEnabled = true,
                    appMode = RoutingMode.EXCLUDE_SELECTED_FROM_VPN,
                    packages = setOf("com.example.app"),
                    siteEnabled = true,
                    siteMode = RoutingMode.EXCLUDE_SELECTED_FROM_VPN,
                    domains = setOf("example.com")
                )
            )

        assertTrue(config.contains(""""final": "proxy""""))
        assertTrue(
            ruleFor(config, """"package_name": [""")
                .contains(""""outbound": "direct"""")
        )
        assertTrue(
            ruleFor(config, """"domain": [""")
                .contains(""""outbound": "direct"""")
        )
    }

    @Test
    fun siteExclusionTakesPriorityOverAppWhitelist() {
        val config =
            SingBoxRoutingConfigBuilder.build(
                RoutingSettings(
                    appEnabled = true,
                    appMode = RoutingMode.ONLY_SELECTED_VIA_VPN,
                    packages = setOf("com.example.app"),
                    siteEnabled = true,
                    siteMode = RoutingMode.EXCLUDE_SELECTED_FROM_VPN,
                    domains = setOf("example.com")
                )
            )

        assertTrue(config.contains(""""final": "direct""""))

        val siteRuleIndex = config.indexOf(""""domain": [""")
        val appRuleIndex = config.indexOf(""""package_name": [""")

        assertTrue(siteRuleIndex >= 0)
        assertTrue(appRuleIndex >= 0)
        assertTrue(siteRuleIndex < appRuleIndex)

        assertTrue(
            ruleFor(config, """"domain": [""")
                .contains(""""outbound": "direct"""")
        )
        assertTrue(
            ruleFor(config, """"package_name": [""")
                .contains(""""outbound": "proxy"""")
        )
    }

    @Test
    fun appExclusionTakesPriorityOverSiteWhitelist() {
        val config =
            SingBoxRoutingConfigBuilder.build(
                RoutingSettings(
                    appEnabled = true,
                    appMode = RoutingMode.EXCLUDE_SELECTED_FROM_VPN,
                    packages = setOf("com.example.app"),
                    siteEnabled = true,
                    siteMode = RoutingMode.ONLY_SELECTED_VIA_VPN,
                    domains = setOf("example.com")
                )
            )

        assertTrue(config.contains(""""final": "direct""""))

        val appRuleIndex = config.indexOf(""""package_name": [""")
        val siteRuleIndex = config.indexOf(""""domain": [""")

        assertTrue(appRuleIndex >= 0)
        assertTrue(siteRuleIndex >= 0)
        assertTrue(appRuleIndex < siteRuleIndex)

        assertTrue(
            ruleFor(config, """"package_name": [""")
                .contains(""""outbound": "direct"""")
        )
        assertTrue(
            ruleFor(config, """"domain": [""")
                .contains(""""outbound": "proxy"""")
        )
    }

    @Test
    fun dnsStaysProxiedInSiteWhitelistMode() {
        val config =
            SingBoxRoutingConfigBuilder.build(
                RoutingSettings(
                    siteEnabled = true,
                    siteMode = RoutingMode.ONLY_SELECTED_VIA_VPN,
                    domains = setOf("example.com")
                )
            )

        assertTrue(config.contains(""""final": "direct""""))

        val dnsRuleIndex =
            config.indexOf(
                """"ip_cidr": ["1.1.1.1/32"]"""
            )

        val domainRuleIndex =
            config.indexOf(
                """"domain": ["""
            )

        assertTrue(dnsRuleIndex >= 0)
        assertTrue(domainRuleIndex >= 0)
        assertTrue(dnsRuleIndex < domainRuleIndex)

        val dnsRule =
            config.substring(
                dnsRuleIndex,
                domainRuleIndex
            )

        assertTrue(
            dnsRule.contains(
                """"outbound": "proxy""""
            )
        )
    }

    private fun ruleFor(
        config: String,
        marker: String
    ): String {
        val start = config.indexOf(marker)
        assertTrue(start >= 0)

        val nextRule = config.indexOf("},", start)
        val end =
            if (nextRule >= 0) {
                nextRule + 1
            } else {
                config.length
            }

        return config.substring(start, end)
    }
}