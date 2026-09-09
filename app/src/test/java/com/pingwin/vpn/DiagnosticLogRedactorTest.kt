package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticLogRedactorTest {

    @Test
    fun vpnUriCredentialsAndHostAreRedacted() {
        val result =
            DiagnosticLogRedactor.redact(
                "vless://550e8400-e29b-41d4-a716-446655440000@95.164.93.212:443?token=secret123"
            )

        assertFalse(
            result.contains(
                "550e8400-e29b-41d4-a716-446655440000"
            )
        )
        assertFalse(
            result.contains(
                "secret123"
            )
        )
        assertTrue(
            result.contains(
                "95.***.***.212:443"
            )
        )
    }

    @Test
    fun ipv4AddressIsMasked() {
        assertEquals(
            "connect 95.***.***.212:443",
            DiagnosticLogRedactor.redact(
                "connect 95.164.93.212:443"
            )
        )
    }

    @Test
    fun domainIsMasked() {
        assertEquals(
            "destination www.***.com:443",
            DiagnosticLogRedactor.redact(
                "destination www.example.com:443"
            )
        )
    }

    @Test
    fun ipv6AddressIsMasked() {
        val result =
            DiagnosticLogRedactor.redact(
                "connect [2001:db8::1]:443"
            )

        assertFalse(
            result.contains(
                "2001:db8::1"
            )
        )
    }

    @Test
    fun timestampIsNotMistakenForIpv6() {
        assertEquals(
            "12:34:56 connected",
            DiagnosticLogRedactor.redact(
                "12:34:56 connected"
            )
        )
    }

    @Test
    fun sensitiveParametersAreRedacted() {
        val result =
            DiagnosticLogRedactor.redact(
                "password=hunter2 token=abc123 uuid=550e8400-e29b-41d4-a716-446655440000"
            )

        assertFalse(result.contains("hunter2"))
        assertFalse(result.contains("abc123"))
        assertFalse(
            result.contains(
                "550e8400-e29b-41d4-a716-446655440000"
            )
        )
    }

    @Test
    fun authorizationHeaderIsRedacted() {
        assertEquals(
            "Authorization: ***",
            DiagnosticLogRedactor.redact(
                "Authorization: Bearer very-secret-token"
            )
        )
    }

    @Test
    fun ordinaryDiagnosticTextIsPreserved() {
        assertEquals(
            "VPN connected successfully",
            DiagnosticLogRedactor.redact(
                "VPN connected successfully"
            )
        )
    }
}
