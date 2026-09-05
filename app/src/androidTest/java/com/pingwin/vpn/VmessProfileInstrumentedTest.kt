package com.pingwin.vpn

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VmessProfileInstrumentedTest {

    @Test
    fun parsesVmessTcpTls() {
        val link =
            vmessLink(
                """
                {
                  "v": "2",
                  "ps": "Test VMess",
                  "add": "example.com",
                  "port": "443",
                  "id": "11111111-1111-4111-8111-111111111111",
                  "aid": "0",
                  "scy": "auto",
                  "net": "tcp",
                  "type": "none",
                  "host": "",
                  "path": "",
                  "tls": "tls",
                  "sni": "cdn.example.com",
                  "alpn": "h2,http/1.1",
                  "fp": "chrome"
                }
                """.trimIndent()
            )

        val profile =
            ConnectionProfileParser.parse(link) as VmessProfile

        assertEquals("example.com", profile.host)
        assertEquals(443, profile.port)
        assertEquals(
            "11111111-1111-4111-8111-111111111111",
            profile.uuid
        )
        assertEquals(0, profile.alterId)
        assertEquals("auto", profile.security)
        assertEquals("tcp", profile.network)
        assertEquals("tls", profile.tls)
        assertEquals("cdn.example.com", profile.serverName)
        assertEquals("chrome", profile.fingerprint)
        assertEquals("h2,http/1.1", profile.alpn)
        assertEquals("Test VMess", profile.name)
    }

    @Test
    fun parsesVmessWebSocket() {
        val link =
            vmessLink(
                """
                {
                  "v": "2",
                  "ps": "VMess WS",
                  "add": "example.com",
                  "port": "443",
                  "id": "11111111-1111-4111-8111-111111111111",
                  "aid": "0",
                  "scy": "auto",
                  "net": "ws",
                  "host": "cdn.example.com",
                  "path": "/vmess",
                  "tls": "tls",
                  "sni": "cdn.example.com"
                }
                """.trimIndent()
            )

        val profile =
            VmessProfile.parse(link)

        assertEquals("ws", profile.network)
        assertEquals("cdn.example.com", profile.transportHost)
        assertEquals("/vmess", profile.path)
    }

    @Test
    fun parsesVmessGrpc() {
        val link =
            vmessLink(
                """
                {
                  "v": "2",
                  "ps": "VMess gRPC",
                  "add": "example.com",
                  "port": "443",
                  "id": "11111111-1111-4111-8111-111111111111",
                  "aid": "0",
                  "scy": "auto",
                  "net": "grpc",
                  "path": "vmess-grpc",
                  "tls": "tls",
                  "sni": "example.com"
                }
                """.trimIndent()
            )

        val profile =
            VmessProfile.parse(link)

        assertEquals("grpc", profile.network)
        assertEquals("vmess-grpc", profile.serviceName)
    }

    private fun vmessLink(
        json: String
    ): String {
        val encoded =
            Base64.encodeToString(
                json.toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )

        return "vmess://$encoded"
    }
}
