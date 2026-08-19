package ru.scripchenko.autovless

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun RoutingScreen(
    onBack: () -> Unit,
    onApplications: () -> Unit,
    onSites: () -> Unit
) {
    val context = LocalContext.current

    val routing =
        RoutingSettingsStore.load(context)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        androidx.compose.material3.IconButton(
            onClick = onBack
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Text(
            text = "Маршрутизация",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Text(
            text =
                "По умолчанию весь трафик идёт через VPN. " +
                    "Здесь можно отдельно настроить приложения и сайты.",
            style =
                MaterialTheme.typography.bodyMedium
        )

        HorizontalDivider()

        RoutingSection(
            title = "Приложения",
            enabled = routing.appEnabled,
            mode = routing.appMode,
            count = routing.packages.size,
            itemWord = "приложений",
            onClick = onApplications
        )

        HorizontalDivider()

        RoutingSection(
            title = "Сайты",
            enabled = routing.siteEnabled,
            mode = routing.siteMode,
            count = routing.domains.size,
            itemWord = "сайтов",
            onClick = onSites
        )

        HorizontalDivider()

        Text(
            text =
                "Изменения применяются после переподключения VPN.",
            style =
                MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun RoutingSection(
    title: String,
    enabled: Boolean,
    mode: RoutingMode,
    count: Int,
    itemWord: String,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            modifier =
                Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleLarge
            )

            Text(
                text =
                    if (!enabled) {
                        "Выключено"
                    } else {
                        when (mode) {
                            RoutingMode.ONLY_SELECTED_VIA_VPN ->
                                "Только выбранные — через VPN"

                            RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
                                "Выбранные — без VPN"
                        }
                    },
                style =
                    MaterialTheme.typography.bodyMedium
            )

            if (enabled) {
                Text(
                    text = "Выбрано $itemWord: $count",
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            text = "›",
            style =
                MaterialTheme.typography.headlineSmall
        )
    }
}
