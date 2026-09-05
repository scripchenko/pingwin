package com.pingwin.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.telephony.TelephonyManager
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

    private var countryReceiver:
        BroadcastReceiver? =
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
                getString(R.string.automation_service_enabled)
            )
        )

        registerNetworkMonitor()
        registerCountryMonitor()
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

        evaluateCurrentNetwork()

        return START_STICKY
    }

    override fun onDestroy() {
        unregisterCountryMonitor()
        unregisterNetworkMonitor()
        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? =
        null

    private fun registerCountryMonitor() {
        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.Q ||
            countryReceiver != null
        ) {
            return
        }

        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?
                ) {
                    lastDecisionKey =
                        null

                    evaluateCurrentNetwork()
                }
            }

        countryReceiver =
            receiver

        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(
                TelephonyManager
                    .ACTION_NETWORK_COUNTRY_CHANGED
            ),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun unregisterCountryMonitor() {
        val receiver =
            countryReceiver
                ?: return

        runCatching {
            unregisterReceiver(
                receiver
            )
        }

        countryReceiver =
            null
    }

    private fun evaluateCurrentNetwork() {
        val network =
            currentNetwork
                ?: return

        val capabilities =
            connectivityManager
                .getNetworkCapabilities(
                    network
                )
                ?: return

        evaluate(
            capabilities
        )
    }
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

        if (
            settings.disconnectWhenAbroad
        ) {
            val countries =
                TelephonyCountryResolver.resolve(
                    this,
                    settings
                )

            val abroadStatus =
                AbroadDetector.determine(
                    countries.homeCountryCode,
                    countries.networkCountryCode
                )

            if (
                abroadStatus ==
                AbroadStatus.ABROAD
            ) {
                val reason =
                    getString(
                        R.string.automation_reason_abroad
                    )

                val key =
                    "abroad:" +
                        countries.homeCountryCode +
                        ":" +
                        countries.networkCountryCode

                if (
                    key !=
                    lastDecisionKey
                ) {
                    lastDecisionKey =
                        key

                    updateNotification(
                        reason
                    )
                }

                stopVpn(
                    reason
                )

                return
            }
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
                        getString(R.string.automation_service_wifi_unknown)
                    )

                    DiagnosticLogStore.append(
                        this,
                        getString(R.string.automation_log_ssid_unavailable)
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
                        getString(R.string.automation_reason_trusted_wifi, ssid)
                    )

                    if (
                        settings
                            .disconnectOnTrustedWifi
                    ) {
                        stopVpn(
                            getString(R.string.automation_reason_trusted_wifi, ssid)
                        )
                    }
                } else {
                    updateNotification(
                        getString(R.string.automation_reason_untrusted_wifi, ssid)
                    )

                    if (
                        settings
                            .connectOnUntrustedWifi
                    ) {
                        startVpn(
                            getString(R.string.automation_reason_untrusted_wifi, ssid)
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
                    getString(R.string.automation_reason_mobile_data)
                )

                if (
                    settings.connectOnMobile
                ) {
                    startVpn(
                        getString(R.string.automation_reason_mobile_data)
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
                getString(R.string.automation_log_vpn_permission_required)
            )

            updateNotification(
                getString(R.string.automation_service_open_for_permission)
            )

            return
        }

        val connection =
            resolveConnection()
                ?: run {
                    DiagnosticLogStore.append(
                        this,
                        getString(R.string.automation_log_no_saved_server)
                    )

                    updateNotification(
                        getString(R.string.automation_service_no_saved_server)
                    )

                    return
                }

        runCatching {
            val profile =
                ConnectionProfileParser.parse(
                    connection.link
                )

            val routing =
                RoutingSettingsStore.load(
                    this
                )

            val config =
                ConnectionConfigBuilder.build(
                    profile,
                    routing,
                    DiagnosticLogStore
                        .isDetailedEnabled(
                            this
                        )
                )

            DiagnosticLogStore.append(
                this,
                getString(R.string.automation_log_start_vpn, reason)
            )

            AutoVlessVpnService.start(
                this,
                config
            )
        }.onFailure {
            DiagnosticLogStore.append(
                this,
                getString(
                    R.string.automation_log_start_error,
                    it.localizedVpnMessage(this)
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
            getString(R.string.automation_log_stop_vpn, reason)
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
                getString(R.string.automation_notification_channel),
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
