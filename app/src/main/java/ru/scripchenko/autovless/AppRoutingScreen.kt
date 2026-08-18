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
import androidx.compose.material3.RadioButton
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
            mutableStateOf(
                RoutingSettingsStore.load(context)
            )
        }

    var query by
        remember {
            mutableStateOf("")
        }

    val visibleApps =
        remember(apps, query) {
            val normalizedQuery =
                query.trim().lowercase()

            if (normalizedQuery.isEmpty()) {
                apps
            } else {
                apps.filter { app ->
                    app.label.lowercase().contains(normalizedQuery) ||
                            app.packageName.lowercase().contains(normalizedQuery)
                }
            }
        }

    fun updateRouting(
        newRouting: RoutingSettings
    ) {
        routing = newRouting
        RoutingSettingsStore.save(
            context,
            newRouting
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
            text = "Приложения",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Изменения применятся при следующем подключении VPN.",
            style = MaterialTheme.typography.bodySmall
        )

        RoutingModeRow(
            title = "Все приложения через VPN",
            selected =
                routing.appMode ==
                        AppRoutingMode.ALL_VIA_VPN,
            onClick = {
                updateRouting(
                    routing.copy(
                        appMode =
                            AppRoutingMode.ALL_VIA_VPN
                    )
                )
            }
        )

        RoutingModeRow(
            title = "Только выбранные через VPN",
            selected =
                routing.appMode ==
                        AppRoutingMode.ONLY_SELECTED_VIA_VPN,
            onClick = {
                updateRouting(
                    routing.copy(
                        appMode =
                            AppRoutingMode.ONLY_SELECTED_VIA_VPN
                    )
                )
            }
        )

        RoutingModeRow(
            title = "Все, кроме выбранных",
            selected =
                routing.appMode ==
                        AppRoutingMode.EXCLUDE_SELECTED_FROM_VPN,
            onClick = {
                updateRouting(
                    routing.copy(
                        appMode =
                            AppRoutingMode.EXCLUDE_SELECTED_FROM_VPN
                    )
                )
            }
        )

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
            },
            label = {
                Text("Поиск приложения")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (
            routing.appMode ==
            AppRoutingMode.ALL_VIA_VPN
        ) {
            Text(
                text =
                    "Сейчас весь трафик приложений направляется в VPN.",
                style =
                    MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text =
                    "Выбрано приложений: ${routing.packages.size}",
                style =
                    MaterialTheme.typography.bodyMedium
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
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
                                enabled =
                                    routing.appMode !=
                                            AppRoutingMode.ALL_VIA_VPN
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

                                updateRouting(
                                    routing.copy(
                                        packages =
                                            newPackages
                                    )
                                )
                            }
                            .padding(vertical = 8.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        enabled =
                            routing.appMode !=
                                    AppRoutingMode.ALL_VIA_VPN,
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

                            updateRouting(
                                routing.copy(
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
                                .padding(start = 8.dp)
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

@Composable
private fun RoutingModeRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            text = title,
            modifier =
                Modifier.padding(start = 8.dp)
        )
    }
}
