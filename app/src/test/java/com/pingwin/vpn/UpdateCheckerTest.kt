package com.pingwin.vpn

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun newerPatchVersionIsDetected() {
        assertTrue(
            UpdateChecker.isNewerVersion(
                remoteVersion = "0.1.4",
                currentVersion = "0.1.3"
            )
        )
    }

    @Test
    fun sameVersionIsNotNewer() {
        assertFalse(
            UpdateChecker.isNewerVersion(
                remoteVersion = "0.1.3",
                currentVersion = "0.1.3"
            )
        )
    }

    @Test
    fun olderVersionIsNotNewer() {
        assertFalse(
            UpdateChecker.isNewerVersion(
                remoteVersion = "0.1.2",
                currentVersion = "0.1.3"
            )
        )
    }

    @Test
    fun multiDigitVersionIsComparedNumerically() {
        assertTrue(
            UpdateChecker.isNewerVersion(
                remoteVersion = "0.1.10",
                currentVersion = "0.1.9"
            )
        )
    }

    @Test
    fun missingPartsAreTreatedAsZero() {
        assertFalse(
            UpdateChecker.isNewerVersion(
                remoteVersion = "1.0",
                currentVersion = "1.0.0"
            )
        )
    }
}
