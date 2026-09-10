package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PortValidatorTest {

    @Test
    fun validBoundariesAreAccepted() {
        assertTrue(
            PortValidator.isValid(1)
        )
        assertTrue(
            PortValidator.isValid(65535)
        )
    }

    @Test
    fun invalidBoundariesAreRejected() {
        assertFalse(
            PortValidator.isValid(0)
        )
        assertFalse(
            PortValidator.isValid(65536)
        )
    }

    @Test
    fun validPortStringIsParsed() {
        assertEquals(
            443,
            PortValidator.parse("443")
        )
    }

    @Test
    fun invalidPortStringsReturnNull() {
        assertNull(
            PortValidator.parse("0")
        )
        assertNull(
            PortValidator.parse("65536")
        )
        assertNull(
            PortValidator.parse("abc")
        )
    }
}
