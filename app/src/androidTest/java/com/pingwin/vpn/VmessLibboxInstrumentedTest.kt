package com.pingwin.vpn

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VmessLibboxInstrumentedTest {

    @Test
    fun libboxAcceptsVmessTcpTls() {
        validate(
            """
            {
              "v": "2",
              "ps": "VMess TCP",
              "add": "example.com",
              "port": "443",
              "id": "11111111-1111-4111-8111-111111111111",
              "aid": "0",
              "scy": "auto",
              "net": "tcp",
              "tls": "tls",
              "sni": "example.com"
            }
            """.trimIndent()
        )
    }

    @Test
    fun libboxAcceptsVmessWebSocket() {
        validate(
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
              "sni": "example.com"
            }
            """.trimIndent()
        )
    }

    @Test
    fun libboxAcceptsVmessGrpc() {
        validate(
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
    }

    @Test
    fun libboxAcceptsVmessH2() {
        validate(
            """
            {
              "v": "2",
              "ps": "VMess H2",
              "add": "example.com",
              "port": "443",
              "id": "11111111-1111-4111-8111-111111111111",
              "aid": "0",
              "scy": "auto",
              "net": "h2",
              "host": "cdn.example.com",
              "path": "/vmess",
              "tls": "tls",
              "sni": "example.com"
            }
            """.trimIndent()
        )
    }

    private fun validate(
        json: String
    ) {
        val encoded =
            Base64.encodeToString(
                json.toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )

        val profile =
            ConnectionProfileParser.parse(
                "vmess://$encoded"
            )

        val config =
            ConnectionConfigBuilder.build(profile)

        val result =
            LibboxValidator.validate(config)

        assertTrue(
            result.exceptionOrNull()?.message
                ?: "libbox rejected VMess config",
            result.isSuccess
        )
    }
}
