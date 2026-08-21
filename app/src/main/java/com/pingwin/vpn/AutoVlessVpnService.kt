package com.pingwin.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.os.Process
import android.system.Os
import android.system.OsConstants
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.annotation.RequiresApi
import io.nekohasekai.libbox.CommandServer
import io.nekohasekai.libbox.CommandServerHandler
import io.nekohasekai.libbox.ConnectionOwner
import io.nekohasekai.libbox.InterfaceUpdateListener
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.LocalDNSTransport
import io.nekohasekai.libbox.NetworkInterfaceIterator
import io.nekohasekai.libbox.Notification
import io.nekohasekai.libbox.OverrideOptions
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.SetupOptions
import io.nekohasekai.libbox.StringIterator
import io.nekohasekai.libbox.SystemProxyStatus
import io.nekohasekai.libbox.TunOptions
import io.nekohasekai.libbox.WIFIState
import java.io.File
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import io.nekohasekai.libbox.NetworkInterface as LibboxNetworkInterface

class AutoVlessVpnService :
    VpnService(),
    PlatformInterface,
    CommandServerHandler {

    companion object {
        private const val TAG = "Pingwin"
        private const val EXTRA_CONFIG = "config"

        private const val ACTION_START = "START"
        private const val ACTION_STOP = "STOP"

        private const val NOTIFICATION_CHANNEL_ID = "autovless_vpn"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context, config: String) {
            val intent =
                Intent(
                    context,
                    AutoVlessVpnService::class.java
                ).apply {
                    action = ACTION_START
                    putExtra(EXTRA_CONFIG, config)
                }

            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent =
                Intent(
                    context,
                    AutoVlessVpnService::class.java
                ).apply {
                    action = ACTION_STOP
                }

            context.startService(intent)
        }
    }

    private val executor =
        Executors.newSingleThreadExecutor()

    private val connectivityManager: ConnectivityManager
        get() =
            getSystemService(
                ConnectivityManager::class.java
            )

    private var commandServer: CommandServer? = null
    private var singBoxLogClient: SingBoxLogClient? = null
    private var tunDescriptor: ParcelFileDescriptor? = null

    private val mainHandler =
        Handler(Looper.getMainLooper())

    @Volatile
    private var physicalDefaultNetwork: Network? = null

    private var physicalNetworkCallback:
            ConnectivityManager.NetworkCallback? = null

    @Volatile
    private var interfaceListener:
            InterfaceUpdateListener? = null

    @Volatile
    private var isStarting = false

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        try {
            setupLibbox()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Libbox setup failed",
                e
            )
        }
    }

    private fun setupLibbox() {
        val baseDir =
            File(filesDir, "sing-box").apply {
                mkdirs()
            }

        val workingDir =
            File(baseDir, "working").apply {
                mkdirs()
            }

        val tempDir =
            File(cacheDir, "sing-box").apply {
                mkdirs()
            }

        val options =
            SetupOptions().apply {
                basePath =
                    baseDir.absolutePath

                workingPath =
                    workingDir.absolutePath

                tempPath =
                    tempDir.absolutePath

                fixAndroidStack =
                    true

                commandServerListenPort =
                    0

                commandServerSecret =
                    ""

                logMaxLines =
                    500

                debug =
                    true
            }

        Libbox.setup(options)

        Log.d(
            TAG,
            "Libbox setup complete"
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForegroundNotification(
            getString(R.string.vpn_service_starting)
        )

        when (intent?.action) {

            ACTION_START -> {
                DiagnosticLogStore.append(
                    this,
                    getString(R.string.vpn_log_start)
                )

                VpnStatus.set(
                    VpnConnectionState.CONNECTING
                )

                val config =
                    intent.getStringExtra(
                        EXTRA_CONFIG
                    )

                if (!config.isNullOrBlank()) {

                    executor.execute {
                        startVpn(config)
                    }

                } else {
                    Log.e(
                        TAG,
                        "Missing sing-box config"
                    )

                    stopVpn()
                }
            }

            ACTION_STOP -> {
                executor.execute {
                    stopVpn()
                }
            }

            else -> {
                Log.w(
                    TAG,
                    "Unknown service action: ${intent?.action}"
                )
            }
        }

        return START_NOT_STICKY
    }

    private fun startVpn(
        config: String
    ) {
        synchronized(this) {

            if (
                commandServer != null ||
                isStarting
            ) {
                Log.d(
                    TAG,
                    "VPN is already running or starting"
                )
                return
            }

            isStarting = true

            try {
                Log.d(
                    TAG,
                    "Creating CommandServer"
                )

                val server =
                    CommandServer(
                        this,
                        this
                    )

                Log.d(
                    TAG,
                    "Starting CommandServer"
                )

                server.start()

                Log.d(
                    TAG,
                    "Starting physical network monitor"
                )

                startPhysicalNetworkMonitor()

                Log.d(
                    TAG,
                    "Starting sing-box service"
                )

                server.startOrReloadService(
                    config,
                    OverrideOptions()
                )

                commandServer =
                    server

                singBoxLogClient =
                    SingBoxLogClient(this).also {
                        it.start()
                    }

                Log.d(
                    TAG,
                    "sing-box started"
                )

                DiagnosticLogStore.append(
                    this,
                    getString(R.string.vpn_log_connected)
                )

                VpnStatus.set(
                    VpnConnectionState.CONNECTED
                )

                if (
                    !AppVisibility.isForeground()
                ) {
                    LauncherIconManager.showGreen(
                        this
                    )
                }


                updateForegroundNotification(
                    getString(R.string.vpn_service_running)
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Failed to start sing-box",
                    e
                )

                DiagnosticLogStore.append(
                    this,
                    getString(R.string.vpn_log_connection_error, e.localizedVpnMessage(this))
                )

                stopVpn()

                VpnStatus.set(
                    VpnConnectionState.ERROR
                )

            } finally {
                isStarting = false
            }
        }
    }

    private fun stopVpn() {
        synchronized(this) {

            stopPhysicalNetworkMonitor()

            singBoxLogClient
                ?.stop()

            singBoxLogClient =
                null

            try {
                commandServer
                    ?.closeService()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "closeService failed",
                    e
                )
            }

            try {
                commandServer
                    ?.close()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "CommandServer close failed",
                    e
                )
            }

            commandServer =
                null

            try {
                tunDescriptor
                    ?.close()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "TUN close failed",
                    e
                )
            }

            tunDescriptor =
                null

            stopForeground(
                STOP_FOREGROUND_REMOVE
            )

            stopSelf()

            DiagnosticLogStore.append(
                this,
                getString(R.string.vpn_log_disconnected)
            )

            VpnStatus.set(
                VpnConnectionState.DISCONNECTED
            )

            if (
                !AppVisibility.isForeground()
            ) {
                LauncherIconManager.showBlue(
                    this
                )
            }


            Log.d(
                TAG,
                "VPN stopped"
            )
        }
    }

    override fun onDestroy() {

        stopPhysicalNetworkMonitor()

            singBoxLogClient
                ?.stop()

            singBoxLogClient =
                null

            try {
                commandServer
                ?.closeService()
        } catch (_: Exception) {
        }

        try {
            commandServer
                ?.close()
        } catch (_: Exception) {
        }

        try {
            tunDescriptor
                ?.close()
        } catch (_: Exception) {
        }

        commandServer =
            null

        tunDescriptor =
            null

        executor.shutdownNow()

        super.onDestroy()
    }

    override fun onRevoke() {
        executor.execute {
            stopVpn()
        }

        super.onRevoke()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return super.onBind(intent)
    }

    // ------------------------------------------------------------
    // Foreground notification
    // ------------------------------------------------------------

    private fun createNotificationChannel() {
        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Pingwin VPN",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description =
                    getString(R.string.vpn_notification_channel_description)
            }

        manager.createNotificationChannel(
            channel
        )
    }

    private fun startForegroundNotification(
        text: String
    ) {
        startForeground(
            NOTIFICATION_ID,
            buildNotification(text)
        )
    }

    private fun updateForegroundNotification(
        text: String
    ) {
        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            NOTIFICATION_ID,
            buildNotification(text)
        )
    }

    private fun buildNotification(
        text: String
    ): android.app.Notification {

        return NotificationCompat
            .Builder(
                this,
                NOTIFICATION_CHANNEL_ID
            )
            .setContentTitle(
                "Pingwin"
            )
            .setContentText(
                text
            )
            .setSmallIcon(
                android.R.drawable.stat_sys_warning
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    // ------------------------------------------------------------
    // PlatformInterface
    // ------------------------------------------------------------

    override fun usePlatformAutoDetectInterfaceControl(): Boolean {
        Log.d(TAG, "usePlatformAutoDetectInterfaceControl CALLED -> true")
        return true
    }

    override fun autoDetectInterfaceControl(
        fd: Int
    ) {
        val network =
            physicalDefaultNetwork

        Log.d(
            TAG,
            "autoDetectInterfaceControl: fd=$fd network=$network"
        )

        if (!protect(fd)) {
            Log.w(
                TAG,
                "protect($fd) returned false"
            )
        }

        if (network == null) {
            Log.e(
                TAG,
                "autoDetectInterfaceControl: no physical network for fd=$fd"
            )
            return
        }

        val duplicate =
            ParcelFileDescriptor.fromFd(fd)

        try {
            network.bindSocket(
                duplicate.fileDescriptor
            )

            Log.d(
                TAG,
                "Network.bindSocket OK: fd=$fd network=$network"
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Network.bindSocket failed: fd=$fd network=$network",
                e
            )
            throw e
        } finally {
            duplicate.close()
        }
    }

    override fun openTun(
        options: TunOptions
    ): Int {

        if (
            prepare(this) != null
        ) {
            error(
                "VPN permission has not been granted"
            )
        }

        val builder =
            Builder()
                .setSession(
                    "Pingwin"
                )
                .setMtu(
                    options.mtu
                )

        val ipv4 =
            options.inet4Address

        while (ipv4.hasNext()) {
            val address =
                ipv4.next()

            builder.addAddress(
                address.address(),
                address.prefix()
            )
        }

        val ipv6 =
            options.inet6Address

        while (ipv6.hasNext()) {
            val address =
                ipv6.next()

            builder.addAddress(
                address.address(),
                address.prefix()
            )
        }

        if (
            options.autoRoute
        ) {

            val ipv4Routes =
                options.inet4RouteRange

            if (
                ipv4Routes.hasNext()
            ) {

                while (
                    ipv4Routes.hasNext()
                ) {
                    val route =
                        ipv4Routes.next()

                    builder.addRoute(
                        route.address(),
                        route.prefix()
                    )
                }

            } else {

                builder.addRoute(
                    "0.0.0.0",
                    0
                )
            }

            val ipv6Routes =
                options.inet6RouteRange

            if (
                ipv6Routes.hasNext()
            ) {

                while (
                    ipv6Routes.hasNext()
                ) {
                    val route =
                        ipv6Routes.next()

                    builder.addRoute(
                        route.address(),
                        route.prefix()
                    )
                }
            }
        }

        builder.addDnsServer(
            "1.1.1.1"
        )

        AppRoutingConfigurator.apply(
            this,
            builder,
            RoutingSettingsStore.load(this)
        )

        val descriptor =
            builder.establish()
                ?: error(
                    "Android failed to establish VPN interface"
                )

        tunDescriptor =
            descriptor

        Log.d(
            TAG,
            "TUN established, fd=${descriptor.fd}"
        )

        return descriptor.fd
    }

    /*
     * On Android, do not use /proc to determine
     * the process that created the connection.
     */
    override fun useProcFS(): Boolean =
        false

    override fun includeAllNetworks(): Boolean =
        false

    override fun underNetworkExtension(): Boolean =
        false

    override fun clearDNSCache() {
    }

    override fun readWIFIState(): WIFIState? =
        null

    override fun localDNSTransport(): LocalDNSTransport? =
        null

    override fun systemCertificates(): StringIterator? =
        null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun findConnectionOwner(
        ipProtocol: Int,
        sourceAddress: String,
        sourcePort: Int,
        destinationAddress: String,
        destinationPort: Int
    ): ConnectionOwner {
        try {
            val uid =
                connectivityManager.getConnectionOwnerUid(
                    ipProtocol,
                    InetSocketAddress(
                        sourceAddress,
                        sourcePort
                    ),
                    InetSocketAddress(
                        destinationAddress,
                        destinationPort
                    )
                )

            if (uid == Process.INVALID_UID) {
                error(
                    "android: connection owner not found"
                )
            }

            val packages =
                packageManager.getPackagesForUid(
                    uid
                )

            return ConnectionOwner().apply {
                userId =
                    uid

                userName =
                    packages?.firstOrNull()
                        ?: ""

                setAndroidPackageNames(
                    StringArray(
                        packages
                            ?.toList()
                            ?.iterator()
                            ?: emptyList<String>()
                                .iterator()
                    )
                )
            }

        } catch (e: Exception) {
            Log.e(
                TAG,
                "findConnectionOwner failed",
                e
            )

            throw e
        }
    }

    override fun getInterfaces(): NetworkInterfaceIterator {

        val result =
            mutableListOf<LibboxNetworkInterface>()

        for (
        network in
        connectivityManager.allNetworks
        ) {

            val properties =
                connectivityManager
                    .getLinkProperties(network)
                    ?: continue

            val capabilities =
                connectivityManager
                    .getNetworkCapabilities(network)
                    ?: continue

            val interfaceName =
                properties.interfaceName
                    ?: continue

            if (
                interfaceName.startsWith("tun") ||
                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_VPN
                )
            ) {
                continue
            }

            val interfaceIndex =
                resolveInterfaceIndex(
                    properties,
                    interfaceName
                )

            if (interfaceIndex <= 0) {
                Log.w(
                    TAG,
                    "Skipping interface $interfaceName: " +
                            "unable to resolve interface index"
                )
                continue
            }

            val boxInterface =
                LibboxNetworkInterface()

            boxInterface.name =
                interfaceName

            boxInterface.index =
                interfaceIndex

            boxInterface.mtu =
                if (properties.mtu > 0) {
                    properties.mtu
                } else {
                    1500
                }

            boxInterface.type =
                when {

                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    ) ->
                        Libbox.InterfaceTypeWIFI

                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR
                    ) ->
                        Libbox.InterfaceTypeCellular

                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_ETHERNET
                    ) ->
                        Libbox.InterfaceTypeEthernet

                    else ->
                        Libbox.InterfaceTypeOther
                }

            boxInterface.metered =
                !capabilities.hasCapability(
                    NetworkCapabilities
                        .NET_CAPABILITY_NOT_METERED
                )

            var flags = 0

            if (
                capabilities.hasCapability(
                    NetworkCapabilities
                        .NET_CAPABILITY_INTERNET
                )
            ) {
                flags =
                    flags or
                            OsConstants.IFF_UP or
                            OsConstants.IFF_RUNNING
            }

            boxInterface.flags =
                flags

            val addresses =
                properties
                    .linkAddresses
                    .mapNotNull { linkAddress ->
                        val host =
                            linkAddress.address.hostAddress
                                ?.substringBefore("%")

                        if (host.isNullOrBlank()) {
                            null
                        } else {
                            "$host/${linkAddress.prefixLength}"
                        }
                    }

            boxInterface.addresses =
                StringArray(
                    addresses.iterator()
                )

            val dnsServers =
                properties
                    .dnsServers
                    .mapNotNull {
                        it.hostAddress
                    }

            boxInterface.dnsServer =
                StringArray(
                    dnsServers.iterator()
                )

            Log.d(
                TAG,
                "Interface from LinkProperties: " +
                        "$interfaceName " +
                        "index=$interfaceIndex " +
                        "mtu=${boxInterface.mtu} " +
                        "type=${boxInterface.type} " +
                        "addresses=${addresses.size}"
            )

            result.add(
                boxInterface
            )
        }

        Log.d(
            TAG,
            "getInterfaces(): ${result.size} interfaces"
        )

        return InterfaceArray(
            result.iterator()
        )
    }

    override fun startDefaultInterfaceMonitor(
        listener: InterfaceUpdateListener?
    ) {
        if (listener == null) {
            return
        }

        Log.d(
            TAG,
            "libbox registered default interface listener"
        )

        interfaceListener =
            listener

        val network =
            physicalDefaultNetwork

        if (network != null) {
            notifyDefaultInterface(
                network
            )
        } else {
            Log.w(
                TAG,
                "No physical default network yet when libbox listener registered"
            )
        }
    }

    override fun closeDefaultInterfaceMonitor(
        listener: InterfaceUpdateListener?
    ) {
        Log.d(
            TAG,
            "libbox closed default interface listener"
        )

        if (
            interfaceListener === listener ||
            listener == null
        ) {
            interfaceListener =
                null
        }
    }

    private fun startPhysicalNetworkMonitor() {
        if (physicalNetworkCallback != null) {
            return
        }

        val current =
            connectivityManager.activeNetwork

        if (current != null) {
            val caps =
                connectivityManager
                    .getNetworkCapabilities(current)

            if (
                caps != null &&
                !caps.hasTransport(
                    NetworkCapabilities.TRANSPORT_VPN
                )
            ) {
                physicalDefaultNetwork =
                    current

                Log.d(
                    TAG,
                    "Captured physical network before VPN: $current"
                )
            }
        }

        val request =
            NetworkRequest
                .Builder()
                .addCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                .addCapability(
                    NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED
                )
                .build()

        val callback =
            object :
                ConnectivityManager.NetworkCallback() {

                override fun onAvailable(
                    network: Network
                ) {
                    val caps =
                        connectivityManager
                            .getNetworkCapabilities(network)

                    if (
                        caps?.hasTransport(
                            NetworkCapabilities.TRANSPORT_VPN
                        ) == true
                    ) {
                        Log.w(
                            TAG,
                            "Ignoring VPN network from physical monitor: $network"
                        )
                        return
                    }

                    physicalDefaultNetwork =
                        network

                    Log.d(
                        TAG,
                        "Physical network available: $network"
                    )

                    notifyDefaultInterface(
                        network
                    )
                }

                override fun onLinkPropertiesChanged(
                    network: Network,
                    linkProperties: LinkProperties
                ) {
                    if (
                        network !=
                        physicalDefaultNetwork
                    ) {
                        return
                    }

                    Log.d(
                        TAG,
                        "Physical link properties: " +
                                "network=$network " +
                                "interface=${linkProperties.interfaceName}"
                    )

                    notifyDefaultInterface(
                        network
                    )
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities:
                    NetworkCapabilities
                ) {
                    if (
                        network !=
                        physicalDefaultNetwork
                    ) {
                        return
                    }

                    notifyDefaultInterface(
                        network
                    )
                }

                override fun onLost(
                    network: Network
                ) {
                    if (
                        network !=
                        physicalDefaultNetwork
                    ) {
                        return
                    }

                    Log.d(
                        TAG,
                        "Physical network lost: $network"
                    )

                    physicalDefaultNetwork =
                        null

                    interfaceListener
                        ?.updateDefaultInterface(
                            "",
                            -1,
                            false,
                            false
                        )
                }
            }

        physicalNetworkCallback =
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

            Log.d(
                TAG,
                "Registered best-matching physical network callback"
            )
        } else {
            connectivityManager
                .registerNetworkCallback(
                    request,
                    callback
                )

            Log.d(
                TAG,
                "Registered physical network callback"
            )
        }

        physicalDefaultNetwork
            ?.let {
                notifyDefaultInterface(
                    it
                )
            }
    }

    private fun resolveInterfaceIndex(
        properties: LinkProperties,
        interfaceName: String
    ): Int {
        val osIndex =
            try {
                Os.if_nametoindex(
                    interfaceName
                )
            } catch (e: Exception) {
                Log.w(
                    TAG,
                    "Os.if_nametoindex($interfaceName) failed",
                    e
                )
                0
            }

        if (osIndex > 0) {
            Log.d(
                TAG,
                "Interface index from Os: " +
                        "$interfaceName index=$osIndex"
            )
            return osIndex
        }

        for (linkAddress in properties.linkAddresses) {
            val address =
                linkAddress.address

            if (address is Inet6Address) {
                val scopeId =
                    address.scopeId

                Log.d(
                    TAG,
                    "IPv6 scope candidate: " +
                            "$interfaceName " +
                            "address=${address.hostAddress} " +
                            "scopeId=$scopeId"
                )

                if (scopeId > 0) {
                    Log.d(
                        TAG,
                        "Interface index from IPv6 scope: " +
                                "$interfaceName index=$scopeId"
                    )
                    return scopeId
                }
            }
        }

        Log.e(
            TAG,
            "Unable to resolve interface index for $interfaceName"
        )

        return 0
    }

    private fun notifyDefaultInterface(
        network: Network
    ) {
        val listener =
            interfaceListener
                ?: return

        val properties =
            connectivityManager
                .getLinkProperties(network)
                ?: run {
                    Log.w(
                        TAG,
                        "Default interface: LinkProperties unavailable " +
                                "for network=$network"
                    )
                    return
                }

        val interfaceName =
            properties.interfaceName

        if (interfaceName.isNullOrBlank()) {
            Log.w(
                TAG,
                "Default interface: interfaceName unavailable " +
                        "for network=$network"
            )
            return
        }

        if (
            interfaceName.startsWith("tun")
        ) {
            Log.w(
                TAG,
                "Ignoring VPN interface as physical default: $interfaceName"
            )
            return
        }

        /*
         * On some Honor devices, the rmnet interface name is visible via
         * ConnectivityManager, while NetworkInterface and if_nametoindex()
         * do not provide an index. In that case, use the numeric scopeId
         * of the IPv6 address from LinkProperties. For link-local IPv6,
         * it corresponds to the system interface index.
         */
        val interfaceIndex =
            resolveInterfaceIndex(
                properties,
                interfaceName
            )

        if (interfaceIndex <= 0) {
            Log.e(
                TAG,
                "Default interface unresolved: " +
                        "$interfaceName ifindex=$interfaceIndex"
            )
            return
        }

        Log.d(
            TAG,
            "Default physical interface BEFORE update: " +
                    "$interfaceName index=$interfaceIndex"
        )

        listener.updateDefaultInterface(
            interfaceName,
            interfaceIndex,
            false,
            false
        )

        Log.d(
            TAG,
            "Default physical interface AFTER update: " +
                    "$interfaceName index=$interfaceIndex"
        )
    }

    private fun stopPhysicalNetworkMonitor() {
        val callback =
            physicalNetworkCallback

        if (callback != null) {
            try {
                connectivityManager
                    .unregisterNetworkCallback(
                        callback
                    )
            } catch (_: Exception) {
            }
        }

        physicalNetworkCallback =
            null

        physicalDefaultNetwork =
            null

        interfaceListener =
            null
    }

    override fun sendNotification(
        notification: Notification?
    ) {
        Log.d(
            TAG,
            "libbox notification: " +
                    "${notification?.title}: " +
                    "${notification?.body}"
        )
    }

    // ------------------------------------------------------------
    // CommandServerHandler
    // ------------------------------------------------------------

    override fun serviceStop() {
        executor.execute {
            stopVpn()
        }
    }

    override fun serviceReload() {
        Log.d(
            TAG,
            "serviceReload requested"
        )
    }

    override fun getSystemProxyStatus(): SystemProxyStatus? =
        null

    override fun setSystemProxyEnabled(
        isEnabled: Boolean
    ) {
    }

    override fun writeDebugMessage(
        message: String?
    ) {
        Log.d(
            TAG,
            message ?: ""
        )
    }

    // ------------------------------------------------------------
    // libbox iterators
    // ------------------------------------------------------------

    private class InterfaceArray(
        private val iterator:
        Iterator<LibboxNetworkInterface>
    ) : NetworkInterfaceIterator {

        override fun hasNext(): Boolean =
            iterator.hasNext()

        override fun next():
                LibboxNetworkInterface =
            iterator.next()
    }

    private class StringArray(
        private val iterator:
        Iterator<String>
    ) : StringIterator {

        override fun len(): Int =
            0

        override fun hasNext(): Boolean =
            iterator.hasNext()

        override fun next(): String =
            iterator.next()
    }

}
