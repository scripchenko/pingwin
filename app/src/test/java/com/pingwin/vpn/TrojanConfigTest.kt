package com.pingwin.vpn

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrojanConfigTest {

    @Test
    fun buildsBasicTrojanTls() {
        val profile =
            TrojanProfile.parse(
                "trojan://secret@example.com:443?security=tls&sni=cdn.example.com"
            )

        val config =
            TrojanConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "trojan""""))
        assertTrue(config.contains(""""server": "example.com""""))
        assertTrue(config.contains(""""server_port": 443"""))
        assertTrue(config.contains(""""password": "secret""""))
        assertTrue(config.contains(""""enabled": true"""))
        assertTrue(config.contains(""""server_name": "cdn.example.com""""))
        assertFalse(config.contains(""""transport""""))
    }

    @Test
    fun buildsTrojanWebSocket() {
        val profile =
            TrojanProfile.parse(
                "trojan://secret@example.com:443?security=tls&type=ws&host=cdn.example.com&path=%2Ftrojan"
            )

        val config =
            TrojanConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "ws""""))
        assertTrue(config.contains(""""path": "/trojan""""))
        assertTrue(config.contains(""""Host": "cdn.example.com""""))
    }

    @Test
    fun buildsTrojanGrpc() {
        val profile =
            TrojanProfile.parse(
                "trojan://secret@example.com:443?security=tls&type=grpc&serviceName=trojan-grpc"
            )

        val config =
            TrojanConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "grpc""""))
        assertTrue(config.contains(""""service_name": "trojan-grpc""""))
    }

    @Test
    fun buildsTrojanReality() {
        val profile =
            TrojanProfile.parse(
                "trojan://secret@example.com:443" +
                    "?security=reality" +
                    "&sni=cdn.example.com" +
                    "&fp=chrome" +
                    "&pbk=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                    "&sid=0123456789abcdef"
            )

        val config =
            TrojanConfigBuilder.build(profile)

        assertTrue(config.contains(""""reality""""))
        assertTrue(
            config.contains(
                """"public_key": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA""""
            )
        )
        assertTrue(
            config.contains(
                """"short_id": "0123456789abcdef""""
            )
        )
    }

    @Test
    fun connectionBuilderUsesTrojanBuilder() {
        val profile =
            ConnectionProfileParser.parse(
                "trojan://secret@example.com:443?security=tls"
            )

        val config =
            ConnectionConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "trojan""""))
    }
}
