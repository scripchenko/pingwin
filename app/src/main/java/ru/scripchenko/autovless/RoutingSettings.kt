package ru.scripchenko.autovless

enum class SiteRoutingMode {
    ALL_VIA_VPN,
    ONLY_SELECTED_VIA_VPN,
    EXCLUDE_SELECTED_FROM_VPN
}

/*
 * Legacy application-routing model.
 * Kept temporarily while the UI and storage are migrated
 * to per-application routing.
 */
enum class AppRoutingMode {
    ALL_VIA_VPN,
    ONLY_SELECTED_VIA_VPN,
    EXCLUDE_SELECTED_FROM_VPN
}

enum class AppRoute {
    VPN,
    SITE_RULES,
    DIRECT
}

data class RoutingSettings(
    val siteMode: SiteRoutingMode = SiteRoutingMode.ALL_VIA_VPN,
    val domains: Set<String> = emptySet(),

    // Legacy fields. They will be removed after migration.
    val appMode: AppRoutingMode = AppRoutingMode.ONLY_SELECTED_VIA_VPN,
    val packages: Set<String> = emptySet(),

    /*
     * New model:
     * - packages absent from both sets -> VPN
     * - packages in siteRulePackages -> SITE_RULES
     * - packages in directPackages -> DIRECT
     */
    val siteRulePackages: Set<String> = emptySet(),
    val directPackages: Set<String> = emptySet()
) {
    fun routeFor(packageName: String): AppRoute =
        when (packageName) {
            in directPackages -> AppRoute.DIRECT
            in siteRulePackages -> AppRoute.SITE_RULES
            else -> AppRoute.VPN
        }
}
