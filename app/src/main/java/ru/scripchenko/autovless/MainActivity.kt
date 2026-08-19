package ru.scripchenko.autovless

import android.content.ClipboardManager
import android.content.Context
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.scripchenko.autovless.ui.theme.AutoVLESSTheme

class MainActivity : ComponentActivity() {

    private enum class Screen {
        HOME,
        SETTINGS,
        ABOUT,
        LOGS,
        ADD_CONNECTION,
        CONNECTIONS,
        ROUTING,
        APP_ROUTING,
        SITE_ROUTING
    }


    override fun onStop() {
        super.onStop()

        when (VpnStatus.state.value) {
            VpnConnectionState.CONNECTED -> {
                LauncherIconManager.showGreen(this)
            }

            VpnConnectionState.DISCONNECTED,
            VpnConnectionState.CONNECTING,
            VpnConnectionState.ERROR -> {
                LauncherIconManager.showBlue(this)
            }
        }
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

                var connectionsRevision by remember {
                    mutableStateOf(0)
                }

                val vpnState by
                    VpnStatus.state.collectAsState()

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

                val qrLauncher =
                    rememberLauncherForActivityResult(
                        contract = ScanContract()
                    ) { result ->
                        val contents =
                            result.contents
                                ?.trim()
                                .orEmpty()

                        if (contents.isBlank()) {
                            return@rememberLauncherForActivityResult
                        }

                        runCatching {
                            ConnectionStore.add(
                                context =
                                    this@MainActivity,
                                link = contents
                            )
                        }
                            .onSuccess {
                                selectedConnection =
                                    ConnectionStore.selected(
                                        this@MainActivity
                                    )

                                if (
                                    screen ==
                                        Screen.ADD_CONNECTION
                                ) {
                                    screen = Screen.HOME
                                }
                            }
                            .onFailure {
                                Toast.makeText(
                                    this@MainActivity,
                                    "QR-код не содержит корректную VLESS-ссылку",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
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
                                routing,
                                DiagnosticLogStore.isDetailedEnabled(
                                    this@MainActivity
                                )
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

                when (screen) {
                    Screen.SETTINGS -> {
                        SettingsScreen(
                            connectionCount =
                                ConnectionStore.loadAll(
                                    this@MainActivity
                                ).size,
                            onHomeClick = {
                                screen = Screen.HOME
                            },
                            onRoutingClick = {
                                screen = Screen.ROUTING
                            },
                            onAutomationClick = {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Автоматизацию добавим следующим этапом",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onConnectionsClick = {
                                screen = Screen.CONNECTIONS
                            },
                            onLogsClick = {
                                screen = Screen.LOGS
                            },
                            onAboutClick = {
                                screen = Screen.ABOUT
                            }
                        )

                        return@AutoVLESSTheme
                    }

                    Screen.LOGS -> {
                        BackHandler {
                            screen = Screen.SETTINGS
                        }

                        LogsScreen(
                            onBack = {
                                screen = Screen.SETTINGS
                            }
                        )

                        return@AutoVLESSTheme
                    }
                    Screen.ABOUT -> {
                        BackHandler {
                            screen = Screen.SETTINGS
                        }

                        AboutScreen(
                            onBack = {
                                screen = Screen.SETTINGS
                            }
                        )

                        return@AutoVLESSTheme
                    }

                    Screen.ADD_CONNECTION -> {
                        BackHandler {
                            screen = Screen.HOME
                        }

                        AddConnectionScreen(
                            onBack = {
                                screen = Screen.HOME
                            },
                            onScanQr = {
                                val options =
                                    ScanOptions().apply {
                                        setDesiredBarcodeFormats(
                                            ScanOptions.QR_CODE
                                        )
                                        setPrompt(
                                            "Наведите камеру на QR-код"
                                        )
                                        setBeepEnabled(false)
                                        setBarcodeImageEnabled(false)
                                        setOrientationLocked(true)
                                        setCaptureActivity(
                                            QrScannerActivity::class.java
                                        )
                                    }

                                qrLauncher.launch(
                                    options
                                )
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
                                remember(
                                    connectionsRevision
                                ) {
                                    ConnectionStore.loadAll(
                                        this@MainActivity
                                    )
                                },
                            selectedId =
                                selectedConnection?.id,
                            lockedConnectionId =
                                if (
                                    vpnState ==
                                        VpnConnectionState.CONNECTED ||
                                    vpnState ==
                                        VpnConnectionState.CONNECTING
                                ) {
                                    selectedConnection?.id
                                } else {
                                    null
                                },
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
                            onDelete = { connection ->
                                ConnectionStore.remove(
                                    this@MainActivity,
                                    connection.id
                                )

                                connectionsRevision++

                                selectedConnection =
                                    ConnectionStore.selected(
                                        this@MainActivity
                                    )
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

                val connection =
                    selectedConnection

                if (connection == null) {
                    AddConnectionScreen(
                        onBack = {},
                        onScanQr = {
                            val options =
                                ScanOptions().apply {
                                    setDesiredBarcodeFormats(
                                        ScanOptions.QR_CODE
                                    )
                                    setPrompt(
                                        "Наведите камеру на QR-код"
                                    )
                                    setBeepEnabled(false)
                                    setBarcodeImageEnabled(false)
                                    setOrientationLocked(true)
                                    setCaptureActivity(
                                        QrScannerActivity::class.java
                                    )
                                }

                            qrLauncher.launch(
                                options
                            )
                        },
                        onAdded = {
                            selectedConnection =
                                ConnectionStore.selected(
                                    this@MainActivity
                                )
                        }
                    )

                    return@AutoVLESSTheme
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

                var pingMs by
                    remember(connection.id) {
                        mutableStateOf<Int?>(null)
                    }

                var pingRefreshKey by
                    remember(connection.id) {
                        mutableStateOf(0)
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

                LaunchedEffect(
                    vpnState,
                    connection.id,
                    profile?.host,
                    profile?.port,
                    pingRefreshKey
                ) {
                    if (
                        vpnState !=
                        VpnConnectionState.CONNECTED ||
                        profile == null
                    ) {
                        pingMs = null
                        return@LaunchedEffect
                    }

                    pingMs =
                        ServerPingMeasurer.measure(
                            context =
                                this@MainActivity,
                            host =
                                profile.host,
                            port =
                                profile.port
                        )
                }

                PingwinHomeScreen(
                    connection = connection,
                    location = serverLocation,
                    vpnState = vpnState,
                    pingMs = pingMs,
                    onPowerClick = {
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
                    },
                    onConnectionsClick = {
                        screen =
                            Screen.CONNECTIONS
                    },
                    onAddQr = {
                        val options =
                            ScanOptions().apply {
                                setDesiredBarcodeFormats(
                                    ScanOptions.QR_CODE
                                )
                                setPrompt(
                                    "Наведите камеру на QR-код"
                                )
                                setBeepEnabled(false)
                                setBarcodeImageEnabled(false)
                                setOrientationLocked(true)
                                setCaptureActivity(QrScannerActivity::class.java)
                            }

                        qrLauncher.launch(
                            options
                        )
                    },
                    onAddClipboard = {
                        val clipboard =
                            getSystemService(
                                Context.CLIPBOARD_SERVICE
                            ) as ClipboardManager

                        val text =
                            clipboard.primaryClip
                                ?.getItemAt(0)
                                ?.coerceToText(
                                    this@MainActivity
                                )
                                ?.toString()
                                ?.trim()
                                .orEmpty()

                        if (text.isNotBlank()) {
                            runCatching {
                                ConnectionStore.add(
                                    context =
                                        this@MainActivity,
                                    link = text
                                )
                            }.onSuccess {
                                selectedConnection =
                                    ConnectionStore.selected(
                                        this@MainActivity
                                    )
                            }.onFailure {
                                screen =
                                    Screen.ADD_CONNECTION
                            }
                        } else {
                            screen =
                                Screen.ADD_CONNECTION
                        }
                    },
                    onAddManual = {
                        screen =
                            Screen.ADD_CONNECTION
                    },
                    onRefreshPing = {
                        pingRefreshKey++
                    },
                    onSettingsClick = {
                        screen =
                            Screen.SETTINGS
                    }
                )
            }
        }
    }
}
