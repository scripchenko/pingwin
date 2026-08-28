package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun parseWorking3xUiVlessLink() {
        val link =
            "vless://11111111-1111-4111-8111-111111111111@203.0.113.10:443" +
                    "?encryption=none" +
                    "&flow=xtls-rprx-vision" +
                    "&fp=chrome" +
                    "&pbk=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                    "&security=reality" +
                    "&sid=0123456789abcdef" +
                    "&sni=amazon.com" +
                    "&spx=%2Ftest-spider" +
                    "&type=tcp" +
                    "#Test-Reality"

        val profile = VlessProfile.parse(link)

        assertEquals("11111111-1111-4111-8111-111111111111", profile.uuid)
        assertEquals("203.0.113.10", profile.host)
        assertEquals(443, profile.port)
        assertEquals("none", profile.encryption)
        assertEquals("xtls-rprx-vision", profile.flow)
        assertEquals("chrome", profile.fingerprint)
        assertEquals("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", profile.publicKey)
        assertEquals("reality", profile.security)
        assertEquals("0123456789abcdef", profile.shortId)
        assertEquals("amazon.com", profile.serverName)
        assertEquals("/test-spider", profile.spiderX)
        assertEquals("tcp", profile.network)
        assertEquals("Test-Reality", profile.name)
    }

    @Test
    fun buildSingBoxConfigForWorkingProfile() {
        val link =
            "vless://11111111-1111-4111-8111-111111111111@203.0.113.10:443" +
                    "?encryption=none" +
                    "&flow=xtls-rprx-vision" +
                    "&fp=chrome" +
                    "&pbk=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                    "&security=reality" +
                    "&sid=0123456789abcdef" +
                    "&sni=amazon.com" +
                    "&spx=%2Ftest-spider" +
                    "&type=tcp" +
                    "#Test-Reality"

        val profile = VlessProfile.parse(link)
        val config = SingBoxConfigBuilder.build(profile)

        assertTrue(config.contains("\"type\": \"vless\""))
        assertTrue(config.contains("\"server_port\": 443"))
        assertTrue(config.contains("\"flow\": \"xtls-rprx-vision\""))
        assertTrue(config.contains("\"server_name\": \"amazon.com\""))
        assertTrue(config.contains("\"fingerprint\": \"chrome\""))
        assertTrue(config.contains("\"short_id\": \"0123456789abcdef\""))
        assertTrue(config.contains("\"final\": \"proxy\""))
    }
}
