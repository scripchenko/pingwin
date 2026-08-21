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

    fun set(
        value: VpnConnectionState
    ) {
        _state.value = value
    }
}
