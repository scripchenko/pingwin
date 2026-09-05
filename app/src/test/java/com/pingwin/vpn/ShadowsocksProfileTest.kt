package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.util.Base64

class ShadowsocksProfileTest {

    @Test
    fun parsesSip002Base64UserInfo() {
        val credentials =
            Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                    "aes-256-gcm:secret"
                        .toByteArray(
                            StandardCharsets.UTF_8
                        )
                )

        val profile =
            ShadowsocksProfile.parse(
                "ss://$credentials@example.com:8388#Test%20SS"
            )

        assertEquals("aes-256-gcm", profile.method)
        assertEquals("secret", profile.password)
        assertEquals("example.com", profile.host)
        assertEquals(8388, profile.port)
        assertEquals("Test SS", profile.name)
    }

    @Test
    fun parsesSip002PlainCredentials() {
        val profile =
            ConnectionProfileParser.parse(
                "ss://aes-128-gcm:secret@example.com:443"
            ) as ShadowsocksProfile

        assertEquals("aes-128-gcm", profile.method)
        assertEquals("secret", profile.password)
        assertEquals("example.com", profile.host)
        assertEquals(443, profile.port)
    }

    @Test
    fun parsesSip002Plugin() {
        val profile =
            ShadowsocksProfile.parse(
                "ss://aes-256-gcm:secret@example.com:8388" +
                    "?plugin=v2ray-plugin%3Btls%3Bhost%3Dcdn.example.com"
            )

        assertEquals(
            "v2ray-plugin;tls;host=cdn.example.com",
            profile.plugin
        )
    }

    @Test
    fun parsesLegacyBase64Link() {
        val payload =
            Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                    "chacha20-ietf-poly1305:secret@example.com:8388"
                        .toByteArray(
                            StandardCharsets.UTF_8
                        )
                )

        val profile =
            ShadowsocksProfile.parse(
                "ss://$payload#Legacy"
            )

        assertEquals(
            "chacha20-ietf-poly1305",
            profile.method
        )
        assertEquals("secret", profile.password)
        assertEquals("example.com", profile.host)
        assertEquals(8388, profile.port)
        assertEquals("Legacy", profile.name)
    }
}
