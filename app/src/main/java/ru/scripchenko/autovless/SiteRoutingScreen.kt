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
fun SiteRoutingScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var routing by
        remember {
            mutableStateOf(
                RoutingSettingsStore.load(context)
            )
        }

    var domainInput by
        remember {
            mutableStateOf("")
        }

    var modeMenuExpanded by
        remember {
            mutableStateOf(false)
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

    fun normalizeDomain(
        value: String
    ): String =
        value
            .trim()
            .lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore("#")
            .removePrefix("*.")
            .removePrefix(".")
            .removeSuffix(".")

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Назад",
            modifier =
                Modifier.clickable(onClick = onBack),
            style =
                MaterialTheme.typography.titleMedium
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = "Маршрутизация сайтов",
                modifier =
                    Modifier.weight(1f),
                style =
                    MaterialTheme.typography.headlineSmall
            )

            Switch(
                checked = routing.siteEnabled,
                onCheckedChange = {
                    save(
                        routing.copy(
                            siteEnabled = it
                        )
                    )
                }
            )
        }

        Text(
            text =
                if (routing.siteEnabled) {
                    "Правила действуют во всех приложениях и браузерах."
                } else {
                    "Отключено. Все сайты открываются через VPN."
                },
            style =
                MaterialTheme.typography.bodyMedium
        )

        Column {
            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick = {
                    modeMenuExpanded = true
                }
            ) {
                Text(
                    when (routing.siteMode) {
                        RoutingMode.ONLY_SELECTED_VIA_VPN ->
                            "Только адреса из списка — через VPN"

                        RoutingMode.EXCLUDE_SELECTED_FROM_VPN ->
                            "Адреса из списка — без VPN"
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
                            "Только адреса из списка должны открываться через VPN"
                        )
                    },
                    onClick = {
                        modeMenuExpanded = false
                        save(
                            routing.copy(
                                siteMode =
                                    RoutingMode.ONLY_SELECTED_VIA_VPN
                            )
                        )
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Адреса из списка не должны открываться через VPN"
                        )
                    },
                    onClick = {
                        modeMenuExpanded = false
                        save(
                            routing.copy(
                                siteMode =
                                    RoutingMode.EXCLUDE_SELECTED_FROM_VPN
                            )
                        )
                    }
                )
            }
        }

        OutlinedTextField(
            value = domainInput,
            onValueChange = {
                domainInput = it
            },
            modifier =
                Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text("Адрес, например youtube.com")
            }
        )

        OutlinedButton(
            modifier =
                Modifier.fillMaxWidth(),
            enabled =
                domainInput.isNotBlank(),
            onClick = {
                val domain =
                    normalizeDomain(domainInput)

                if (domain.isNotEmpty()) {
                    save(
                        routing.copy(
                            domains =
                                routing.domains + domain
                        )
                    )
                    domainInput = ""
                }
            }
        ) {
            Text("Добавить")
        }

        Text(
            text =
                "Адресов в списке: ${routing.domains.size}",
            style =
                MaterialTheme.typography.bodySmall
        )

        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
        ) {
            items(
                items =
                    routing.domains
                        .toList()
                        .sorted(),
                key = { it }
            ) { domain ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 12.dp
                            ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text = domain,
                        modifier =
                            Modifier.weight(1f)
                    )

                    Text(
                        text = "Удалить",
                        modifier =
                            Modifier.clickable {
                                save(
                                    routing.copy(
                                        domains =
                                            routing.domains - domain
                                    )
                                )
                            },
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }

                HorizontalDivider()
            }
        }
    }
}
