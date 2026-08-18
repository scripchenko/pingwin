package ru.scripchenko.autovless

import android.content.Context

object RoutingSettingsStore {

    private const val PREFS_NAME = "routing_settings"

    private const val KEY_SITE_MODE = "site_mode"
    private const val KEY_DOMAINS = "domains"

    // Legacy application-routing keys.
    private const val KEY_APP_MODE = "app_mode"
    private const val KEY_PACKAGES = "packages"

    // New per-application routing keys.
    private const val KEY_SITE_RULE_PACKAGES = "site_rule_packages"
    private const val KEY_DIRECT_PACKAGES = "direct_packages"

    fun load(context: Context): RoutingSettings {
        val prefs =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val siteMode =
            enumValueOrDefault(
                prefs.getString(KEY_SITE_MODE, null),
                SiteRoutingMode.ALL_VIA_VPN
            )

        val appMode =
            enumValueOrDefault(
                prefs.getString(KEY_APP_MODE, null),
                AppRoutingMode.ONLY_SELECTED_VIA_VPN
            )

        return RoutingSettings(
            siteMode = siteMode,
            domains =
                prefs.getStringSet(
                    KEY_DOMAINS,
                    emptySet()
                )?.toSet()
                    ?: emptySet(),

            appMode = appMode,
            packages =
                prefs.getStringSet(
                    KEY_PACKAGES,
                    emptySet()
                )?.toSet()
                    ?: emptySet(),

            siteRulePackages =
                prefs.getStringSet(
                    KEY_SITE_RULE_PACKAGES,
                    emptySet()
                )?.toSet()
                    ?: emptySet(),

            directPackages =
                prefs.getStringSet(
                    KEY_DIRECT_PACKAGES,
                    emptySet()
                )?.toSet()
                    ?: emptySet()
        )
    }

    fun save(
        context: Context,
        settings: RoutingSettings
    ) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                KEY_SITE_MODE,
                settings.siteMode.name
            )
            .putStringSet(
                KEY_DOMAINS,
                settings.domains.toSet()
            )

            // Legacy values are kept until the UI migration is complete.
            .putString(
                KEY_APP_MODE,
                settings.appMode.name
            )
            .putStringSet(
                KEY_PACKAGES,
                settings.packages.toSet()
            )

            .putStringSet(
                KEY_SITE_RULE_PACKAGES,
                settings.siteRulePackages.toSet()
            )
            .putStringSet(
                KEY_DIRECT_PACKAGES,
                settings.directPackages.toSet()
            )
            .apply()
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(
        value: String?,
        defaultValue: T
    ): T =
        value
            ?.let {
                runCatching {
                    enumValueOf<T>(it)
                }.getOrNull()
            }
            ?: defaultValue
}
