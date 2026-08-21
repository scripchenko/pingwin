package com.pingwin.vpn

enum class RoutingMode {
    ONLY_SELECTED_VIA_VPN,
    EXCLUDE_SELECTED_FROM_VPN
}

data class RoutingSettings(
    val siteEnabled: Boolean = false,
    val siteMode: RoutingMode = RoutingMode.ONLY_SELECTED_VIA_VPN,
    val domains: Set<String> = emptySet(),

    val appEnabled: Boolean = false,
    val appMode: RoutingMode = RoutingMode.ONLY_SELECTED_VIA_VPN,
    val packages: Set<String> = emptySet()
) {
    val enabled: Boolean
        get() = siteEnabled || appEnabled
}
