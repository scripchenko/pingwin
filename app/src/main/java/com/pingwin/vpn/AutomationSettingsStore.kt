package com.pingwin.vpn

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AutomationSettingsStore {

    private const val PREFS_NAME =
        "pingwin_automation"

    private const val KEY_SETTINGS =
        "settings"

    fun load(
        context: Context
    ): AutomationSettings {
        val raw =
            prefs(context)
                .getString(
                    KEY_SETTINGS,
                    null
                )
                ?: return AutomationSettings()

        return runCatching {
            val json =
                JSONObject(raw)

            val wifiArray =
                json.optJSONArray(
                    "trustedWifiSsids"
                )
                    ?: JSONArray()

            val ssids =
                buildSet {
                    for (
                        i in 0 until
                            wifiArray.length()
                    ) {
                        val value =
                            wifiArray
                                .optString(i)
                                .trim()

                        if (
                            value.isNotEmpty()
                        ) {
                            add(value)
                        }
                    }
                }

            AutomationSettings(
                enabled =
                    json.optBoolean(
                        "enabled",
                        false
                    ),
                trustedWifiSsids =
                    ssids,
                connectOnUntrustedWifi =
                    json.optBoolean(
                        "connectOnUntrustedWifi",
                        true
                    ),
                connectOnMobile =
                    json.optBoolean(
                        "connectOnMobile",
                        true
                    ),
                disconnectOnTrustedWifi =
                    json.optBoolean(
                        "disconnectOnTrustedWifi",
                        true
                    ),
                serverId =
                    json.optString(
                        "serverId",
                        ""
                    ).takeIf {
                        it.isNotBlank()
                    }
            )
        }.getOrElse {
            AutomationSettings()
        }
    }

    fun save(
        context: Context,
        settings: AutomationSettings
    ) {
        val wifi =
            JSONArray().apply {
                settings
                    .trustedWifiSsids
                    .sorted()
                    .forEach {
                        put(it)
                    }
            }

        val json =
            JSONObject().apply {
                put(
                    "enabled",
                    settings.enabled
                )
                put(
                    "trustedWifiSsids",
                    wifi
                )
                put(
                    "connectOnUntrustedWifi",
                    settings.connectOnUntrustedWifi
                )
                put(
                    "connectOnMobile",
                    settings.connectOnMobile
                )
                put(
                    "disconnectOnTrustedWifi",
                    settings.disconnectOnTrustedWifi
                )

                if (
                    settings.serverId != null
                ) {
                    put(
                        "serverId",
                        settings.serverId
                    )
                }
            }

        prefs(context)
            .edit()
            .putString(
                KEY_SETTINGS,
                json.toString()
            )
            .apply()
    }

    private fun prefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
}
