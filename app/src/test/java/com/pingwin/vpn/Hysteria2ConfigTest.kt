package com.pingwin.vpn

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Hysteria2ConfigTest {

    @Test
    fun buildsBasicHysteria2Outbound() {
        val profile =
            Hysteria2Profile.parse(
                "hysteria2://secret@example.com:443?sni=cdn.example.com"
            )

        val config =
            Hysteria2ConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "hysteria2""""))
        assertTrue(config.contains(""""tag": "proxy""""))
        assertTrue(config.contains(""""server": "example.com""""))
        assertTrue(config.contains(""""server_port": 443"""))
        assertTrue(config.contains(""""password": "secret""""))
        assertTrue(config.contains(""""server_name": "cdn.example.com""""))
        assertTrue(config.contains(""""insecure": false"""))
        assertFalse(config.contains(""""server_ports""""))
    }

    @Test
    fun buildsPortHopping() {
        val profile =
            Hysteria2Profile.parse(
                "hy2://secret@example.com:20000-20010,30000"
            )

        val config =
            Hysteria2ConfigBuilder.build(profile)

        assertTrue(
            config.contains(
                """"server_ports": ["20000:20010", "30000:30000"]"""
            )
        )
        assertFalse(config.contains(""""server_port": 20000"""))
    }

    @Test
    fun buildsSalamanderObfuscation() {
        val profile =
            Hysteria2Profile.parse(
                "hysteria2://secret@example.com:443?obfs=salamander&obfs-password=hidden"
            )

        val config =
            Hysteria2ConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "salamander""""))
        assertTrue(config.contains(""""password": "hidden""""))
    }

    @Test
    fun buildsInsecureTls() {
        val profile =
            Hysteria2Profile.parse(
                "hysteria2://secret@example.com:443?insecure=1"
            )

        val config =
            Hysteria2ConfigBuilder.build(profile)

        assertTrue(config.contains(""""insecure": true"""))
    }

    @Test
    fun connectionBuilderUsesHysteria2Builder() {
        val profile =
            ConnectionProfileParser.parse(
                "hysteria2://secret@example.com:443"
            )

        val config =
            ConnectionConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "hysteria2""""))
    }
}
