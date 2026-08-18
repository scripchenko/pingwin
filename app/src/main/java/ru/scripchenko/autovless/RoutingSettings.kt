package ru.scripchenko.autovless

enum class SiteRoutingMode {
    ALL_VIA_VPN,
    ONLY_SELECTED_VIA_VPN,
    EXCLUDE_SELECTED_FROM_VPN
}

enum class AppRoutingMode {
    ALL_VIA_VPN,
    ONLY_SELECTED_VIA_VPN,
    EXCLUDE_SELECTED_FROM_VPN
}

data class RoutingSettings(
    val siteMode: SiteRoutingMode = SiteRoutingMode.ALL_VIA_VPN,
    val domains: Set<String> = emptySet(),
    val appMode: AppRoutingMode = AppRoutingMode.ALL_VIA_VPN,
    val packages: Set<String> = emptySet()
)
