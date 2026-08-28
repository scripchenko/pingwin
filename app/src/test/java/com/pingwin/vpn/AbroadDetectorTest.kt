package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Test

class AbroadDetectorTest {

    @Test
    fun sameCountryIsHome() {
        assertEquals(
            AbroadStatus.HOME,
            AbroadDetector.determine(
                "RU",
                "RU"
            )
        )
    }

    @Test
    fun differentCountryIsAbroad() {
        assertEquals(
            AbroadStatus.ABROAD,
            AbroadDetector.determine(
                "RU",
                "DE"
            )
        )
    }

    @Test
    fun countryCodesAreNormalized() {
        assertEquals(
            AbroadStatus.HOME,
            AbroadDetector.determine(
                " ru ",
                "Ru"
            )
        )
    }

    @Test
    fun missingHomeCountryIsUnknown() {
        assertEquals(
            AbroadStatus.UNKNOWN,
            AbroadDetector.determine(
                null,
                "DE"
            )
        )
    }

    @Test
    fun missingNetworkCountryIsUnknown() {
        assertEquals(
            AbroadStatus.UNKNOWN,
            AbroadDetector.determine(
                "RU",
                ""
            )
        )
    }

    @Test
    fun invalidCountryCodeIsUnknown() {
        assertEquals(
            AbroadStatus.UNKNOWN,
            AbroadDetector.determine(
                "RUS",
                "DE"
            )
        )
    }
}
