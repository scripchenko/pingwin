package com.pingwin.vpn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

@Composable
fun ConnectionDiagnosticsScreen(
    connection: SavedConnection?,
    vpnState: VpnConnectionState,
    activeConnectionId: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var refreshKey by remember(connection?.id) { mutableStateOf(0) }
    var result by remember(connection?.id) {
        mutableStateOf<ConnectionDiagnosticResult?>(null)
    }
    var running by remember(connection?.id) { mutableStateOf(false) }

    LaunchedEffect(connection?.id, refreshKey) {
        if (connection != null) {
            running = true
            result = null
            try {
                result = ConnectionDiagnostics.run(context, connection)
            } finally {
                running = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Text(text = "←", style = MaterialTheme.typography.headlineMedium)
        }

        Text(
            text = stringResource(R.string.diagnostics_title),
            fontSize = 28.sp,
            color = Color(0xFF17191F)
        )

        if (connection == null) {
            Text(stringResource(R.string.diagnostics_profile_missing))
            return@Column
        }

        result?.let { report ->
            if (report.protocol != null) {
                Text(
                    text = stringResource(
                        R.string.diagnostics_server,
                        report.protocol,
                        report.maskedHost.orEmpty(),
                        report.port ?: 0
                    ),
                    color = Color(0xFF555D6B),
                    fontSize = 16.sp
                )
            }
        }

        val vpnStateText = when {
            vpnState == VpnConnectionState.CONNECTED &&
                activeConnectionId == connection.id -> R.string.diagnostics_vpn_this_active
            vpnState == VpnConnectionState.CONNECTED -> R.string.diagnostics_vpn_other_active
            vpnState == VpnConnectionState.CONNECTING -> R.string.diagnostics_vpn_connecting
            vpnState == VpnConnectionState.ERROR -> R.string.diagnostics_vpn_error
            else -> R.string.diagnostics_vpn_off
        }
        Text(
            text = stringResource(vpnStateText),
            fontSize = 14.sp,
            color = Color(0xFF555D6B)
        )

        Text(
            text = stringResource(R.string.diagnostics_explanation),
            fontSize = 14.sp,
            color = Color(0xFF555D6B)
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !running,
            onClick = { refreshKey++ }
        ) {
            Text(stringResource(R.string.diagnostics_repeat))
        }

        if (running) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(stringResource(R.string.diagnostics_running))
        }

        result?.let { report ->
            DiagnosticStepCard(R.string.diagnostics_link_title, report.link)
            DiagnosticStepCard(R.string.diagnostics_config_title, report.config)
            DiagnosticStepCard(R.string.diagnostics_dns_title, report.dns)
            DiagnosticStepCard(R.string.diagnostics_tcp_title, report.tcp)
        }

        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun DiagnosticStepCard(titleRes: Int, step: DiagnosticStep) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                fontSize = 17.sp,
                color = Color(0xFF17191F),
                fontWeight = FontWeight.SemiBold
            )
            val statusText = when (step.status) {
                DiagnosticStatus.OK -> R.string.diagnostics_ok
                DiagnosticStatus.FAILED -> R.string.diagnostics_failed
                DiagnosticStatus.SKIPPED -> R.string.diagnostics_skipped
            }
            val statusColor = when (step.status) {
                DiagnosticStatus.OK -> Color(0xFF168346)
                DiagnosticStatus.FAILED -> Color(0xFFBE3B3B)
                DiagnosticStatus.SKIPPED -> Color(0xFF6B7280)
            }
            Text(
                text = stringResource(statusText),
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(step.message.stringRes()),
                color = Color(0xFF555D6B),
                fontSize = 14.sp
            )
        }
    }
}

private fun DiagnosticMessage.stringRes(): Int = when (this) {
    DiagnosticMessage.LINK_VALID -> R.string.diagnostics_link_ok
    DiagnosticMessage.LINK_INVALID -> R.string.diagnostics_link_bad
    DiagnosticMessage.CONFIG_VALID -> R.string.diagnostics_config_ok
    DiagnosticMessage.CONFIG_INVALID -> R.string.diagnostics_config_bad
    DiagnosticMessage.DNS_RESOLVED -> R.string.diagnostics_dns_ok
    DiagnosticMessage.DNS_FAILED -> R.string.diagnostics_dns_bad
    DiagnosticMessage.DNS_NOT_NEEDED -> R.string.diagnostics_dns_literal
    DiagnosticMessage.TCP_REACHABLE -> R.string.diagnostics_tcp_ok
    DiagnosticMessage.TCP_UNREACHABLE -> R.string.diagnostics_tcp_bad
    DiagnosticMessage.UDP_NOT_TESTED -> R.string.diagnostics_udp_skipped
    DiagnosticMessage.NO_PHYSICAL_NETWORK -> R.string.diagnostics_no_network
    DiagnosticMessage.PREVIOUS_STEP_FAILED -> R.string.diagnostics_previous_failed
}
