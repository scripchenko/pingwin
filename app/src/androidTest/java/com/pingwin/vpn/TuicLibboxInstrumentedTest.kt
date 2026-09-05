package com.pingwin.vpn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TuicLibboxInstrumentedTest {

    @Test
    fun libboxAcceptsBasicTuic() {
        validate(
            "tuic://11111111-1111-4111-8111-111111111111:secret@example.com:443" +
                "?sni=example.com" +
                "&alpn=h3"
        )
    }

    @Test
    fun libboxAcceptsTuicOptions() {
        validate(
            "tuic://11111111-1111-4111-8111-111111111111:secret@example.com:443" +
                "?sni=example.com" +
                "&alpn=h3" +
                "&congestion_control=bbr" +
                "&udp_relay_mode=native" +
                "&zero_rtt_handshake=true" +
                "&heartbeat=10s"
        )
    }

    @Test
    fun libboxAcceptsTuicInsecureTls() {
        validate(
            "tuic://11111111-1111-4111-8111-111111111111:secret@example.com:443" +
                "?sni=example.com" +
                "&allow_insecure=1"
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
                ?: "libbox rejected TUIC config",
            result.isSuccess
        )
    }
}
