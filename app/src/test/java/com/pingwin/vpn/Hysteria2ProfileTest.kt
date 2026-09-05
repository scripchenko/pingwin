package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Hysteria2ProfileTest {

    @Test
    fun parsesHysteria2Link() {
        val profile =
            Hysteria2Profile.parse(
                "hysteria2://secret@example.com:443?sni=cdn.example.com&insecure=1&obfs=salamander&obfs-password=obfs123#Test%20HY2"
            )

        assertEquals("example.com", profile.host)
        assertEquals(443, profile.port)
        assertEquals(listOf("443"), profile.serverPorts)
        assertEquals("secret", profile.password)
        assertEquals("cdn.example.com", profile.serverName)
        assertTrue(profile.insecure)
        assertEquals("salamander", profile.obfsType)
        assertEquals("obfs123", profile.obfsPassword)
        assertEquals("Test HY2", profile.name)
    }

    @Test
    fun parsesHy2AliasAndPortHopping() {
        val profile =
            ConnectionProfileParser.parse(
                "hy2://password@example.com:20000-20010,30000"
            ) as Hysteria2Profile

        assertEquals("example.com", profile.host)
        assertEquals(20000, profile.port)
        assertEquals(
            listOf("20000:20010", "30000"),
            profile.serverPorts
        )
        assertFalse(profile.insecure)
    }

    @Test
    fun usesDefaultPort443() {
        val profile =
            Hysteria2Profile.parse(
                "hysteria2://password@example.com"
            )

        assertEquals(443, profile.port)
        assertEquals(listOf("443"), profile.serverPorts)
    }

    @Test
    fun parsesIpv6Server() {
        val profile =
            Hysteria2Profile.parse(
                "hysteria2://password@[2001:db8::1]:8443"
            )

        assertEquals("2001:db8::1", profile.host)
        assertEquals(8443, profile.port)
    }
}
