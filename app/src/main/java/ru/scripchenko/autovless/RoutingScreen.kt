package ru.scripchenko.autovless

import android.content.Context
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

    val selectedApplications =
        routing.packages
            .map { packageName ->
                applicationName(
                    context = context,
                    packageName = packageName
                )
            }
            .sortedBy {
                it.lowercase()
            }

    val selectedSites =
        routing.domains
            .sortedBy {
                it.lowercase()
            }

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
                "Настройте, какой трафик должен идти через VPN. " +
                    "Правила для приложений и сайтов задаются отдельно.",
            style =
                MaterialTheme.typography.bodyMedium
        )

        HorizontalDivider()

        RoutingSection(
            title = "Приложения",
            status =
                applicationStatus(
                    enabled = routing.appEnabled,
                    mode = routing.appMode,
                    selectedCount =
                        selectedApplications.size
                ),
            selectedText =
                applicationSelectionText(
                    enabled = routing.appEnabled,
                    mode = routing.appMode,
                    applications =
                        selectedApplications
                ),
            onClick = onApplications
        )

        HorizontalDivider()

        RoutingSection(
            title = "Сайты",
            status =
                siteStatus(
                    enabled = routing.siteEnabled,
                    mode = routing.siteMode,
                    selectedCount =
                        selectedSites.size
                ),
            selectedText =
                siteSelectionText(
                    enabled = routing.siteEnabled,
                    mode = routing.siteMode,
                    sites = selectedSites
                ),
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
    status: String,
    selectedText: String?,
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
                text = status,
                style =
                    MaterialTheme.typography.bodyMedium
            )

            if (selectedText != null) {
                Text(
                    text = selectedText,
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

private fun applicationStatus(
    enabled: Boolean,
    mode: RoutingMode,
    selectedCount: Int
): String {
    if (!enabled) {
        return "Все приложения идут через VPN"
    }

    if (selectedCount == 0) {
        return when (mode) {
            RoutingMode.ONLY_SELECTED_VIA_VPN ->
                "Через VPN приложения не выбраны"

            RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
                "Все приложения идут через VPN"
        }
    }

    return when (mode) {
        RoutingMode.ONLY_SELECTED_VIA_VPN ->
            "Только выбранные — через VPN"

        RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
            "Выбранные — без VPN"
    }
}

private fun siteStatus(
    enabled: Boolean,
    mode: RoutingMode,
    selectedCount: Int
): String {
    if (!enabled) {
        return "Все сайты идут через VPN"
    }

    if (selectedCount == 0) {
        return when (mode) {
            RoutingMode.ONLY_SELECTED_VIA_VPN ->
                "Через VPN сайты не выбраны"

            RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
                "Все сайты идут через VPN"
        }
    }

    return when (mode) {
        RoutingMode.ONLY_SELECTED_VIA_VPN ->
            "Только выбранные — через VPN"

        RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
            "Выбранные — без VPN"
    }
}

private fun applicationSelectionText(
    enabled: Boolean,
    mode: RoutingMode,
    applications: List<String>
): String? {
    if (
        !enabled ||
        applications.isEmpty()
    ) {
        return null
    }

    val prefix =
        when (mode) {
            RoutingMode.ONLY_SELECTED_VIA_VPN ->
                "Через VPN: "

            RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
                "Без VPN: "
        }

    return prefix +
        applications.joinToString(", ")
}

private fun siteSelectionText(
    enabled: Boolean,
    mode: RoutingMode,
    sites: List<String>
): String? {
    if (
        !enabled ||
        sites.isEmpty()
    ) {
        return null
    }

    val prefix =
        when (mode) {
            RoutingMode.ONLY_SELECTED_VIA_VPN ->
                "Через VPN: "

            RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
                "Без VPN: "
        }

    return prefix +
        sites.joinToString(", ")
}

private fun applicationName(
    context: Context,
    packageName: String
): String =
    runCatching {
        val applicationInfo =
            context.packageManager
                .getApplicationInfo(
                    packageName,
                    0
                )

        context.packageManager
            .getApplicationLabel(applicationInfo)
            .toString()
    }.getOrDefault(packageName)