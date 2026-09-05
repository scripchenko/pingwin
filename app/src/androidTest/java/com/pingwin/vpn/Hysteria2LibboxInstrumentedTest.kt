package com.pingwin.vpn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Hysteria2LibboxInstrumentedTest {

    @Test
    fun libboxAcceptsBasicHysteria2() {
        validate(
            "hysteria2://secret@example.com:443?sni=example.com"
        )
    }

    @Test
    fun libboxAcceptsHysteria2WithSalamander() {
        validate(
            "hysteria2://secret@example.com:443" +
                "?sni=example.com" +
                "&obfs=salamander" +
                "&obfs-password=hidden"
        )
    }

    @Test
    fun libboxAcceptsHysteria2PortHopping() {
        validate(
            "hy2://secret@example.com:20000-20010,30000" +
                "?sni=example.com"
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
                ?: "libbox rejected Hysteria2 config",
            result.isSuccess
        )
    }
}
