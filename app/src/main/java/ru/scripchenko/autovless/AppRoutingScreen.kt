package ru.scripchenko.autovless

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AppRoutingScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val apps =
        remember {
            InstalledAppsLoader.load(context)
        }

    var routing by
        remember {
            val loaded =
                RoutingSettingsStore.load(context)

            val normalized =
                if (loaded.appMode == AppRoutingMode.EXCLUDE_SELECTED_FROM_VPN) {
                    val allPackages =
                        apps.map { it.packageName }.toSet()

                    loaded.copy(
                        appMode = AppRoutingMode.ONLY_SELECTED_VIA_VPN,
                        packages = allPackages - loaded.packages
                    )
                } else {
                    loaded
                }

            if (normalized != loaded) {
                RoutingSettingsStore.save(
                    context,
                    normalized
                )
            }

            mutableStateOf(normalized)
        }

    var query by
        remember {
            mutableStateOf("")
        }

    val allViaVpn =
        routing.appMode ==
                AppRoutingMode.ALL_VIA_VPN

    val visibleApps =
        remember(
            apps,
            query
        ) {
            val normalizedQuery =
                query.trim().lowercase()

            if (normalizedQuery.isEmpty()) {
                apps
            } else {
                apps.filter { app ->
                    app.label
                        .lowercase()
                        .contains(normalizedQuery) ||
                            app.packageName
                                .lowercase()
                                .contains(normalizedQuery)
                }
            }
        }

    fun saveRouting(
        updated: RoutingSettings
    ) {
        routing = updated

        RoutingSettingsStore.save(
            context,
            updated
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onBack
        ) {
            Text("Назад")
        }

        Text(
            text = "Приложения через VPN",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Text(
            text =
                "По умолчанию через VPN идут только выбранные приложения. " +
                        "Остальные полностью используют обычное подключение.",
            style =
                MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        saveRouting(
                            routing.copy(
                                appMode =
                                    if (allViaVpn) {
                                        AppRoutingMode.ONLY_SELECTED_VIA_VPN
                                    } else {
                                        AppRoutingMode.ALL_VIA_VPN
                                    }
                            )
                        )
                    },
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = "Все приложения через VPN",
                modifier =
                    Modifier.weight(1f)
            )

            Switch(
                checked = allViaVpn,
                onCheckedChange = { checked ->
                    saveRouting(
                        routing.copy(
                            appMode =
                                if (checked) {
                                    AppRoutingMode.ALL_VIA_VPN
                                } else {
                                    AppRoutingMode.ONLY_SELECTED_VIA_VPN
                                }
                        )
                    )
                }
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
            },
            label = {
                Text("Поиск приложения")
            },
            singleLine = true,
            modifier =
                Modifier.fillMaxWidth()
        )

        Text(
            text =
                if (allViaVpn) {
                    "Через VPN идут все приложения"
                } else {
                    "Выбрано приложений: ${routing.packages.size}"
                },
            style =
                MaterialTheme.typography.bodyMedium
        )

        Text(
            text =
                "Изменения применятся при следующем подключении VPN.",
            style =
                MaterialTheme.typography.bodySmall
        )

        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
        ) {
            items(
                items = visibleApps,
                key = {
                    it.packageName
                }
            ) { app ->

                val checked =
                    app.packageName in
                            routing.packages

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = !allViaVpn
                            ) {
                                val newPackages =
                                    routing.packages
                                        .toMutableSet()

                                if (checked) {
                                    newPackages.remove(
                                        app.packageName
                                    )
                                } else {
                                    newPackages.add(
                                        app.packageName
                                    )
                                }

                                saveRouting(
                                    routing.copy(
                                        appMode =
                                            AppRoutingMode.ONLY_SELECTED_VIA_VPN,
                                        packages =
                                            newPackages
                                    )
                                )
                            }
                            .padding(
                                vertical = 8.dp
                            ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked =
                            if (allViaVpn) true else checked,
                        enabled = !allViaVpn,
                        onCheckedChange = { isChecked ->
                            val newPackages =
                                routing.packages
                                    .toMutableSet()

                            if (isChecked) {
                                newPackages.add(
                                    app.packageName
                                )
                            } else {
                                newPackages.remove(
                                    app.packageName
                                )
                            }

                            saveRouting(
                                routing.copy(
                                    appMode =
                                        AppRoutingMode.ONLY_SELECTED_VIA_VPN,
                                    packages =
                                        newPackages
                                )
                            )
                        }
                    )

                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(
                                    start = 8.dp
                                )
                    ) {
                        Text(
                            text = app.label,
                            style =
                                MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = app.packageName,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }

                HorizontalDivider()
            }
        }
    }
}
