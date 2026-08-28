package com.pingwin.vpn

import android.content.Context
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import java.util.Locale

object TelephonyCountryResolver {

    data class Countries(
        val homeCountryCode: String?,
        val networkCountryCode: String?
    )

    fun resolve(
        context: Context,
        settings: AutomationSettings
    ): Countries {
        val telephony =
            resolveTelephonyManager(
                context
            )

        val automaticHome =
            normalizeCountryCode(
                runCatching {
                    telephony.simCountryIso
                }.getOrNull()
            )

        val configuredHome =
            normalizeCountryCode(
                settings.homeCountryCode
            )

        val network =
            normalizeCountryCode(
                runCatching {
                    telephony.networkCountryIso
                }.getOrNull()
            )

        return Countries(
            homeCountryCode =
                configuredHome
                    ?: automaticHome,
            networkCountryCode =
                network
        )
    }

    private fun resolveTelephonyManager(
        context: Context
    ): TelephonyManager {
        val base =
            context.getSystemService(
                TelephonyManager::class.java
            )

        val subscriptionId =
            SubscriptionManager
                .getDefaultDataSubscriptionId()

        return if (
            SubscriptionManager
                .isValidSubscriptionId(
                    subscriptionId
                )
        ) {
            runCatching {
                base.createForSubscriptionId(
                    subscriptionId
                )
            }.getOrDefault(base)
        } else {
            base
        }
    }

    private fun normalizeCountryCode(
        value: String?
    ): String? {
        val normalized =
            value
                ?.trim()
                ?.uppercase(Locale.ROOT)

        return normalized
            ?.takeIf {
                it.length == 2 &&
                    it.all { character ->
                        character in 'A'..'Z'
                    }
            }
    }
}
