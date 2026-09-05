package com.pingwin.vpn

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadowsocksConfigTest {

    @Test
    fun buildsAes256Gcm() {
        val profile =
            ShadowsocksProfile.parse(
                "ss://YWVzLTI1Ni1nY206c2VjcmV0@example.com:8388"
            )

        val config =
            ShadowsocksConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "shadowsocks""""))
        assertTrue(config.contains(""""server": "example.com""""))
        assertTrue(config.contains(""""server_port": 8388"""))
        assertTrue(config.contains(""""method": "aes-256-gcm""""))
        assertTrue(config.contains(""""password": "secret""""))
    }

    @Test
    fun buildsChacha20Poly1305() {
        val profile =
            ShadowsocksProfile.parse(
                "ss://Y2hhY2hhMjAtaWV0Zi1wb2x5MTMwNTpzZWNyZXQ@example.com:443"
            )

        val config =
            ShadowsocksConfigBuilder.build(profile)

        assertTrue(
            config.contains(
                """"method": "chacha20-ietf-poly1305""""
            )
        )
    }

    @Test
    fun connectionBuilderUsesShadowsocksBuilder() {
        val profile =
            ConnectionProfileParser.parse(
                "ss://YWVzLTEyOC1nY206c2VjcmV0@example.com:8388"
            )

        val config =
            ConnectionConfigBuilder.build(profile)

        assertTrue(config.contains(""""type": "shadowsocks""""))
    }

    @Test
    fun doesNotAddTlsOrTransport() {
        val profile =
            ShadowsocksProfile.parse(
                "ss://YWVzLTI1Ni1nY206c2VjcmV0@example.com:8388"
            )

        val config =
            ShadowsocksConfigBuilder.build(profile)

        assertFalse(config.contains(""""tls""""))
        assertFalse(config.contains(""""transport""""))
    }
}
