package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VlessProfileCompatibilityTest {

    @Test
    fun ignoresUnknownQueryParameters() {
        val link =
            "vless://11111111-1111-4111-8111-111111111111@203.0.113.10:443" +
                "?security=reality" +
                "&pbk=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "&sni=example.com" +
                "&sid=0123456789abcdef" +
                "&type=tcp" +
                "&packetEncoding=xudp" +
                "&futureParameter=future-value" +
                "#Test"

        val profile = VlessProfile.parse(link)

        assertEquals("reality", profile.security)
        assertEquals("example.com", profile.serverName)
        assertEquals("0123456789abcdef", profile.shortId)
        assertEquals("tcp", profile.network)
    }

    @Test
    fun acceptsDifferentQueryParameterOrder() {
        val link =
            "vless://11111111-1111-4111-8111-111111111111@203.0.113.10:8443" +
                "?sni=example.com" +
                "&type=tcp" +
                "&flow=xtls-rprx-vision" +
                "&security=reality" +
                "&fp=chrome" +
                "&encryption=none"

        val profile = VlessProfile.parse(link)

        assertEquals(8443, profile.port)
        assertEquals("none", profile.encryption)
        assertEquals("xtls-rprx-vision", profile.flow)
        assertEquals("chrome", profile.fingerprint)
        assertEquals("reality", profile.security)
        assertEquals("example.com", profile.serverName)
        assertEquals("tcp", profile.network)
    }

    @Test
    fun decodesEncodedValuesAndSupportsMinimalLink() {
        val link =
            "vless://11111111-1111-4111-8111-111111111111@203.0.113.10" +
                "?spx=%2Ftest%2Fpath" +
                "#Test%20Server"

        val profile = VlessProfile.parse(link)

        assertEquals(443, profile.port)
        assertEquals("none", profile.encryption)
        assertEquals("/test/path", profile.spiderX)
        assertEquals("Test Server", profile.name)
        assertNull(profile.flow)
        assertNull(profile.security)
    }

    @Test
    fun parsesTransportParameters() {
        val link =
            "vless://11111111-1111-4111-8111-111111111111@203.0.113.10:443" +
                "?security=tls" +
                "&type=ws" +
                "&host=cdn.example.com" +
                "&path=%2Fvpn%2Fws" +
                "&serviceName=my-service" +
                "&mode=gun" +
                "&authority=grpc.example.com" +
                "&alpn=h2%2Chttp%2F1.1"

        val profile = VlessProfile.parse(link)

        assertEquals("ws", profile.network)
        assertEquals("cdn.example.com", profile.transportHost)
        assertEquals("/vpn/ws", profile.path)
        assertEquals("my-service", profile.serviceName)
        assertEquals("gun", profile.mode)
        assertEquals("grpc.example.com", profile.authority)
        assertEquals("h2,http/1.1", profile.alpn)
    }
}
