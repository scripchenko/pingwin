package com.pingwin.vpn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConnectionListScreen(
    connections: List<SavedConnection>,
    selectedId: String?,
    lockedConnectionId: String?,
    onBack: () -> Unit,
    onSelect: (SavedConnection) -> Unit,
    onDelete: (SavedConnection) -> Unit,
    onAdd: () -> Unit
) {
    var connectionToDelete by remember { mutableStateOf<SavedConnection?>(null) }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = 20.dp
                ),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBack
        ) {
            Text(
                text = "←",
                style =
                    MaterialTheme.typography.headlineMedium
            )
        }

        Text(
            text =
                stringResource(
                    R.string.connections_title
                ),
            fontSize = 30.sp,
            color = Color(0xFF17191F)
        )

        if (connections.isEmpty()) {
            Text(
                text =
                    stringResource(
                        R.string.connections_empty
                    ),
                color = Color(0xFF777D89),
                fontSize = 16.sp
            )
        } else {
            connections.forEach { connection ->
                val profile =
                    runCatching {
                        ConnectionProfileParser.parse(
                            connection.link
                        )
                    }.getOrNull()

                val host =
                    profile?.host
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "—"

                val deleteEnabled =
                    connection.id != lockedConnectionId

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    shape =
                        RoundedCornerShape(20.dp),
                    color =
                        Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(connection)
                                }
                                .padding(
                                    start = 16.dp,
                                    top = 14.dp,
                                    bottom = 14.dp,
                                    end = 8.dp
                                ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            text =
                                if (connection.id == selectedId) {
                                    "●"
                                } else {
                                    "○"
                                },
                            color =
                                if (connection.id == selectedId) {
                                    Color(0xFF2450C8)
                                } else {
                                    Color(0xFF9AA0AA)
                                },
                            fontSize = 22.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.width(14.dp)
                        )

                        Text(
                            text = "${profile?.protocol?.displayName ?: "VPN"} · $host",
                            modifier =
                                Modifier.weight(1f),
                            fontSize = 17.sp,
                            color = Color(0xFF17191F),
                            fontWeight =
                                FontWeight.SemiBold,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        IconButton(
                            enabled = deleteEnabled,
                            onClick = {
                                connectionToDelete = connection
                            }
                        ) {
                            Text(
                                text = "✕",
                                fontSize = 20.sp,
                                color =
                                    if (deleteEnabled) {
                                        Color(0xFFD84A4A)
                                    } else {
                                        Color(0xFFBFC3CB)
                                    }
                            )
                        }
                    }
                }
            }
        }

        Button(
            modifier =
                Modifier.fillMaxWidth(),
            onClick = onAdd
        ) {
            Text(
                stringResource(
                    R.string.connections_add
                )
            )
        }
    }
    connectionToDelete?.let { connection ->
        AlertDialog(
            onDismissRequest = {
                connectionToDelete = null
            },
            title = {
                Text(
                    stringResource(
                        R.string.connections_delete_title
                    )
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.connections_delete_message
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        connectionToDelete = null
                        onDelete(connection)
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.site_routing_delete
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        connectionToDelete = null
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.cancel
                        )
                    )
                }
            }
        )
    }
}
