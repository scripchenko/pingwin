package ru.scripchenko.autovless

import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.scripchenko.autovless.ui.theme.AutoVLESSTheme

class MainActivity : ComponentActivity() {

    private enum class Screen {
        HOME,
        ADD_CONNECTION,
        CONNECTIONS,
        ROUTING,
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

                var selectedConnection by remember {
                    mutableStateOf(
                        ConnectionStore.selected(
                            this@MainActivity
                        )
                    )
                }

                val vpnState by
                    VpnStatus.state.collectAsState()

                when (screen) {
                    Screen.ADD_CONNECTION -> {
                        BackHandler {
                            screen = Screen.HOME
                        }

                        AddConnectionScreen(
                            onBack = {
                                screen = Screen.HOME
                            },
                            onAdded = {
                                selectedConnection =
                                    ConnectionStore.selected(
                                        this@MainActivity
                                    )

                                screen = Screen.HOME
                            }
                        )

                        return@AutoVLESSTheme
                    }

                    Screen.CONNECTIONS -> {
                        BackHandler {
                            screen = Screen.HOME
                        }

                        ConnectionListScreen(
                            connections =
                                ConnectionStore.loadAll(
                                    this@MainActivity
                                ),
                            selectedId =
                                selectedConnection?.id,
                            onBack = {
                                screen = Screen.HOME
                            },
                            onSelect = { connection ->
                                ConnectionStore.select(
                                    this@MainActivity,
                                    connection.id
                                )

                                selectedConnection =
                                    connection

                                screen = Screen.HOME
                            },
                            onAdd = {
                                screen =
                                    Screen.ADD_CONNECTION
                            }
                        )

                        return@AutoVLESSTheme
                    }

                    Screen.ROUTING -> {
                        BackHandler {
                            screen = Screen.HOME
                        }

                        RoutingScreen(
                            onBack = {
                                screen = Screen.HOME
                            },
                            onApplications = {
                                screen = Screen.APP_ROUTING
                            },
                            onSites = {
                                screen = Screen.SITE_ROUTING
                            }
                        )

                        return@AutoVLESSTheme
                    }

                    Screen.APP_ROUTING -> {
                        BackHandler {
                            screen = Screen.ROUTING
                        }

                        AppRoutingScreen(
                            onBack = {
                                screen = Screen.ROUTING
                            }
                        )

                        return@AutoVLESSTheme
                    }

                    Screen.SITE_ROUTING -> {
                        BackHandler {
                            screen = Screen.ROUTING
                        }

                        SiteRoutingScreen(
                            onBack = {
                                screen = Screen.ROUTING
                            }
                        )

                        return@AutoVLESSTheme
                    }

                    Screen.HOME -> Unit
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
                            pendingConfig?.let {
                                AutoVlessVpnService.start(
                                    this@MainActivity,
                                    it
                                )
                            }
                        } else {
                            VpnStatus.set(
                                VpnConnectionState.ERROR
                            )
                        }

                        pendingConfig = null
                    }

                fun startVpnConnection(
                    connection: SavedConnection
                ) {
                    try {
                        val profile =
                            VlessProfile.parse(
                                connection.link
                            )

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
                            VpnStatus.set(
                                VpnConnectionState.ERROR
                            )
                            return
                        }

                        val permissionIntent =
                            VpnService.prepare(
                                this@MainActivity
                            )

                        if (permissionIntent != null) {
                            pendingConfig = config

                            vpnPermissionLauncher.launch(
                                permissionIntent
                            )
                        } else {
                            AutoVlessVpnService.start(
                                this@MainActivity,
                                config
                            )
                        }
                    } catch (_: Exception) {
                        VpnStatus.set(
                            VpnConnectionState.ERROR
                        )
                    }
                }

                Column(
                    modifier =
                        Modifier.fillMaxSize(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(24.dp)
                    )

                    Text(
                        text = "AutoVLESS",
                        style =
                            MaterialTheme.typography.headlineMedium
                    )

                    val connection =
                        selectedConnection

                    if (connection == null) {
                        Column(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text =
                                    "Подключение ещё не добавлено",
                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Button(
                                onClick = {
                                    screen =
                                        Screen.ADD_CONNECTION
                                }
                            ) {
                                Text(
                                    "Добавить подключение"
                                )
                            }
                        }

                        return@Column
                    }

                    val profile =
                        remember(connection.id) {
                            runCatching {
                                VlessProfile.parse(
                                    connection.link
                                )
                            }.getOrNull()
                        }

                    var serverLocation by
                        remember(connection.id) {
                            mutableStateOf<ServerLocation?>(null)
                        }

                    LaunchedEffect(
                        connection.id,
                        profile?.host
                    ) {
                        val host =
                            profile?.host
                                ?.takeIf {
                                    it.isNotBlank()
                                }

                        serverLocation =
                            if (host == null) {
                                null
                            } else {
                                withContext(
                                    Dispatchers.IO
                                ) {
                                    ServerLocationResolver.resolve(
                                        this@MainActivity,
                                        host
                                    )
                                }
                            }
                    }

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    screen =
                                        Screen.CONNECTIONS
                                }
                    ) {
                        ServerConnectionCard(
                            connection = connection,
                            location = serverLocation
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Button(
                        modifier =
                            Modifier.size(160.dp),
                        shape =
                            CircleShape,
                        enabled =
                            vpnState !=
                                VpnConnectionState.CONNECTING,
                        colors =
                            ButtonDefaults.buttonColors(),
                        onClick = {
                            when (vpnState) {
                                VpnConnectionState.CONNECTED -> {
                                    AutoVlessVpnService.stop(
                                        this@MainActivity
                                    )
                                }

                                VpnConnectionState.DISCONNECTED,
                                VpnConnectionState.ERROR -> {
                                    startVpnConnection(
                                        connection
                                    )
                                }

                                VpnConnectionState.CONNECTING ->
                                    Unit
                            }
                        }
                    ) {
                        Text(
                            text =
                                when (vpnState) {
                                    VpnConnectionState.CONNECTING ->
                                        "…"

                                    else ->
                                        "⏻"
                                },
                            style =
                                MaterialTheme.typography.displayMedium
                        )
                    }

                    Text(
                        text =
                            when (vpnState) {
                                VpnConnectionState.DISCONNECTED ->
                                    "Нажмите, чтобы включить"

                                VpnConnectionState.CONNECTING ->
                                    "Подключение..."

                                VpnConnectionState.CONNECTED ->
                                    "VPN подключён"

                                VpnConnectionState.ERROR ->
                                    "Ошибка подключения"
                            },
                        style =
                            MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            screen =
                                Screen.ROUTING
                        }
                    ) {
                        Text("Маршрутизация")
                    }

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            screen =
                                Screen.ADD_CONNECTION
                        }
                    ) {
                        Text("Добавить ещё подключение")
                    }

                    val statusText =
                        when (vpnState) {
                            VpnConnectionState.DISCONNECTED ->
                                "Отключено"

                            VpnConnectionState.CONNECTING ->
                                "Подключение..."

                            VpnConnectionState.CONNECTED ->
                                "Подключено"

                            VpnConnectionState.ERROR ->
                                "Ошибка подключения"
                        }

                    Text(
                        text = "Статус: $statusText",
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
