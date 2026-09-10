package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class PortValidationIntegrationTest {

    @Test
    fun vlessRejectsOutOfRangePort() {
        try {
            VlessProfile.parse(
                "vless://11111111-1111-4111-8111-111111111111@example.com:65536"
            )
            fail("Expected VlessParseException")
        } catch (e: VlessParseException) {
            assertEquals(
                VlessParseError.INVALID_PORT,
                e.error
            )
        }
    }

    @Test
    fun trojanRejectsOutOfRangePort() {
        try {
            TrojanProfile.parse(
                "trojan://secret@example.com:65536"
            )
            fail("Expected TrojanParseException")
        } catch (e: TrojanParseException) {
            assertEquals(
                TrojanParseError.INVALID_PORT,
                e.error
            )
        }
    }

    @Test
    fun tuicRejectsOutOfRangePort() {
        try {
            TuicProfile.parse(
                "tuic://11111111-1111-4111-8111-111111111111:secret@example.com:65536"
            )
            fail("Expected TuicParseException")
        } catch (e: TuicParseException) {
            assertEquals(
                TuicParseError.INVALID_PORT,
                e.error
            )
        }
    }
}
