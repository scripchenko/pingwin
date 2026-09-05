package com.pingwin.vpn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShadowsocksLibboxInstrumentedTest {

    @Test
    fun libboxAcceptsAes256Gcm() {
        validate(
            "ss://YWVzLTI1Ni1nY206c2VjcmV0@example.com:8388"
        )
    }

    @Test
    fun libboxAcceptsAes128Gcm() {
        validate(
            "ss://YWVzLTEyOC1nY206c2VjcmV0@example.com:8388"
        )
    }

    @Test
    fun libboxAcceptsChacha20Poly1305() {
        validate(
            "ss://Y2hhY2hhMjAtaWV0Zi1wb2x5MTMwNTpzZWNyZXQ@example.com:8388"
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
                ?: "libbox rejected Shadowsocks config",
            result.isSuccess
        )
    }
}
