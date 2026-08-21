package com.pingwin.vpn

data class AutomationSettings(
    val enabled: Boolean = false,
    val trustedWifiSsids: Set<String> = emptySet(),
    val connectOnUntrustedWifi: Boolean = true,
    val connectOnMobile: Boolean = true,
    val disconnectOnTrustedWifi: Boolean = true,
    val serverId: String? = null
)
