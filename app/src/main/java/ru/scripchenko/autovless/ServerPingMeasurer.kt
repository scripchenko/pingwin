package ru.scripchenko.autovless

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress

object ServerPingMeasurer {

    suspend fun measure(
        context: Context,
        host: String,
        port: Int,
        attempts: Int = 3
    ): Int? =
        withContext(Dispatchers.IO) {
            val connectivityManager =
                context.getSystemService(
                    ConnectivityManager::class.java
                )

            val physicalNetwork =
                connectivityManager.allNetworks
                    .firstOrNull { network ->
                        val capabilities =
                            connectivityManager
                                .getNetworkCapabilities(network)
                                ?: return@firstOrNull false

                        capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_INTERNET
                        ) &&
                            !capabilities.hasTransport(
                                NetworkCapabilities.TRANSPORT_VPN
                            )
                    }
                    ?: return@withContext null

            val measurements =
                buildList {
                    repeat(attempts) {
                        val socket =
                            physicalNetwork
                                .socketFactory
                                .createSocket()

                        try {
                            val started =
                                SystemClock.elapsedRealtimeNanos()

                            socket.connect(
                                InetSocketAddress(
                                    host,
                                    port
                                ),
                                2500
                            )

                            val elapsedMs =
                                (
                                    SystemClock.elapsedRealtimeNanos() -
                                        started
                                    ) / 1_000_000L

                            add(
                                elapsedMs
                                    .coerceAtLeast(1L)
                                    .coerceAtMost(Int.MAX_VALUE.toLong())
                                    .toInt()
                            )
                        } catch (_: Exception) {
                        } finally {
                            runCatching {
                                socket.close()
                            }
                        }
                    }
                }

            measurements
                .sorted()
                .let { values ->
                    if (values.isEmpty()) {
                        null
                    } else {
                        values[
                            values.size / 2
                        ]
                    }
                }
        }
}
