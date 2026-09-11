package com.pingwin.vpn

import org.junit.Assert.assertTrue
import org.junit.Test

class SingBoxRoutingConfigBuilderTest {

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

        assertTrue(
            config.contains(
                """"final": "direct""""
            )
        )

        val dnsRuleIndex =
            config.indexOf(
                """"ip_cidr": ["1.1.1.1/32"]"""
            )

        val domainRuleIndex =
            config.indexOf(
                """"domain": ["""
            )

        assertTrue(
            dnsRuleIndex >= 0
        )
        assertTrue(
            domainRuleIndex >= 0
        )
        assertTrue(
            dnsRuleIndex < domainRuleIndex
        )

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
}
