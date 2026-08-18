package ru.scripchenko.autovless

import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.scripchenko.autovless.ui.theme.AutoVLESSTheme

class MainActivity : ComponentActivity() {

    private enum class Screen {
        HOME,
        APP_ROUTING,
        SITE_ROUTING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AutoVLESSTheme {
                var screen by remember {
                    mutableStateOf(Screen.HOME)
                }

                when (screen) {
                    Screen.APP_ROUTING -> {
                        BackHandler {
                            screen = Screen.HOME
                        }

                        AppRoutingScreen(
                            onBack = {
                                screen = Screen.HOME
                            }
                        )

                        return@AutoVLESSTheme
                    }

                    Screen.SITE_ROUTING -> {
                        BackHandler {
                            screen = Screen.HOME
                        }

                        SiteRoutingScreen(
                            onBack = {
                                screen = Screen.HOME
                            }
                        )

                        return@AutoVLESSTheme
                    }

                    Screen.HOME -> Unit
                }

                var link by remember {
                    mutableStateOf("")
                }

                var status by remember {
                    mutableStateOf("Отключено")
                }

                var pendingConfig by remember {
                    mutableStateOf<String?>(null)
                }

                val vpnPermissionLauncher =
                    rememberLauncherForActivityResult(
                        contract =
                            ActivityResultContracts.StartActivityForResult()
                    ) { result ->

                        if (result.resultCode == RESULT_OK) {
                            val config =
                                pendingConfig

                            if (config != null) {
                                AutoVlessVpnService.start(
                                    this@MainActivity,
                                    config
                                )

                                status =
                                    "Запуск VPN..."
                            }
                        } else {
                            status =
                                "Разрешение VPN не предоставлено"
                        }

                        pendingConfig = null
                    }

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "AutoVLESS",
                        style =
                            MaterialTheme.typography.headlineMedium
                    )

                    OutlinedTextField(
                        value = link,
                        onValueChange = {
                            link = it
                        },
                        label = {
                            Text("VLESS-ссылка")
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        minLines = 4
                    )

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            try {
                                val profile =
                                    VlessProfile.parse(link)

                                val routing =
                                    RoutingSettingsStore.load(
                                        this@MainActivity
                                    )

                                val config =
                                    SingBoxConfigBuilder.build(
                                        profile,
                                        routing
                                    )

                                val validation =
                                    LibboxValidator.validate(
                                        config
                                    )

                                if (validation.isFailure) {
                                    status =
                                        "Ошибка конфигурации: " +
                                                (
                                                    validation
                                                        .exceptionOrNull()
                                                        ?.message
                                                        ?: "неизвестная ошибка"
                                                )

                                    return@Button
                                }

                                val permissionIntent =
                                    VpnService.prepare(
                                        this@MainActivity
                                    )

                                if (permissionIntent != null) {
                                    pendingConfig =
                                        config

                                    vpnPermissionLauncher.launch(
                                        permissionIntent
                                    )
                                } else {
                                    AutoVlessVpnService.start(
                                        this@MainActivity,
                                        config
                                    )

                                    status =
                                        "Запуск VPN..."
                                }

                            } catch (e: Exception) {
                                status =
                                    "Ошибка: ${e.message}"
                            }
                        }
                    ) {
                        Text("Подключить")
                    }

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            AutoVlessVpnService.stop(
                                this@MainActivity
                            )

                            status =
                                "Отключено"
                        }
                    ) {
                        Text("Отключить")
                    }

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            screen =
                                Screen.APP_ROUTING
                        }
                    ) {
                        Text("Приложения")
                    }

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            screen =
                                Screen.SITE_ROUTING
                        }
                    ) {
                        Text("Сайты")
                    }

                    Text(
                        text = "Статус: $status"
                    )
                }
            }
        }
    }
}
