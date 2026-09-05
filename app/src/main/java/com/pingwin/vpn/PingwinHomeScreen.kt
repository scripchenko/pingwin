package com.pingwin.vpn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
    routingSettings: RoutingSettings,
    automationSettings: AutomationSettings,
    onPowerClick: () -> Unit,
    onConnectionsClick: () -> Unit,
    onAppRoutingClick: () -> Unit,
    onSiteRoutingClick: () -> Unit,
    onAutomationClick: () -> Unit,
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
            Color(0xFF4C8DFF)
        }

    val profile =
        remember(connection.id) {
            runCatching {
                ConnectionProfileParser.parse(
                    connection.link
                )
            }.getOrNull()
        }

    val host =
        profile?.host
            ?.takeIf {
                it.isNotBlank()
            }

    val defaultConnectionName =
        if (host != null) {
            "${profile?.protocol?.displayName ?: "VPN"} · $host"
        } else {
            "VLESS"
        }

    val displayConnectionName =
        if (
            connection.name == defaultConnectionName ||
            connection.name == "VLESS"
        ) {
            stringResource(
                R.string.home_primary_server
            )
        } else {
            connection.name
        }

    val locationName =
        location?.countryName
            ?.takeIf {
                it.isNotBlank()
            }
            ?: stringResource(
                R.string.home_location_unknown
            )

    val appsSubtitle =
        if (routingSettings.appEnabled) {
            pluralStringResource(
                R.plurals.home_apps_count,
                routingSettings.packages.size,
                routingSettings.packages.size
            )
        } else {
            stringResource(
                R.string.home_routing_disabled
            )
        }

    val sitesSubtitle =
        if (routingSettings.siteEnabled) {
            pluralStringResource(
                R.plurals.home_sites_count,
                routingSettings.domains.size,
                routingSettings.domains.size
            )
        } else {
            stringResource(
                R.string.home_routing_disabled
            )
        }

    val wifiAutomationEnabled =
        automationSettings.connectOnUntrustedWifi ||
            automationSettings.disconnectOnTrustedWifi

    val automationSubtitle =
        if (!automationSettings.enabled) {
            stringResource(
                R.string.automation_disabled
            )
        } else {
            when {
                wifiAutomationEnabled &&
                    automationSettings.connectOnMobile ->
                    stringResource(
                        R.string.home_automation_wifi_mobile
                    )

                wifiAutomationEnabled ->
                    stringResource(
                        R.string.home_automation_wifi
                    )

                automationSettings.connectOnMobile ->
                    stringResource(
                        R.string.home_automation_mobile
                    )

                else ->
                    stringResource(
                        R.string.automation_enabled
                    )
            }
        }

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
                    text =
                        BuildConfig.VERSION_NAME,
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
                            Text(
                                stringResource(
                                    R.string.home_add_qr
                                )
                            )
                        },
                        onClick = {
                            addMenuExpanded = false
                            onAddQr()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(
                                    R.string.home_add_clipboard
                                )
                            )
                        },
                        onClick = {
                            addMenuExpanded = false
                            onAddClipboard()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(
                                    R.string.home_add_manual
                                )
                            )
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
            shadowElevation = 3.dp
        ) {
            Row(
                modifier =
                    Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 15.dp
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
                    fontSize = 28.sp
                )

                Spacer(
                    modifier =
                        Modifier.width(13.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            displayConnectionName,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis,
                        fontSize = 17.sp,
                        color =
                            Color(0xFF17191F),
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(2.dp)
                    )

                    Text(
                        text = locationName,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        color =
                            Color(0xFF697184)
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text = "⌄",
                    fontSize = 22.sp,
                    color =
                        Color(0xFF455064)
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(26.dp)
        )

        Box(
            modifier =
                Modifier.fillMaxWidth(),
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
                            .size(184.dp)
                            .shadow(
                                elevation = 15.dp,
                                shape = CircleShape,
                                ambientColor =
                                    accent.copy(
                                        alpha = 0.35f
                                    ),
                                spotColor =
                                    accent.copy(
                                        alpha = 0.35f
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
                            Modifier.size(110.dp)
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )

                Text(
                    text =
                        when (vpnState) {
                            VpnConnectionState.DISCONNECTED ->
                                stringResource(
                                    R.string.home_status_disconnected
                                )

                            VpnConnectionState.CONNECTING ->
                                stringResource(
                                    R.string.home_status_connecting
                                )

                            VpnConnectionState.CONNECTED ->
                                stringResource(
                                    R.string.home_status_connected
                                )

                            VpnConnectionState.ERROR ->
                                stringResource(
                                    R.string.home_status_error
                                )
                        },
                    color =
                        Color(0xFF15171C),
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text = "ϟ",
                        fontSize = 21.sp,
                        color =
                            if (connected) {
                                Color(0xFF2BAE57)
                            } else {
                                Color(0xFF8B94A5)
                            }
                    )

                    Spacer(
                        modifier =
                            Modifier.width(7.dp)
                    )

                    Text(
                        text =
                            if (connected) {
                                pingMs
                                    ?.let {
                                        "$it ms"
                                    }
                                    ?: "— ms"
                            } else {
                                "— ms"
                            },
                        fontSize = 17.sp,
                        color =
                            if (connected) {
                                Color(0xFF17191F)
                            } else {
                                Color(0xFF8B94A5)
                            },
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
                            if (connected) {
                                Color(0xFF657084)
                            } else {
                                Color(0xFFB0B6C1)
                            },
                        modifier =
                            Modifier
                                .clickable(
                                    enabled = connected
                                ) {
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

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(22.dp),
            color =
                Color.White,
            shadowElevation = 4.dp
        ) {
            Column {
                HomeInfoRow(
                    icon = "⇄",
                    iconColor =
                        Color(0xFF2F6FED),
                    iconBackground =
                        Color(0xFFEAF1FF),
                    title =
                        stringResource(
                            R.string.home_app_rules
                        ),
                    subtitle =
                        appsSubtitle,
                    onClick =
                        onAppRoutingClick
                )

                HomeDivider()

                HomeInfoRow(
                    icon = "◎",
                    iconColor =
                        Color(0xFF2BAE57),
                    iconBackground =
                        Color(0xFFEAF8EF),
                    title =
                        stringResource(
                            R.string.home_site_rules
                        ),
                    subtitle =
                        sitesSubtitle,
                    onClick =
                        onSiteRoutingClick
                )


            }
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(26.dp),
            color =
                Color.White,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Surface(
                    modifier =
                        Modifier.weight(1f),
                    shape =
                        RoundedCornerShape(18.dp),
                    color =
                        Color(0xFFE9EEFF)
                ) {
                    Row(
                        modifier =
                            Modifier.padding(
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
                                Color(0xFF2450C8)
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
                            .weight(1f)
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
                        text =
                            stringResource(
                                R.string.settings_title
                            ),
                        color =
                            Color(0xFF555B67),
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )
    }
}

@Composable
private fun HomeInfoRow(
    icon: String,
    iconColor: Color,
    iconBackground: Color,
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
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Surface(
            shape =
                RoundedCornerShape(11.dp),
            color =
                iconBackground
        ) {
            Box(
                modifier =
                    Modifier.size(40.dp),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = icon,
                    color = iconColor,
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }

        Spacer(
            modifier =
                Modifier.width(13.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color =
                    Color(0xFF17191F),
                fontSize = 15.sp,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )

            Text(
                text = subtitle,
                color =
                    Color(0xFF697184),
                fontSize = 13.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Text(
            text = "›",
            color =
                Color(0xFF657084),
            fontSize = 26.sp,
            fontWeight =
                FontWeight.Light
        )
    }
}

@Composable
private fun HomeDivider() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 69.dp,
                    end = 16.dp
                )
                .height(1.dp)
                .background(
                    Color(0xFFE9ECF1)
                )
    )
}
