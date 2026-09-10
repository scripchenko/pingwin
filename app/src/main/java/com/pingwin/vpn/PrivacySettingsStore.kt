package com.pingwin.vpn

import android.content.Context

object PrivacySettingsStore {

    private const val PREFS_NAME =
        "pingwin_privacy"

    private const val KEY_EXTERNAL_GEOIP =
        "external_geoip"

    fun isExternalGeoIpEnabled(
        context: Context
    ): Boolean =
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .getBoolean(
                KEY_EXTERNAL_GEOIP,
                false
            )

    fun setExternalGeoIpEnabled(
        context: Context,
        enabled: Boolean
    ) {
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putBoolean(
                KEY_EXTERNAL_GEOIP,
                enabled
            )
            .apply()
    }
}
