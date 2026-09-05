package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TuicProfileTest {

    @Test
    fun parsesTuicCredentialsAndTls() {
        val profile =
            TuicProfile.parse(
                "tuic://11111111-1111-4111-8111-111111111111:secret@example.com:443" +
                    "?sni=cdn.example.com" +
                    "&alpn=h3" +
                    "#Test%20TUIC"
            )

        assertEquals(
            "11111111-1111-4111-8111-111111111111",
            profile.uuid
        )
        assertEquals("secret", profile.password)
        assertEquals("example.com", profile.host)
        assertEquals(443, profile.port)
        assertEquals("cdn.example.com", profile.serverName)
        assertEquals("h3", profile.alpn)
        assertEquals("Test TUIC", profile.name)
        assertFalse(profile.insecure)
        assertFalse(profile.zeroRttHandshake)
    }

    @Test
    fun parsesTuicOptions() {
        val profile =
            ConnectionProfileParser.parse(
                "tuic://11111111-1111-4111-8111-111111111111:secret@example.com:8443" +
                    "?congestion_control=bbr" +
                    "&udp_relay_mode=native" +
                    "&allow_insecure=1" +
                    "&zero_rtt_handshake=true" +
                    "&heartbeat=10s"
            ) as TuicProfile

        assertEquals(8443, profile.port)
        assertEquals("bbr", profile.congestionControl)
        assertEquals("native", profile.udpRelayMode)
        assertEquals("10s", profile.heartbeat)
        assertTrue(profile.insecure)
        assertTrue(profile.zeroRttHandshake)
    }

    @Test
    fun usesDefaultPort443() {
        val profile =
            TuicProfile.parse(
                "tuic://11111111-1111-4111-8111-111111111111:secret@example.com"
            )

        assertEquals(443, profile.port)
    }

    @Test
    fun preservesEncodedPasswordCharacters() {
        val profile =
            TuicProfile.parse(
                "tuic://11111111-1111-4111-8111-111111111111:p%40ss%3Aword@example.com:443"
            )

        assertEquals("p@ss:word", profile.password)
    }
}
