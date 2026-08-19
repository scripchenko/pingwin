package ru.scripchenko.autovless

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.content.ContextCompat

class AutomationService : Service() {

    companion object {
        private const val CHANNEL_ID =
            "pingwin_automation"

        private const val NOTIFICATION_ID =
            2001

        fun sync(
            context: Context
        ) {
            val settings =
                AutomationSettingsStore.load(
                    context
                )

            if (settings.enabled) {
                ContextCompat.startForegroundService(
                    context,
                    Intent(
                        context,
                        AutomationService::class.java
                    )
                )
            } else {
                context.stopService(
                    Intent(
                        context,
                        AutomationService::class.java
                    )
                )
            }
        }
    }

    private val connectivityManager:
        ConnectivityManager
        get() =
            getSystemService(
                ConnectivityManager::class.java
            )

    private val mainHandler =
        Handler(
            Looper.getMainLooper()
        )

    private var networkCallback:
        ConnectivityManager.NetworkCallback? =
        null

    private var currentNetwork:
        Network? =
        null

    private var lastDecisionKey:
        String? =
        null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            AutomationNotification.build(
                this,
                "Автоматизация включена"
            )
        )

        registerNetworkMonitor()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val settings =
            AutomationSettingsStore.load(
                this
            )

        if (!settings.enabled) {
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    override fun onDestroy() {
        unregisterNetworkMonitor()
        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? =
        null

    private fun registerNetworkMonitor() {
        if (networkCallback != null) {
            return
        }

        val request =
            NetworkRequest
                .Builder()
                .addCapability(
                    NetworkCapabilities
                        .NET_CAPABILITY_INTERNET
                )
                .addCapability(
                    NetworkCapabilities
                        .NET_CAPABILITY_NOT_RESTRICTED
                )
                .addCapability(
                    NetworkCapabilities
                        .NET_CAPABILITY_NOT_VPN
                )
                .build()

        val callback =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S
            ) {
                object :
                    ConnectivityManager.NetworkCallback(
                        ConnectivityManager
                            .NetworkCallback
                            .FLAG_INCLUDE_LOCATION_INFO
                    ) {

                    override fun onAvailable(
                        network: Network
                    ) {
                        currentNetwork =
                            network
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        capabilities:
                            NetworkCapabilities
                    ) {
                        currentNetwork =
                            network

                        evaluate(
                            capabilities
                        )
                    }

                    override fun onLost(
                        network: Network
                    ) {
                        if (
                            currentNetwork ==
                            network
                        ) {
                            currentNetwork =
                                null

                            lastDecisionKey =
                                null
                        }
                    }
                }
            } else {
                object :
                    ConnectivityManager.NetworkCallback() {

                    override fun onAvailable(
                        network: Network
                    ) {
                        currentNetwork =
                            network
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        capabilities:
                            NetworkCapabilities
                    ) {
                        currentNetwork =
                            network

                        evaluate(
                            capabilities
                        )
                    }

                    override fun onLost(
                        network: Network
                    ) {
                        if (
                            currentNetwork ==
                            network
                        ) {
                            currentNetwork =
                                null

                            lastDecisionKey =
                                null
                        }
                    }
                }
            }

        networkCallback =
            callback

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {
            connectivityManager
                .registerBestMatchingNetworkCallback(
                    request,
                    callback,
                    mainHandler
                )
        } else {
            connectivityManager
                .registerNetworkCallback(
                    request,
                    callback
                )
        }
    }

    private fun unregisterNetworkMonitor() {
        val callback =
            networkCallback
                ?: return

        runCatching {
            connectivityManager
                .unregisterNetworkCallback(
                    callback
                )
        }

        networkCallback =
            null

        currentNetwork =
            null

        lastDecisionKey =
            null
    }

    private fun evaluate(
        capabilities:
            NetworkCapabilities
    ) {
        val settings =
            AutomationSettingsStore.load(
                this
            )

        if (!settings.enabled) {
            stopSelf()
            return
        }

        when {
            capabilities.hasTransport(
                NetworkCapabilities
                    .TRANSPORT_WIFI
            ) -> {
                val ssid =
                    resolveWifiSsid(
                        capabilities
                    )

                if (ssid == null) {
                    lastDecisionKey =
                        "wifi:unknown"

                    updateNotification(
                        "Не удалось определить Wi-Fi сеть"
                    )

                    DiagnosticLogStore.append(
                        this,
                        "Автоматизация: SSID недоступен — правило Wi-Fi не применено"
                    )

                    return
                }

                val trusted =
                    settings
                        .trustedWifiSsids
                        .any {
                            it.equals(
                                ssid,
                                ignoreCase = true
                            )
                        }

                val key =
                    "wifi:$ssid:" +
                        if (trusted) {
                            "trusted"
                        } else {
                            "untrusted"
                        }

                if (
                    key ==
                    lastDecisionKey
                ) {
                    return
                }

                lastDecisionKey =
                    key

                if (trusted) {
                    updateNotification(
                        "Доверенный Wi-Fi: $ssid"
                    )

                    if (
                        settings
                            .disconnectOnTrustedWifi
                    ) {
                        stopVpn(
                            "Доверенный Wi-Fi: $ssid"
                        )
                    }
                } else {
                    updateNotification(
                        "Недоверенный Wi-Fi: $ssid"
                    )

                    if (
                        settings
                            .connectOnUntrustedWifi
                    ) {
                        startVpn(
                            "Недоверенный Wi-Fi: $ssid"
                        )
                    }
                }
            }

            capabilities.hasTransport(
                NetworkCapabilities
                    .TRANSPORT_CELLULAR
            ) -> {
                val key =
                    "mobile"

                if (
                    key ==
                    lastDecisionKey
                ) {
                    return
                }

                lastDecisionKey =
                    key

                updateNotification(
                    "Мобильный интернет"
                )

                if (
                    settings.connectOnMobile
                ) {
                    startVpn(
                        "Мобильный интернет"
                    )
                }
            }
        }
    }

    private fun resolveWifiSsid(
        capabilities:
            NetworkCapabilities
    ): String? {
        val fromCapabilities =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {
                (
                    capabilities.transportInfo
                        as? WifiInfo
                    )
                    ?.ssid
            } else {
                null
            }

        val raw =
            fromCapabilities
                ?: runCatching {
                    @Suppress("DEPRECATION")
                    val wifiManager =
                        applicationContext
                            .getSystemService(
                                Context.WIFI_SERVICE
                            ) as WifiManager

                    @Suppress("DEPRECATION")
                    wifiManager
                        .connectionInfo
                        ?.ssid
                }.getOrNull()

        return raw
            ?.trim()
            ?.removeSurrounding("\"")
            ?.takeIf {
                it.isNotBlank() &&
                    it !=
                    WifiManager.UNKNOWN_SSID
            }
    }

    private fun startVpn(
        reason: String
    ) {
        val current =
            VpnStatus.state.value

        if (
            current ==
            VpnConnectionState.CONNECTED ||
            current ==
            VpnConnectionState.CONNECTING
        ) {
            return
        }

        if (
            VpnService.prepare(this) !=
            null
        ) {
            DiagnosticLogStore.append(
                this,
                "Автоматизация: требуется разрешение VPN"
            )

            updateNotification(
                "Откройте pingwin и разрешите VPN"
            )

            return
        }

        val connection =
            resolveConnection()
                ?: run {
                    DiagnosticLogStore.append(
                        this,
                        "Автоматизация: нет сохранённого сервера"
                    )

                    updateNotification(
                        "Нет сохранённого сервера"
                    )

                    return
                }

        runCatching {
            val profile =
                VlessProfile.parse(
                    connection.link
                )

            val routing =
                RoutingSettingsStore.load(
                    this
                )

            val config =
                SingBoxConfigBuilder.build(
                    profile,
                    routing,
                    DiagnosticLogStore
                        .isDetailedEnabled(
                            this
                        )
                )

            DiagnosticLogStore.append(
                this,
                "Автоматизация: включение VPN — $reason"
            )

            AutoVlessVpnService.start(
                this,
                config
            )
        }.onFailure {
            DiagnosticLogStore.append(
                this,
                "Автоматизация: ошибка запуска — " +
                    (
                        it.message
                            ?: it.javaClass.simpleName
                    )
            )
        }
    }

    private fun stopVpn(
        reason: String
    ) {
        val current =
            VpnStatus.state.value

        if (
            current ==
            VpnConnectionState.DISCONNECTED ||
            current ==
            VpnConnectionState.ERROR
        ) {
            return
        }

        DiagnosticLogStore.append(
            this,
            "Автоматизация: отключение VPN — $reason"
        )

        AutoVlessVpnService.stop(
            this
        )
    }

    private fun resolveConnection():
        SavedConnection? {
        val settings =
            AutomationSettingsStore.load(
                this
            )

        val preferredId =
            settings.serverId

        if (preferredId != null) {
            ConnectionStore
                .loadAll(this)
                .firstOrNull {
                    it.id ==
                        preferredId
                }
                ?.let {
                    return it
                }
        }

        return ConnectionStore.selected(
            this
        )
    }

    private fun createNotificationChannel() {
        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Автоматизация pingwin",
                NotificationManager
                    .IMPORTANCE_LOW
            )
        )
    }

    private fun updateNotification(
        text: String
    ) {
        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            NOTIFICATION_ID,
            AutomationNotification.build(
                this,
                text
            )
        )
    }

    private object AutomationNotification {

        fun build(
            context: Context,
            text: String
        ) =
            androidx.core.app
                .NotificationCompat
                .Builder(
                    context,
                    CHANNEL_ID
                )
                .setSmallIcon(
                    android.R.drawable
                        .stat_notify_sync
                )
                .setContentTitle(
                    "pingwin"
                )
                .setContentText(
                    text
                )
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(
                    androidx.core.app
                        .NotificationCompat
                        .PRIORITY_LOW
                )
                .build()
    }
}
