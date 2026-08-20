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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
            mutableStateOf(
                RoutingSettingsStore.load(context)
            )
        }

    var query by
        remember {
            mutableStateOf("")
        }

    var modeMenuExpanded by
        remember {
            mutableStateOf(false)
        }

    var hideSystemApps by
        remember {
            mutableStateOf(true)
        }

    fun save(
        updated: RoutingSettings
    ) {
        routing = updated
        RoutingSettingsStore.save(
            context,
            updated
        )
    }

    val filteredApps =
        remember(
            apps,
            query,
            hideSystemApps,
            routing.packages
        ) {
            val normalized =
                query.trim().lowercase()

            apps
                .asSequence()
                .filter { app ->
                    !hideSystemApps ||
                        !app.isSystem ||
                        app.packageName in routing.packages
                }
                .filter { app ->
                    normalized.isEmpty() ||
                        app.label
                            .lowercase()
                            .contains(normalized) ||
                        app.packageName
                            .lowercase()
                            .contains(normalized)
                }
                .sortedWith(
                    compareByDescending<InstalledApp> {
                        it.packageName in routing.packages
                    }.thenBy {
                        it.label.lowercase()
                    }
                )
                .toList()
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.material3.IconButton(
            onClick = onBack
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = "Маршрутизация приложений",
                modifier =
                    Modifier.weight(1f),
                style =
                    MaterialTheme.typography.headlineSmall
            )

            Switch(
                checked = routing.appEnabled,
                onCheckedChange = {
                    save(
                        routing.copy(
                            appEnabled = it
                        )
                    )
                }
            )
        }

        Text(
            text =
                if (routing.appEnabled) {
                    "Включено"
                } else {
                    "Отключено. Все приложения работают через VPN."
                },
            style =
                MaterialTheme.typography.bodyMedium
        )

        Column {
            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(8.dp),
                onClick = {
                    modeMenuExpanded = true
                }
            ) {
                Text(
                    when (routing.appMode) {
                        RoutingMode.ONLY_SELECTED_VIA_VPN ->
                            "Только приложения из списка — через VPN"

                        RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
                            "Приложения из списка — без VPN"
                    }
                )
            }

            DropdownMenu(
                expanded = modeMenuExpanded,
                onDismissRequest = {
                    modeMenuExpanded = false
                }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Только приложения из списка должны работать через VPN"
                        )
                    },
                    onClick = {
                        modeMenuExpanded = false
                        save(
                            routing.copy(
                                appMode =
                                    RoutingMode.ONLY_SELECTED_VIA_VPN
                            )
                        )
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Приложения из списка не должны работать через VPN"
                        )
                    },
                    onClick = {
                        modeMenuExpanded = false
                        save(
                            routing.copy(
                                appMode =
                                    RoutingMode.EXCLUDE_SELECTED_FROM_VPN
                            )
                        )
                    }
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        hideSystemApps =
                            !hideSystemApps
                    },
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = "Скрыть системные приложения",
                modifier =
                    Modifier.weight(1f),
                style =
                    MaterialTheme.typography.bodyMedium
            )

            Switch(
                checked = hideSystemApps,
                onCheckedChange = {
                    hideSystemApps = it
                }
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
            },
            modifier =
                Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text("Поиск приложения")
            }
        )

        Text(
            text =
                "Выбрано: ${routing.packages.size}",
            style =
                MaterialTheme.typography.bodySmall
        )

        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
        ) {
            items(
                items = filteredApps,
                key = {
                    it.packageName
                }
            ) { app ->
                val checked =
                    app.packageName in routing.packages

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updated =
                                    routing.packages
                                        .toMutableSet()

                                if (checked) {
                                    updated.remove(
                                        app.packageName
                                    )
                                } else {
                                    updated.add(
                                        app.packageName
                                    )
                                }

                                save(
                                    routing.copy(
                                        packages = updated
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
                        checked = checked,
                        onCheckedChange = { enabled ->
                            val updated =
                                routing.packages
                                    .toMutableSet()

                            if (enabled) {
                                updated.add(
                                    app.packageName
                                )
                            } else {
                                updated.remove(
                                    app.packageName
                                )
                            }

                            save(
                                routing.copy(
                                    packages = updated
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
