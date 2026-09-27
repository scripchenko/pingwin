package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationDecisionPolicyTest {
    @Test
    fun trustedWifiMatchIsExactAndCaseSensitive() {
        val trusted =
            setOf("HomeWiFi")

        assertTrue(
            AutomationDecisionPolicy.isTrustedWifi(
                "HomeWiFi",
                trusted
            )
        )

        assertFalse(
            AutomationDecisionPolicy.isTrustedWifi(
                "homewifi",
                trusted
            )
        )
    }

    @Test
    fun trustedWifiCanRequestDisconnect() {
        val settings =
            AutomationSettings(
                trustedWifiSsids = setOf("HomeWiFi"),
                disconnectOnTrustedWifi = true
            )

        assertEquals(
            AutomationVpnAction.DISCONNECT,
            AutomationDecisionPolicy.wifiAction(
                settings,
                "HomeWiFi"
            )
        )
    }

    @Test
    fun trustedWifiDoesNothingWhenDisconnectRuleDisabled() {
        val settings =
            AutomationSettings(
                trustedWifiSsids = setOf("HomeWiFi"),
                disconnectOnTrustedWifi = false
            )

        assertEquals(
            AutomationVpnAction.NONE,
            AutomationDecisionPolicy.wifiAction(
                settings,
                "HomeWiFi"
            )
        )
    }

    @Test
    fun untrustedWifiCanRequestConnect() {
        val settings =
            AutomationSettings(
                trustedWifiSsids = setOf("HomeWiFi"),
                connectOnUntrustedWifi = true
            )

        assertEquals(
            AutomationVpnAction.CONNECT,
            AutomationDecisionPolicy.wifiAction(
                settings,
                "CafeWiFi"
            )
        )
    }

    @Test
    fun untrustedWifiDoesNothingWhenConnectRuleDisabled() {
        val settings =
            AutomationSettings(
                trustedWifiSsids = setOf("HomeWiFi"),
                connectOnUntrustedWifi = false
            )

        assertEquals(
            AutomationVpnAction.NONE,
            AutomationDecisionPolicy.wifiAction(
                settings,
                "CafeWiFi"
            )
        )
    }

    @Test
    fun repeatedEvaluationReturnsSameRequiredAction() {
        val settings =
            AutomationSettings(
                trustedWifiSsids = emptySet(),
                connectOnUntrustedWifi = true
            )

        val first =
            AutomationDecisionPolicy.wifiAction(
                settings,
                "CafeWiFi"
            )

        val second =
            AutomationDecisionPolicy.wifiAction(
                settings,
                "CafeWiFi"
            )

        assertEquals(
            AutomationVpnAction.CONNECT,
            first
        )
        assertEquals(first, second)
    }

    @Test
    fun mobileCanRequestConnect() {
        val settings =
            AutomationSettings(
                connectOnMobile = true
            )

        assertEquals(
            AutomationVpnAction.CONNECT,
            AutomationDecisionPolicy.mobileAction(
                settings
            )
        )
    }

    @Test
    fun mobileDoesNothingWhenRuleDisabled() {
        val settings =
            AutomationSettings(
                connectOnMobile = false
            )

        assertEquals(
            AutomationVpnAction.NONE,
            AutomationDecisionPolicy.mobileAction(
                settings
            )
        )
    }
}