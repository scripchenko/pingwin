package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrojanProfileTest {

    @Test
    fun parsesTrojanTcpTls() {
        val profile =
            TrojanProfile.parse(
                "trojan://secret@example.com:443?security=tls&sni=cdn.example.com&fp=chrome#Test%20Trojan"
            )

        assertEquals("secret", profile.password)
        assertEquals("example.com", profile.host)
        assertEquals(443, profile.port)
        assertEquals("tls", profile.security)
        assertEquals("cdn.example.com", profile.serverName)
        assertEquals("chrome", profile.fingerprint)
        assertEquals("Test Trojan", profile.name)
        assertFalse(profile.insecure)
    }

    @Test
    fun parsesTrojanWebSocket() {
        val profile =
            ConnectionProfileParser.parse(
                "trojan://secret@example.com:443?security=tls&type=ws&host=cdn.example.com&path=%2Ftrojan"
            ) as TrojanProfile

        assertEquals("ws", profile.network)
        assertEquals("cdn.example.com", profile.transportHost)
        assertEquals("/trojan", profile.path)
    }

    @Test
    fun parsesTrojanGrpc() {
        val profile =
            TrojanProfile.parse(
                "trojan://secret@example.com:443?security=tls&type=grpc&serviceName=my-service"
            )

        assertEquals("grpc", profile.network)
        assertEquals("my-service", profile.serviceName)
    }

    @Test
    fun parsesAllowInsecure() {
        val profile =
            TrojanProfile.parse(
                "trojan://secret@example.com:443?allowInsecure=1"
            )

        assertTrue(profile.insecure)
    }
}
