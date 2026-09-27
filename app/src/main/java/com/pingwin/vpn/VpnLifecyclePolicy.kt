package com.pingwin.vpn

object VpnLifecyclePolicy {
    fun canStart(
        hasCommandServer: Boolean,
        isStarting: Boolean
    ): Boolean =
        !hasCommandServer && !isStarting

    fun isStaleStop(
        expectedSessionStartId: Int?,
        currentSessionStartId: Int
    ): Boolean =
        expectedSessionStartId != null &&
            expectedSessionStartId != currentSessionStartId

    fun lockedConnectionId(
        state: VpnConnectionState,
        activeConnectionId: String?
    ): String? =
        if (
            state == VpnConnectionState.CONNECTED ||
            state == VpnConnectionState.CONNECTING
        ) {
            activeConnectionId
        } else {
            null
        }

    fun displayedConnectionId(
        state: VpnConnectionState,
        activeConnectionId: String?,
        selectedConnectionId: String?
    ): String? =
        if (
            state == VpnConnectionState.CONNECTED ||
            state == VpnConnectionState.CONNECTING
        ) {
            activeConnectionId ?: selectedConnectionId
        } else {
            selectedConnectionId
        }
}