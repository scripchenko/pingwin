package ru.scripchenko.autovless

import android.content.Context
import android.net.VpnService
import android.util.Log

object AppRoutingConfigurator {

    private const val TAG = "AutoVLESS"

    fun apply(
        context: Context,
        builder: VpnService.Builder,
        routing: RoutingSettings
    ) {
        val packages =
            routing.packages
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
                .sorted()
                .toList()

        if (packages.isEmpty() || routing.appMode == AppRoutingMode.ALL_VIA_VPN) {
            return
        }

        when (routing.appMode) {
            AppRoutingMode.ALL_VIA_VPN -> Unit

            AppRoutingMode.ONLY_SELECTED_VIA_VPN -> {
                packages.forEach { packageName ->
                    try {
                        builder.addAllowedApplication(packageName)
                    } catch (e: Exception) {
                        Log.w(
                            TAG,
                            "Unable to allow application $packageName",
                            e
                        )
                    }
                }
            }

            AppRoutingMode.EXCLUDE_SELECTED_FROM_VPN -> {
                packages.forEach { packageName ->
                    try {
                        builder.addDisallowedApplication(packageName)
                    } catch (e: Exception) {
                        Log.w(
                            TAG,
                            "Unable to exclude application $packageName",
                            e
                        )
                    }
                }
            }
        }
    }
}
