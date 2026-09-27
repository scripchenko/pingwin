package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionProfileParserTest {

    @Test
    fun rejectsOversizedInputBeforeProtocolParsing() {
        val link =
            "vless://" +
                "a".repeat(
                    ConnectionProfileParser.MAX_LINK_LENGTH
                )

        assertEquals(
            ConnectionParseError.INPUT_TOO_LARGE,
            parseError(link)
        )
    }

    @Test
    fun rejectsRawControlCharacters() {
        val link =
            "vless://11111111-1111-4111-8111-111111111111@example.com:443" +
                "\n#Injected"

        assertEquals(
            ConnectionParseError.INVALID_CHARACTERS,
            parseError(link)
        )
    }

    @Test
    fun stillAcceptsNormalConnectionLink() {
        val profile =
            ConnectionProfileParser.parse(
                "vless://11111111-1111-4111-8111-111111111111@example.com:443"
            )

        assertEquals(
            "example.com",
            profile.host
        )
    }

    private fun parseError(
        link: String
    ): ConnectionParseError {
        try {
            ConnectionProfileParser.parse(link)
        } catch (error: ConnectionParseException) {
            return error.error
        }

        throw AssertionError(
            "Expected ConnectionParseException"
        )
    }
}