package com.pingwin.vpn

enum class AutomationVpnAction {
    NONE,
    CONNECT,
    DISCONNECT
}

object AutomationDecisionPolicy {
    fun isTrustedWifi(
        ssid: String,
        trustedWifiSsids: Set<String>
    ): Boolean =
        ssid in trustedWifiSsids

    fun wifiAction(
        settings: AutomationSettings,
        ssid: String
    ): AutomationVpnAction {
        val trusted =
            isTrustedWifi(
                ssid,
                settings.trustedWifiSsids
            )

        return when {
            trusted &&
                settings.disconnectOnTrustedWifi ->
                AutomationVpnAction.DISCONNECT

            !trusted &&
                settings.connectOnUntrustedWifi ->
                AutomationVpnAction.CONNECT

            else ->
                AutomationVpnAction.NONE
        }
    }

    fun mobileAction(
        settings: AutomationSettings
    ): AutomationVpnAction =
        if (settings.connectOnMobile) {
            AutomationVpnAction.CONNECT
        } else {
            AutomationVpnAction.NONE
        }
}