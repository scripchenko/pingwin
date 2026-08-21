package com.pingwin.vpn

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
import androidx.compose.ui.res.stringResource
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
            text =
                stringResource(
                    R.string.routing_title
                ),
            style =
                MaterialTheme.typography.headlineMedium
        )

        Text(
            text =
                stringResource(
                    R.string.routing_intro
                ),
            style =
                MaterialTheme.typography.bodyMedium
        )

        HorizontalDivider()

        RoutingSection(
            title =
                stringResource(
                    R.string.routing_applications
                ),
            status =
                applicationStatus(
                    context = context,
                    enabled = routing.appEnabled,
                    mode = routing.appMode,
                    selectedCount =
                        selectedApplications.size
                ),
            selectedText =
                applicationSelectionText(
                    context = context,
                    enabled = routing.appEnabled,
                    mode = routing.appMode,
                    applications =
                        selectedApplications
                ),
            onClick = onApplications
        )

        HorizontalDivider()

        RoutingSection(
            title =
                stringResource(
                    R.string.routing_sites
                ),
            status =
                siteStatus(
                    context = context,
                    enabled = routing.siteEnabled,
                    mode = routing.siteMode,
                    selectedCount =
                        selectedSites.size
                ),
            selectedText =
                siteSelectionText(
                    context = context,
                    enabled = routing.siteEnabled,
                    mode = routing.siteMode,
                    sites = selectedSites
                ),
            onClick = onSites
        )

        HorizontalDivider()

        Text(
            text =
                stringResource(
                    R.string.routing_changes_reconnect
                ),
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
    context: Context,
    enabled: Boolean,
    mode: RoutingMode,
    selectedCount: Int
): String {
    if (!enabled) {
        return context.getString(
            R.string.routing_all_apps_vpn
        )
    }

    if (selectedCount == 0) {
        return when (mode) {
            RoutingMode.ONLY_SELECTED_VIA_VPN ->
                context.getString(
                    R.string.routing_no_apps_for_vpn
                )

            RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
                context.getString(
                    R.string.routing_all_apps_vpn
                )
        }
    }

    return when (mode) {
        RoutingMode.ONLY_SELECTED_VIA_VPN ->
            context.getString(
                R.string.routing_only_selected_vpn
            )

        RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
            context.getString(
                R.string.routing_selected_without_vpn
            )
    }
}

private fun siteStatus(
    context: Context,
    enabled: Boolean,
    mode: RoutingMode,
    selectedCount: Int
): String {
    if (!enabled) {
        return context.getString(
            R.string.routing_all_sites_vpn
        )
    }

    if (selectedCount == 0) {
        return when (mode) {
            RoutingMode.ONLY_SELECTED_VIA_VPN ->
                context.getString(
                    R.string.routing_no_sites_for_vpn
                )

            RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
                context.getString(
                    R.string.routing_all_sites_vpn
                )
        }
    }

    return when (mode) {
        RoutingMode.ONLY_SELECTED_VIA_VPN ->
            context.getString(
                R.string.routing_only_selected_vpn
            )

        RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
            context.getString(
                R.string.routing_selected_without_vpn
            )
    }
}

private fun applicationSelectionText(
    context: Context,
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

    val applicationsText =
        applications.joinToString(", ")

    return when (mode) {
        RoutingMode.ONLY_SELECTED_VIA_VPN ->
            context.getString(
                R.string.routing_via_vpn_list,
                applicationsText
            )

        RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
            context.getString(
                R.string.routing_without_vpn_list,
                applicationsText
            )
    }
}

private fun siteSelectionText(
    context: Context,
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

    val sitesText =
        sites.joinToString(", ")

    return when (mode) {
        RoutingMode.ONLY_SELECTED_VIA_VPN ->
            context.getString(
                R.string.routing_via_vpn_list,
                sitesText
            )

        RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
            context.getString(
                R.string.routing_without_vpn_list,
                sitesText
            )
    }
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
