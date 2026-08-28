package com.pingwin.vpn

import java.util.Locale

enum class AbroadStatus {
    HOME,
    ABROAD,
    UNKNOWN
}

object AbroadDetector {

    fun determine(
        homeCountryCode: String?,
        networkCountryCode: String?
    ): AbroadStatus {
        val home =
            normalizeCountryCode(
                homeCountryCode
            )

        val network =
            normalizeCountryCode(
                networkCountryCode
            )

        if (
            home == null ||
            network == null
        ) {
            return AbroadStatus.UNKNOWN
        }

        return if (home == network) {
            AbroadStatus.HOME
        } else {
            AbroadStatus.ABROAD
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
