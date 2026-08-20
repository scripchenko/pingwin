package ru.scripchenko.autovless

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PingwinHomeScreen(
    connection: SavedConnection,
    location: ServerLocation?,
    vpnState: VpnConnectionState,
    pingMs: Int?,
    onPowerClick: () -> Unit,
    onConnectionsClick: () -> Unit,
    onAddQr: () -> Unit,
    onAddClipboard: () -> Unit,
    onAddManual: () -> Unit,
    onRefreshPing: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var addMenuExpanded by remember {
        mutableStateOf(false)
    }

    val connected =
        vpnState == VpnConnectionState.CONNECTED

    val accent =
        if (connected) {
            Color(0xFF2BAE57)
        } else {
            Color(0xFF69B4FF)
        }

    val profile =
        remember(connection.id) {
            runCatching {
                VlessProfile.parse(
                    connection.link
                )
            }.getOrNull()
        }

    val host =
        profile?.host
            ?.takeIf {
                it.isNotBlank()
            }
            ?: "—"

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFFFAFBFD)
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = 20.dp
                )
    ) {
        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Image(
                painter =
                    painterResource(
                        R.drawable.pingwin_icon_blue
                    ),
                contentDescription = "pingwin",
                modifier =
                    Modifier.size(38.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(2.dp)
            )

            Text(
                text = "pingwin",
                color =
                    Color(0xFF10285A),
                fontSize = 29.sp,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Surface(
                shape =
                    RoundedCornerShape(8.dp),
                color =
                    Color(0xFFE9EDF6)
            ) {
                Text(
                    text = "0.1.0",
                    color =
                        Color(0xFF667085),
                    fontSize = 12.sp,
                    modifier =
                        Modifier.padding(
                            horizontal = 7.dp,
                            vertical = 4.dp
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.weight(1f)
            )

            Box {
                Text(
                    text = "+",
                    color =
                        Color(0xFF17325F),
                    fontSize = 36.sp,
                    fontWeight =
                        FontWeight.Light,
                    modifier =
                        Modifier
                            .clickable {
                                addMenuExpanded = true
                            }
                            .padding(
                                horizontal = 8.dp,
                                vertical = 2.dp
                            )
                )

                DropdownMenu(
                    expanded =
                        addMenuExpanded,
                    onDismissRequest = {
                        addMenuExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("QR-код")
                        },
                        onClick = {
                            addMenuExpanded = false
                            onAddQr()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Буфер обмена")
                        },
                        onClick = {
                            addMenuExpanded = false
                            onAddClipboard()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Вручную")
                        },
                        onClick = {
                            addMenuExpanded = false
                            onAddManual()
                        }
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        onConnectionsClick()
                    },
            shape =
                RoundedCornerShape(22.dp),
            color =
                Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border =
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFF858A94)
                )
        ) {
            Row(
                modifier =
                    Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = connection.name,
                    modifier =
                        Modifier.weight(1f),
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis,
                    fontSize = 17.sp,
                    color =
                        Color(0xFF202227)
                )

                Text(
                    text = "▾",
                    fontSize = 20.sp,
                    color =
                        Color(0xFF202227)
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            contentAlignment =
                Alignment.Center
        ) {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(176.dp)
                            .shadow(
                                elevation = 15.dp,
                                shape = CircleShape,
                                ambientColor =
                                    accent.copy(
                                        alpha = 0.40f
                                    ),
                                spotColor =
                                    accent.copy(
                                        alpha = 0.40f
                                    )
                            )
                            .background(
                                Color.White,
                                CircleShape
                            )
                            .border(
                                width = 1.4.dp,
                                color = accent,
                                shape = CircleShape
                            )
                            .clickable(
                                enabled =
                                    vpnState !=
                                        VpnConnectionState.CONNECTING
                            ) {
                                onPowerClick()
                            },
                    contentAlignment =
                        Alignment.Center
                ) {
                    Image(
                        painter =
                            painterResource(
                                if (connected) {
                                    R.drawable.pingwin_icon_green
                                } else {
                                    R.drawable.pingwin_icon_blue
                                }
                            ),
                        contentDescription =
                            "VPN",
                        modifier =
                            Modifier.size(104.dp)
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Text(
                    text =
                        when (vpnState) {
                            VpnConnectionState.DISCONNECTED ->
                                "Нажмите для подключения"

                            VpnConnectionState.CONNECTING ->
                                "Подключение..."

                            VpnConnectionState.CONNECTED ->
                                "Подключено"

                            VpnConnectionState.ERROR ->
                                "Ошибка подключения"
                        },
                    color =
                        Color(0xFF15171C),
                    fontSize = 18.sp,
                    fontWeight =
                        if (connected) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                )

                if (connected) {
                    Spacer(
                        modifier =
                            Modifier.height(9.dp)
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⌁",
                            fontSize = 23.sp,
                            color =
                                Color(0xFF17191F)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(7.dp)
                        )

                        Text(
                            text =
                                pingMs
                                    ?.let {
                                        "$it ms"
                                    }
                                    ?: "— ms",
                            fontSize = 17.sp,
                            color =
                                Color(0xFF17191F),
                            fontWeight =
                                FontWeight.SemiBold
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Text(
                            text = "↻",
                            fontSize = 19.sp,
                            color =
                                Color(0xFF657084),
                            modifier =
                                Modifier
                                    .clickable {
                                        onRefreshPing()
                                    }
                                    .padding(
                                        horizontal = 4.dp,
                                        vertical = 2.dp
                                    )
                        )
                    }
                }
            }
        }

        if (connected) {
            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(24.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 7.dp
                    )
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 14.dp
                            ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            location?.flagEmoji
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "🌐",
                        fontSize = 32.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.width(14.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = connection.name,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis,
                            color =
                                Color(0xFF17191F),
                            fontSize = 15.sp,
                            fontWeight =
                                FontWeight.SemiBold
                        )

                        Text(
                            text = host,
                            color =
                                Color(0xFF697184),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(10.dp)
                    )

                    Text(
                        text = "VLESS",
                        color =
                            Color(0xFF17191F),
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        text = "›",
                        color =
                            Color(0xFF1594F6),
                        fontSize = 34.sp,
                        fontWeight =
                            FontWeight.Light
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(32.dp)
            )
        } else {
            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 14.dp
                    ),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Surface(
                shape =
                    RoundedCornerShape(18.dp),
                color =
                    Color(0xFFE9EEFF)
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal = 18.dp,
                            vertical = 10.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text = "⌂",
                        fontSize = 22.sp,
                        color =
                            Color(0xFF2450C8)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        text = "Главная",
                        color =
                            Color(0xFF2450C8),
                        fontSize = 15.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier =
                    Modifier
                        .clickable {
                            onSettingsClick()
                        }
                        .padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙",
                    fontSize = 22.sp,
                    color =
                        Color(0xFF555B67)
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text = "Настройки",
                    color =
                        Color(0xFF555B67),
                    fontSize = 15.sp
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )
    }
}
