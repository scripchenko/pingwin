package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerLocationResolverTest {

    @Test
    fun normalizesCountryCodes() {
        assertEquals(
            "DE",
            ServerLocationResolver.normalizeCountryCode(" de ")
        )
    }

    @Test
    fun rejectsInvalidCountryCodes() {
        assertNull(
            ServerLocationResolver.normalizeCountryCode("DEU")
        )
        assertNull(
            ServerLocationResolver.normalizeCountryCode("D1")
        )
    }

    @Test
    fun choosesMajorityCountryCode() {
        assertEquals(
            "FR",
            ServerLocationResolver.chooseCountryCode(
                listOf("FR", "fr", "DE")
            )
        )
    }

    @Test
    fun acceptsSingleValidProviderResult() {
        assertEquals(
            "NL",
            ServerLocationResolver.chooseCountryCode(
                listOf("NL")
            )
        )
    }

    @Test
    fun rejectsConflictingProviderResultsWithoutMajority() {
        assertNull(
            ServerLocationResolver.chooseCountryCode(
                listOf("FR", "DE", "NL")
            )
        )
    }

    @Test
    fun ignoresInvalidProviderResultsWhenVoting() {
        assertEquals(
            "PL",
            ServerLocationResolver.chooseCountryCode(
                listOf("PL", "pl", "???")
            )
        )
    }
}