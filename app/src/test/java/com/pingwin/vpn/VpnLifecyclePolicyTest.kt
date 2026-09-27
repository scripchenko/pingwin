package com.pingwin.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VpnLifecyclePolicyTest {
    @Test
    fun repeatedStartIsRejectedWhenServerAlreadyExists() {
        assertFalse(
            VpnLifecyclePolicy.canStart(
                hasCommandServer = true,
                isStarting = false
            )
        )
    }

    @Test
    fun repeatedStartIsRejectedWhileStartupIsInProgress() {
        assertFalse(
            VpnLifecyclePolicy.canStart(
                hasCommandServer = false,
                isStarting = true
            )
        )
    }

    @Test
    fun startIsAllowedWhenSessionIsIdle() {
        assertTrue(
            VpnLifecyclePolicy.canStart(
                hasCommandServer = false,
                isStarting = false
            )
        )
    }

    @Test
    fun oldSessionStopIsRecognizedAsStale() {
        assertTrue(
            VpnLifecyclePolicy.isStaleStop(
                expectedSessionStartId = 10,
                currentSessionStartId = 12
            )
        )
    }

    @Test
    fun currentSessionStopIsNotStale() {
        assertFalse(
            VpnLifecyclePolicy.isStaleStop(
                expectedSessionStartId = 12,
                currentSessionStartId = 12
            )
        )
    }

    @Test
    fun explicitStopWithoutExpectedSessionIsNeverStale() {
        assertFalse(
            VpnLifecyclePolicy.isStaleStop(
                expectedSessionStartId = null,
                currentSessionStartId = 12
            )
        )
    }

    @Test
    fun activeConnectionLocksSelectionWhileConnected() {
        assertEquals(
            "active",
            VpnLifecyclePolicy.lockedConnectionId(
                VpnConnectionState.CONNECTED,
                "active"
            )
        )
    }

    @Test
    fun activeConnectionLocksSelectionWhileConnecting() {
        assertEquals(
            "active",
            VpnLifecyclePolicy.lockedConnectionId(
                VpnConnectionState.CONNECTING,
                "active"
            )
        )
    }

    @Test
    fun disconnectedStateDoesNotLockSelectedProfile() {
        assertNull(
            VpnLifecyclePolicy.lockedConnectionId(
                VpnConnectionState.DISCONNECTED,
                "old-active"
            )
        )
    }

    @Test
    fun connectedHomeUsesActiveInsteadOfNewlySelectedProfile() {
        assertEquals(
            "active",
            VpnLifecyclePolicy.displayedConnectionId(
                VpnConnectionState.CONNECTED,
                "active",
                "selected"
            )
        )
    }

    @Test
    fun disconnectedHomeUsesSelectedProfile() {
        assertEquals(
            "selected",
            VpnLifecyclePolicy.displayedConnectionId(
                VpnConnectionState.DISCONNECTED,
                "old-active",
                "selected"
            )
        )
    }

    @Test
    fun missingActiveProfileFallsBackToSelectedProfile() {
        assertEquals(
            "selected",
            VpnLifecyclePolicy.displayedConnectionId(
                VpnConnectionState.CONNECTED,
                null,
                "selected"
            )
        )
    }
}