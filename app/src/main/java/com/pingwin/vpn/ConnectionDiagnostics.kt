package com.pingwin.vpn

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.InetAddress
import kotlin.coroutines.coroutineContext

enum class DiagnosticStatus {
    OK,
    FAILED,
    SKIPPED
}

enum class DiagnosticMessage {
    LINK_VALID,
    LINK_INVALID,
    CONFIG_VALID,
    CONFIG_INVALID,
    DNS_RESOLVED,
    DNS_FAILED,
    DNS_NOT_NEEDED,
    TCP_REACHABLE,
    TCP_UNREACHABLE,
    UDP_NOT_TESTED,
    NO_PHYSICAL_NETWORK,
    PREVIOUS_STEP_FAILED
}

data class DiagnosticStep(
    val status: DiagnosticStatus,
    val message: DiagnosticMessage
)

data class ConnectionDiagnosticResult(
    val protocol: String?,
    val maskedHost: String?,
    val port: Int?,
    val link: DiagnosticStep,
    val config: DiagnosticStep,
    val dns: DiagnosticStep,
    val tcp: DiagnosticStep
)

/**
 * Passive checks only: never starts, stops, or reconfigures the VPN.
 * Network probes are explicitly routed over a non-VPN Android Network.
 * Neither the saved link nor the generated sing-box config is logged or returned.
 */
object ConnectionDiagnostics {

    suspend fun run(
        context: Context,
        connection: SavedConnection
    ): ConnectionDiagnosticResult = withContext(Dispatchers.IO) {
        val skipped = DiagnosticStep(
            DiagnosticStatus.SKIPPED,
            DiagnosticMessage.PREVIOUS_STEP_FAILED
        )

        val profile = try {
            ConnectionProfileParser.parse(connection.link)
        } catch (_: Exception) {
            return@withContext ConnectionDiagnosticResult(
                protocol = null,
                maskedHost = null,
                port = null,
                link = DiagnosticStep(
                    DiagnosticStatus.FAILED,
                    DiagnosticMessage.LINK_INVALID
                ),
                config = skipped,
                dns = skipped,
                tcp = skipped
            )
        }

        val configIsValid = try {
            // Default routing deliberately avoids loading/writing user routing settings.
            // This validates the server profile, not custom routing rules.
            val config = ConnectionConfigBuilder.build(
                profile = profile,
                routing = RoutingSettings(),
                detailedLogging = false
            )
            LibboxValidator.validate(config).isSuccess
        } catch (_: Exception) {
            false
        }

        val configStep = DiagnosticStep(
            if (configIsValid) DiagnosticStatus.OK else DiagnosticStatus.FAILED,
            if (configIsValid) DiagnosticMessage.CONFIG_VALID else DiagnosticMessage.CONFIG_INVALID
        )

        val physicalNetwork = findPhysicalNetwork(context)
        if (physicalNetwork == null) {
            val noNetwork = DiagnosticStep(
                DiagnosticStatus.SKIPPED,
                DiagnosticMessage.NO_PHYSICAL_NETWORK
            )
            return@withContext ConnectionDiagnosticResult(
                protocol = profile.protocol.displayName,
                maskedHost = HostPrivacyMasker.mask(profile.host),
                port = profile.port,
                link = DiagnosticStep(DiagnosticStatus.OK, DiagnosticMessage.LINK_VALID),
                config = configStep,
                dns = noNetwork,
                tcp = noNetwork
            )
        }

        val isLiteral = isIpLiteral(profile.host)
        val addresses = try {
            physicalNetwork.getAllByName(profile.host).toList()
        } catch (_: Exception) {
            emptyList<InetAddress>()
        }
        coroutineContext.ensureActive()

        val dnsStep = when {
            addresses.isEmpty() -> DiagnosticStep(
                DiagnosticStatus.FAILED,
                DiagnosticMessage.DNS_FAILED
            )
            isLiteral -> DiagnosticStep(
                DiagnosticStatus.SKIPPED,
                DiagnosticMessage.DNS_NOT_NEEDED
            )
            else -> DiagnosticStep(
                DiagnosticStatus.OK,
                DiagnosticMessage.DNS_RESOLVED
            )
        }

        val tcpStep = when {
            profile.protocol == ConnectionProtocol.HYSTERIA2 ||
                profile.protocol == ConnectionProtocol.TUIC -> DiagnosticStep(
                DiagnosticStatus.SKIPPED,
                DiagnosticMessage.UDP_NOT_TESTED
            )
            addresses.isEmpty() -> skipped
            else -> {
                // At most 3 addresses and 2 seconds per address, so failed
                // connections cannot block the diagnostics screen indefinitely.
                val reachable = addresses.take(3).any { address ->
                    coroutineContext.ensureActive()
                    try {
                        physicalNetwork.socketFactory.createSocket().use { socket ->
                            socket.connect(
                                InetSocketAddress(address, profile.port),
                                2_000
                            )
                        }
                        true
                    } catch (_: Exception) {
                        false
                    }
                }
                DiagnosticStep(
                    if (reachable) DiagnosticStatus.OK else DiagnosticStatus.FAILED,
                    if (reachable) DiagnosticMessage.TCP_REACHABLE else DiagnosticMessage.TCP_UNREACHABLE
                )
            }
        }

        ConnectionDiagnosticResult(
            protocol = profile.protocol.displayName,
            maskedHost = HostPrivacyMasker.mask(profile.host),
            port = profile.port,
            link = DiagnosticStep(DiagnosticStatus.OK, DiagnosticMessage.LINK_VALID),
            config = configStep,
            dns = dnsStep,
            tcp = tcpStep
        )
    }

    private fun findPhysicalNetwork(context: Context): Network? {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return null

        return runCatching {
            manager.allNetworks
                .mapNotNull { network ->
                    val capabilities = manager.getNetworkCapabilities(network)
                        ?: return@mapNotNull null
                    if (
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                        !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    ) {
                        return@mapNotNull null
                    }
                    val score =
                        (if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) 10 else 0) +
                        (if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) 3 else 0) +
                        (if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) 2 else 0) +
                        (if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) 1 else 0)
                    network to score
                }
                .maxByOrNull { it.second }
                ?.first
        }.getOrNull()
    }

    private fun isIpLiteral(host: String): Boolean {
        if (host.contains(':')) return true // IPv6 literal
        val octets = host.split('.')
        return octets.size == 4 && octets.all { part ->
            part.isNotEmpty() && part.all(Char::isDigit) &&
                part.toIntOrNull()?.let { it in 0..255 } == true
        }
    }
}
