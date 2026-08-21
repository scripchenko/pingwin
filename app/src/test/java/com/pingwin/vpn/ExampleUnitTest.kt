package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun parseWorking3xUiVlessLink() {
        val link =
            "vless://79ffee41-ab87-44c2-8f53-fd0afe05fa11@95.164.93.212:443" +
                    "?encryption=none" +
                    "&flow=xtls-rprx-vision" +
                    "&fp=chrome" +
                    "&pbk=lSPik24zP7ion8DrfLkvoTrVnT7J2VOEbouTq3uTcho" +
                    "&security=reality" +
                    "&sid=50" +
                    "&sni=amazon.com" +
                    "&spx=%2F4b01c62261fac66" +
                    "&type=tcp" +
                    "#VLESS-Reality-dmitry_scripchenko"

        val profile = VlessProfile.parse(link)

        assertEquals("79ffee41-ab87-44c2-8f53-fd0afe05fa11", profile.uuid)
        assertEquals("95.164.93.212", profile.host)
        assertEquals(443, profile.port)
        assertEquals("none", profile.encryption)
        assertEquals("xtls-rprx-vision", profile.flow)
        assertEquals("chrome", profile.fingerprint)
        assertEquals(
            "lSPik24zP7ion8DrfLkvoTrVnT7J2VOEbouTq3uTcho",
            profile.publicKey
        )
        assertEquals("reality", profile.security)
        assertEquals("50", profile.shortId)
        assertEquals("amazon.com", profile.serverName)
        assertEquals("/4b01c62261fac66", profile.spiderX)
        assertEquals("tcp", profile.network)
        assertEquals("VLESS-Reality-dmitry_scripchenko", profile.name)
    }

    @Test
    fun buildSingBoxConfigForWorkingProfile() {
        val link =
            "vless://79ffee41-ab87-44c2-8f53-fd0afe05fa11@95.164.93.212:443" +
                    "?encryption=none" +
                    "&flow=xtls-rprx-vision" +
                    "&fp=chrome" +
                    "&pbk=lSPik24zP7ion8DrfLkvoTrVnT7J2VOEbouTq3uTcho" +
                    "&security=reality" +
                    "&sid=50" +
                    "&sni=amazon.com" +
                    "&spx=%2F4b01c62261fac66" +
                    "&type=tcp" +
                    "#VLESS-Reality-dmitry_scripchenko"

        val profile = VlessProfile.parse(link)
        val config = SingBoxConfigBuilder.build(profile)

        assertTrue(config.contains("\"type\": \"vless\""))
        assertTrue(config.contains("\"server_port\": 443"))
        assertTrue(config.contains("\"flow\": \"xtls-rprx-vision\""))
        assertTrue(config.contains("\"server_name\": \"amazon.com\""))
        assertTrue(config.contains("\"fingerprint\": \"chrome\""))
        assertTrue(config.contains("\"short_id\": \"50\""))
        assertTrue(config.contains("\"final\": \"proxy\""))
    }
}