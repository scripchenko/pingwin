package com.pingwin.vpn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    connectionCount: Int,
    onHomeClick: () -> Unit,
    onGeneralClick: () -> Unit,
    onRoutingClick: () -> Unit,
    onAutomationClick: () -> Unit,
    onConnectionsClick: () -> Unit,
    onLogsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = 22.dp
                )
    ) {
        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        Text(
            text =
                stringResource(
                    R.string.settings_title
                ),
            fontSize = 31.sp,
            fontWeight =
                FontWeight.Normal,
            color =
                Color(0xFF17191F)
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        SettingsRow(
            icon = "◉",
            title =
                stringResource(
                    R.string.settings_general
                ),
            subtitle =
                stringResource(
                    R.string.settings_general_subtitle
                ),
            onClick = onGeneralClick
        )

        SettingsRow(
            icon = "↗",
            title =
                stringResource(
                    R.string.settings_routing
                ),
            subtitle =
                stringResource(
                    R.string.settings_routing_subtitle
                ),
            onClick = onRoutingClick
        )

        SettingsRow(
            icon = "⚡",
            title =
                stringResource(
                    R.string.settings_automation
                ),
            subtitle =
                stringResource(
                    R.string.settings_automation_subtitle
                ),
            onClick = onAutomationClick
        )

        SettingsRow(
            icon = "▤",
            title =
                stringResource(
                    R.string.settings_connections
                ),
            subtitle =
                pluralStringResource(
                    id = R.plurals.saved_servers,
                    count = connectionCount,
                    connectionCount
                ),
            onClick = onConnectionsClick
        )

        SettingsRow(
            icon = "≡",
            title =
                stringResource(
                    R.string.settings_logs
                ),
            subtitle =
                stringResource(
                    R.string.settings_logs_subtitle
                ),
            onClick = onLogsClick
        )

        SettingsRow(
            icon = "ⓘ",
            title =
                stringResource(
                    R.string.settings_about
                ),
            subtitle = "pingwin ${BuildConfig.VERSION_NAME}",
            onClick = onAboutClick
        )

        Spacer(
            modifier =
                Modifier.weight(1f)
        )

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
            Row(
                modifier =
                    Modifier
                        .clickable {
                            onHomeClick()
                        }
                        .padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = "⌂",
                    fontSize = 22.sp,
                    color =
                        Color(0xFF555B67)
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text =
                        stringResource(
                            R.string.settings_home
                        ),
                    color =
                        Color(0xFF555B67),
                    fontSize = 15.sp
                )
            }

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
                        text = "⚙",
                        fontSize = 22.sp,
                        color =
                            Color(0xFF2450C8)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        text =
                            stringResource(
                                R.string.settings_title
                            ),
                        color =
                            Color(0xFF2450C8),
                        fontSize = 15.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(
                    vertical = 15.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 25.sp,
            color =
                Color(0xFF50545E),
            modifier =
                Modifier.width(50.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontSize = 19.sp,
                color =
                    Color(0xFF1A1C21)
            )

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color =
                    Color(0xFF777D89)
            )
        }

        Text(
            text = "›",
            fontSize = 31.sp,
            color =
                Color(0xFF8A8F99)
        )
    }
}
