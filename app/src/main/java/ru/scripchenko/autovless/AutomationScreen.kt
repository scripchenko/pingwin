package ru.scripchenko.autovless

import android.Manifest
import android.content.Context
import android.net.wifi.WifiManager
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AutomationScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var settings by remember {
        mutableStateOf(
            AutomationSettingsStore.load(
                context
            )
        )
    }

    var newSsid by remember {
        mutableStateOf("")
    }
    var pendingEnable by remember {
        mutableStateOf(false)
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted && pendingEnable) {
                val updated =
                    settings.copy(
                        enabled = true
                    )

                settings = updated

                AutomationSettingsStore.save(
                    context,
                    updated
                )

                AutomationService.sync(
                    context
                )
            } else if (!granted) {
                Toast.makeText(
                    context,
                    "Для доверенных Wi-Fi нужен доступ к местоположению",
                    Toast.LENGTH_LONG
                ).show()
            }

            pendingEnable = false
        }

    fun save(
        newValue: AutomationSettings
    ) {
        settings = newValue

        AutomationSettingsStore.save(
            context,
            newValue
        )

        AutomationService.sync(
            context
        )
    }

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
            text = "Автоматизация",
            fontSize = 30.sp,
            color = Color(0xFF17191F)
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                "Автоматическое управление VPN в зависимости от сети.",
            fontSize = 15.sp,
            lineHeight = 21.sp,
            color = Color(0xFF777D89)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text = "Автоматизация",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1C21)
                )

                Text(
                    text =
                        if (settings.enabled) {
                            "Включена"
                        } else {
                            "Выключена"
                        },
                    fontSize = 14.sp,
                    color = Color(0xFF777D89),
                    modifier =
                        Modifier.padding(
                            top = 3.dp
                        )
                )
            }

            Switch(
                checked = settings.enabled,
                onCheckedChange = { enabled ->
                    if (!enabled) {
                        save(
                            settings.copy(
                                enabled = false
                            )
                        )
                    } else {
                        val granted =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) ==
                                PackageManager.PERMISSION_GRANTED

                        if (granted) {
                            save(
                                settings.copy(
                                    enabled = true
                                )
                            )
                        } else {
                            pendingEnable = true

                            locationPermissionLauncher.launch(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }
                    }
                }
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
        ) {
            Text(
                text = "Когда включать VPN",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF343840)
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            AutomationSwitchRow(
                title = "Мобильный интернет",
                subtitle = "Включать VPN при использовании мобильной сети",
                checked = settings.connectOnMobile,
                enabled = settings.enabled,
                onCheckedChange = {
                    save(
                        settings.copy(
                            connectOnMobile = it
                        )
                    )
                }
            )

            AutomationSwitchRow(
                title = "Недоверенный Wi-Fi",
                subtitle =
                    "Включать VPN в Wi-Fi сетях, которых нет в списке доверенных",
                checked =
                    settings.connectOnUntrustedWifi,
                enabled = settings.enabled,
                onCheckedChange = {
                    save(
                        settings.copy(
                            connectOnUntrustedWifi = it
                        )
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Text(
                text = "Доверенные Wi-Fi",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF343840)
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text =
                    "Например: домашняя или офисная сеть.",
                fontSize = 14.sp,
                color = Color(0xFF777D89)
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                enabled = settings.enabled,
                onClick = {
                    val ssid =
                        currentWifiSsid(
                            context
                        )

                    if (ssid == null) {
                        Toast.makeText(
                            context,
                            "Не удалось определить текущую Wi-Fi сеть",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        save(
                            settings.copy(
                                trustedWifiSsids =
                                    settings
                                        .trustedWifiSsids +
                                        ssid
                            )
                        )

                        Toast.makeText(
                            context,
                            "Сеть «$ssid» добавлена в доверенные",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text("Добавить текущую Wi-Fi сеть")
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            OutlinedTextField(
                value = newSsid,
                onValueChange = {
                    newSsid = it
                },
                enabled = settings.enabled,
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("Название Wi-Fi (SSID)")
                }
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                enabled =
                    settings.enabled &&
                        newSsid.trim().isNotEmpty(),
                onClick = {
                    val ssid =
                        newSsid.trim()

                    if (ssid.isNotEmpty()) {
                        save(
                            settings.copy(
                                trustedWifiSsids =
                                    settings
                                        .trustedWifiSsids +
                                        ssid
                            )
                        )

                        newSsid = ""
                    }
                }
            ) {
                Text("Добавить доверенную сеть")
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (
                settings.trustedWifiSsids.isEmpty()
            ) {
                Text(
                    text =
                        "Доверенных Wi-Fi сетей пока нет.",
                    fontSize = 14.sp,
                    color = Color(0xFF8A8F99)
                )
            } else {
                settings
                    .trustedWifiSsids
                    .sorted()
                    .forEach { ssid ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 8.dp
                                    ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                text = ssid,
                                modifier =
                                    Modifier.weight(1f),
                                fontSize = 17.sp,
                                color =
                                    Color(0xFF1A1C21)
                            )

                            IconButton(
                                enabled =
                                    settings.enabled,
                                onClick = {
                                    save(
                                        settings.copy(
                                            trustedWifiSsids =
                                                settings
                                                    .trustedWifiSsids -
                                                    ssid
                                        )
                                    )
                                }
                            ) {
                                Text(
                                    text = "✕",
                                    fontSize = 20.sp,
                                    color =
                                        Color(0xFFD84A4A)
                                )
                            }
                        }
                    }
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            AutomationSwitchRow(
                title = "Отключать VPN",
                subtitle =
                    "Отключать VPN при подключении к доверенной Wi-Fi сети",
                checked =
                    settings.disconnectOnTrustedWifi,
                enabled = settings.enabled,
                onCheckedChange = {
                    save(
                        settings.copy(
                            disconnectOnTrustedWifi = it
                        )
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(26.dp)
            )
        }
    }
}

@Composable
private fun AutomationSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 10.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                color =
                    if (enabled) {
                        Color(0xFF1A1C21)
                    } else {
                        Color(0xFF9A9EA7)
                    }
            )

            Text(
                text = subtitle,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color =
                    Color(0xFF777D89),
                modifier =
                    Modifier.padding(
                        top = 3.dp
                    )
            )
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange =
                onCheckedChange
        )
    }
}

private fun currentWifiSsid(
    context: Context
): String? =
    runCatching {
        @Suppress("DEPRECATION")
        val wifiManager =
            context.applicationContext
                .getSystemService(
                    Context.WIFI_SERVICE
                ) as WifiManager

        @Suppress("DEPRECATION")
        wifiManager
            .connectionInfo
            ?.ssid
            ?.trim()
            ?.removeSurrounding("\"")
            ?.takeIf {
                it.isNotBlank() &&
                    it != WifiManager.UNKNOWN_SSID
            }
    }.getOrNull()