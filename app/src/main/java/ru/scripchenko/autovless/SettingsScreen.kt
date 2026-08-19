package ru.scripchenko.autovless

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    connectionCount: Int,
    onHomeClick: () -> Unit,
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
            text = "Настройки",
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
            icon = "↗",
            title = "Маршрутизация",
            subtitle = "Приложения и сайты",
            onClick = onRoutingClick
        )

        SettingsRow(
            icon = "⚡",
            title = "Автоматизация",
            subtitle = "Автовключение и правила",
            onClick = onAutomationClick
        )

        SettingsRow(
            icon = "▤",
            title = "Подключения",
            subtitle =
                connectionCountText(
                    connectionCount
                ),
            onClick = onConnectionsClick
        )

        SettingsRow(
            icon = "≡",
            title = "Логи",
            subtitle = "Диагностика подключения",
            onClick = onLogsClick
        )

        SettingsRow(
            icon = "ⓘ",
            title = "О программе",
            subtitle = "pingwin 0.1.0",
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
                    text = "Главная",
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
                        text = "Настройки",
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

private fun connectionCountText(
    count: Int
): String =
    when {
        count % 10 == 1 &&
            count % 100 != 11 ->
            "$count сохранённый сервер"

        count % 10 in 2..4 &&
            count % 100 !in 12..14 ->
            "$count сохранённых сервера"

        else ->
            "$count сохранённых серверов"
    }
