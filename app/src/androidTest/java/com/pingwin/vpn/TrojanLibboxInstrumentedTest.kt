package com.pingwin.vpn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrojanLibboxInstrumentedTest {

    @Test
    fun libboxAcceptsTrojanTls() {
        validate(
            "trojan://secret@example.com:443" +
                "?security=tls" +
                "&sni=example.com"
        )
    }

    @Test
    fun libboxAcceptsTrojanWebSocket() {
        validate(
            "trojan://secret@example.com:443" +
                "?security=tls" +
                "&sni=example.com" +
                "&type=ws" +
                "&host=cdn.example.com" +
                "&path=%2Ftrojan"
        )
    }

    @Test
    fun libboxAcceptsTrojanGrpc() {
        validate(
            "trojan://secret@example.com:443" +
                "?security=tls" +
                "&sni=example.com" +
                "&type=grpc" +
                "&serviceName=trojan-grpc"
        )
    }

    @Test
    fun libboxAcceptsTrojanReality() {
        validate(
            "trojan://secret@example.com:443" +
                "?security=reality" +
                "&sni=example.com" +
                "&fp=chrome" +
                "&pbk=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "&sid=0123456789abcdef"
        )
    }

    private fun validate(
        link: String
    ) {
        val profile =
            ConnectionProfileParser.parse(link)

        val config =
            ConnectionConfigBuilder.build(profile)

        val result =
            LibboxValidator.validate(config)

        assertTrue(
            result.exceptionOrNull()?.message
                ?: "libbox rejected Trojan config",
            result.isSuccess
        )
    }
}
