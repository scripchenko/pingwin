package com.pingwin.vpn

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VlessTransportConfigTest {

    @Test
    fun buildsWebSocketTransportWithTls() {
        val profile =
            VlessProfile.parse(
                "vless://11111111-1111-4111-8111-111111111111@example.com:443" +
                    "?encryption=none" +
                    "&security=tls" +
                    "&sni=cdn.example.com" +
                    "&type=ws" +
                    "&host=cdn.example.com" +
                    "&path=%2Fvpn"
            )

        val config =
            SingBoxConfigBuilder.build(profile)

        assertTrue(config.contains("\"type\": \"ws\""))
        assertTrue(config.contains("\"path\": \"/vpn\""))
        assertTrue(config.contains("\"Host\": \"cdn.example.com\""))
        assertTrue(config.contains("\"enabled\": true"))
        assertTrue(config.contains("\"server_name\": \"cdn.example.com\""))
        assertFalse(config.contains("\"reality\""))
    }

    @Test
    fun buildsGrpcTransportWithTls() {
        val profile =
            VlessProfile.parse(
                "vless://11111111-1111-4111-8111-111111111111@example.com:443" +
                    "?encryption=none" +
                    "&security=tls" +
                    "&sni=grpc.example.com" +
                    "&type=grpc" +
                    "&serviceName=pingwin"
            )

        val config =
            SingBoxConfigBuilder.build(profile)

        assertTrue(config.contains("\"type\": \"grpc\""))
        assertTrue(config.contains("\"service_name\": \"pingwin\""))
    }

    @Test
    fun buildsHttpUpgradeTransport() {
        val profile =
            VlessProfile.parse(
                "vless://11111111-1111-4111-8111-111111111111@example.com:443" +
                    "?encryption=none" +
                    "&security=tls" +
                    "&type=httpupgrade" +
                    "&host=edge.example.com" +
                    "&path=%2Fupgrade"
            )

        val config =
            SingBoxConfigBuilder.build(profile)

        assertTrue(config.contains("\"type\": \"httpupgrade\""))
        assertTrue(config.contains("\"host\": \"edge.example.com\""))
        assertTrue(config.contains("\"path\": \"/upgrade\""))
    }

    @Test
    fun buildsHttpTransport() {
        val profile =
            VlessProfile.parse(
                "vless://11111111-1111-4111-8111-111111111111@example.com:443" +
                    "?encryption=none" +
                    "&security=tls" +
                    "&type=http" +
                    "&host=edge.example.com" +
                    "&path=%2Fhttp"
            )

        val config =
            SingBoxConfigBuilder.build(profile)

        assertTrue(config.contains("\"type\": \"http\""))
        assertTrue(config.contains("\"host\": [\"edge.example.com\"]"))
        assertTrue(config.contains("\"path\": \"/http\""))
    }

    @Test
    fun keepsTcpWithoutTransportBlock() {
        val profile =
            VlessProfile.parse(
                "vless://11111111-1111-4111-8111-111111111111@example.com:443" +
                    "?encryption=none" +
                    "&security=reality" +
                    "&pbk=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                    "&sni=example.com" +
                    "&type=tcp"
            )

        val config =
            SingBoxConfigBuilder.build(profile)

        assertFalse(config.contains("\"transport\""))
        assertTrue(config.contains("\"reality\""))
    }
}
