package com.pingwin.vpn

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TuicConfigTest {

    @Test
    fun buildsBasicTuic() {
        val profile =
            TuicProfile.parse(
                "tuic://11111111-1111-4111-8111-111111111111:secret@example.com:443" +
                    "?sni=cdn.example.com" +
                    "&alpn=h3"
            )

        val config =
            TuicConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "tuic""""))
        assertTrue(config.contains(""""server": "example.com""""))
        assertTrue(config.contains(""""server_port": 443"""))
        assertTrue(
            config.contains(
                """"uuid": "11111111-1111-4111-8111-111111111111""""
            )
        )
        assertTrue(config.contains(""""password": "secret""""))
        assertTrue(config.contains(""""server_name": "cdn.example.com""""))
        assertTrue(config.contains(""""alpn": ["h3"]"""))
    }

    @Test
    fun buildsTuicOptions() {
        val profile =
            TuicProfile.parse(
                "tuic://11111111-1111-4111-8111-111111111111:secret@example.com:443" +
                    "?congestion_control=bbr" +
                    "&udp_relay_mode=native" +
                    "&zero_rtt_handshake=true" +
                    "&heartbeat=10s"
            )

        val config =
            TuicConfigBuilder.build(profile)

        assertTrue(config.contains(""""congestion_control": "bbr""""))
        assertTrue(config.contains(""""udp_relay_mode": "native""""))
        assertTrue(config.contains(""""zero_rtt_handshake": true"""))
        assertTrue(config.contains(""""heartbeat": "10s""""))
    }

    @Test
    fun buildsTuicInsecureTls() {
        val profile =
            TuicProfile.parse(
                "tuic://11111111-1111-4111-8111-111111111111:secret@example.com:443" +
                    "?allow_insecure=1"
            )

        val config =
            TuicConfigBuilder.build(profile)

        assertTrue(config.contains(""""insecure": true"""))
    }

    @Test
    fun omitsOptionalFieldsWhenAbsent() {
        val profile =
            TuicProfile.parse(
                "tuic://11111111-1111-4111-8111-111111111111@example.com:443"
            )

        val config =
            TuicConfigBuilder.build(profile)

        assertFalse(config.contains(""""password""""))
        assertFalse(config.contains(""""congestion_control""""))
        assertFalse(config.contains(""""udp_relay_mode""""))
        assertFalse(config.contains(""""zero_rtt_handshake""""))
        assertFalse(config.contains(""""heartbeat""""))
    }

    @Test
    fun connectionBuilderUsesTuicBuilder() {
        val profile =
            ConnectionProfileParser.parse(
                "tuic://11111111-1111-4111-8111-111111111111:secret@example.com:443"
            )

        val config =
            ConnectionConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "tuic""""))
    }
}
