package com.pingwin.vpn

import android.content.Context

object RoutingSettingsStore {

    private const val PREFS_NAME = "routing_settings"

    private const val KEY_SITE_ENABLED = "site_enabled"
    private const val KEY_SITE_MODE = "site_mode"
    private const val KEY_DOMAINS = "domains"

    private const val KEY_APP_ENABLED = "app_enabled"
    private const val KEY_APP_MODE = "app_mode"
    private const val KEY_PACKAGES = "packages"

    // Keys from the intermediate routing implementation.
    private const val KEY_SITE_RULE_PACKAGES = "site_rule_packages"
    private const val KEY_DIRECT_PACKAGES = "direct_packages"

    fun load(context: Context): RoutingSettings {
        val prefs =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        return RoutingSettings(
            siteEnabled =
                prefs.getBoolean(
                    KEY_SITE_ENABLED,
                    false
                ),
            siteMode =
                enumValueOrDefault(
                    prefs.getString(
                        KEY_SITE_MODE,
                        null
                    ),
                    RoutingMode.ONLY_SELECTED_VIA_VPN
                ),
            domains =
                prefs.getStringSet(
                    KEY_DOMAINS,
                    emptySet()
                )?.toSet()
                    ?: emptySet(),

            appEnabled =
                prefs.getBoolean(
                    KEY_APP_ENABLED,
                    false
                ),
            appMode =
                enumValueOrDefault(
                    prefs.getString(
                        KEY_APP_MODE,
                        null
                    ),
                    RoutingMode.ONLY_SELECTED_VIA_VPN
                ),
            packages =
                prefs.getStringSet(
                    KEY_PACKAGES,
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
            .putBoolean(
                KEY_SITE_ENABLED,
                settings.siteEnabled
            )
            .putString(
                KEY_SITE_MODE,
                settings.siteMode.name
            )
            .putStringSet(
                KEY_DOMAINS,
                settings.domains.toSet()
            )
            .putBoolean(
                KEY_APP_ENABLED,
                settings.appEnabled
            )
            .putString(
                KEY_APP_MODE,
                settings.appMode.name
            )
            .putStringSet(
                KEY_PACKAGES,
                settings.packages.toSet()
            )
            .remove(KEY_SITE_RULE_PACKAGES)
            .remove(KEY_DIRECT_PACKAGES)
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
