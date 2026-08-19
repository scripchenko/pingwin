package ru.scripchenko.autovless

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = 20.dp
                )
    ) {
        IconButton(
            onClick = onBack
        ) {
            Text(
                text = "←",
                fontSize = 30.sp,
                color = Color(0xFF17191F)
            )
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "О программе",
            fontSize = 30.sp,
            color = Color(0xFF17191F)
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Text(
            text = "pingwin",
            fontSize = 38.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF17191F)
        )

        Text(
            text = "Версия 0.1.0",
            fontSize = 15.sp,
            color = Color(0xFF777D89),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text =
                "Простой VPN-клиент для подключения к VLESS-серверам " +
                    "с гибкой маршрутизацией приложений и сайтов.",
            fontSize = 17.sp,
            lineHeight = 25.sp,
            color = Color(0xFF343840)
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        AboutRow(
            title = "Версия",
            value = "0.1.0"
        )

        AboutRow(
            title = "Ядро",
            value = "sing-box"
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "© 2026 pingwin",
            fontSize = 14.sp,
            color = Color(0xFF8A8F99),
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun AboutRow(
    title: String,
    value: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            color = Color(0xFF1A1C21)
        )

        Spacer(
            modifier = Modifier.width(20.dp)
        )

        Text(
            text = value,
            fontSize = 16.sp,
            color = Color(0xFF777D89)
        )
    }
}
