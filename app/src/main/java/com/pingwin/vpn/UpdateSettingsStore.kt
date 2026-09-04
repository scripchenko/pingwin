package com.pingwin.vpn

import android.content.Context

object UpdateSettingsStore {

    private const val PREFS_NAME =
        "pingwin_updates"

    private const val KEY_AUTO_CHECK =
        "auto_check"

    private const val KEY_LAST_NOTIFIED_VERSION =
        "last_notified_version"

    private const val KEY_AVAILABLE_VERSION =
        "available_version"

    fun isAutoCheckEnabled(
        context: Context
    ): Boolean =
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .getBoolean(
                KEY_AUTO_CHECK,
                false
            )

    fun setAutoCheckEnabled(
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
                KEY_AUTO_CHECK,
                enabled
            )
            .apply()
    }

    fun getLastNotifiedVersion(
        context: Context
    ): String? =
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .getString(
                KEY_LAST_NOTIFIED_VERSION,
                null
            )

    fun setLastNotifiedVersion(
        context: Context,
        version: String
    ) {
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_LAST_NOTIFIED_VERSION,
                version
            )
            .apply()
    }

    fun getAvailableVersion(
        context: Context
    ): String? =
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .getString(
                KEY_AVAILABLE_VERSION,
                null
            )

    fun setAvailableVersion(
        context: Context,
        version: String
    ) {
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_AVAILABLE_VERSION,
                version
            )
            .apply()
    }

    fun clearAvailableVersion(
        context: Context
    ) {
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(
                KEY_AVAILABLE_VERSION
            )
            .apply()
    }
}
