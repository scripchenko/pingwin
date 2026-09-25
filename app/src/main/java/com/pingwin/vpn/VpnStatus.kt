package com.pingwin.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class VpnConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

object VpnStatus {
    private val _state =
        MutableStateFlow(
            VpnConnectionState.DISCONNECTED
        )

    val state: StateFlow<VpnConnectionState> =
        _state.asStateFlow()

    private val _activeConnectionId =
        MutableStateFlow<String?>(null)

    val activeConnectionId: StateFlow<String?> =
        _activeConnectionId.asStateFlow()

    fun set(
        value: VpnConnectionState
    ) {
        _state.value = value
    }

    fun setActiveConnectionId(
        connectionId: String?
    ) {
        _activeConnectionId.value = connectionId
    }
}
