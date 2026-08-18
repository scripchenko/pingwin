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
        val directPackages =
            routing.directPackages
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
                .sorted()
                .toList()

        val usesNewModel =
            routing.directPackages.isNotEmpty() ||
                    routing.siteRulePackages.isNotEmpty()

        if (usesNewModel) {
            directPackages.forEach { packageName ->
                excludeApplication(
                    builder,
                    packageName
                )
            }
            return
        }

        // Temporary compatibility with the previous routing UI.
        val legacyPackages =
            routing.packages
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
                .sorted()
                .toList()

        if (
            legacyPackages.isEmpty() ||
            routing.appMode == AppRoutingMode.ALL_VIA_VPN
        ) {
            return
        }

        when (routing.appMode) {
            AppRoutingMode.ALL_VIA_VPN -> Unit

            AppRoutingMode.ONLY_SELECTED_VIA_VPN -> {
                legacyPackages.forEach { packageName ->
                    allowApplication(
                        builder,
                        packageName
                    )
                }
            }

            AppRoutingMode.EXCLUDE_SELECTED_FROM_VPN -> {
                legacyPackages.forEach { packageName ->
                    excludeApplication(
                        builder,
                        packageName
                    )
                }
            }
        }
    }

    private fun allowApplication(
        builder: VpnService.Builder,
        packageName: String
    ) {
        try {
            builder.addAllowedApplication(
                packageName
            )
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Unable to allow application $packageName",
                e
            )
        }
    }

    private fun excludeApplication(
        builder: VpnService.Builder,
        packageName: String
    ) {
        try {
            builder.addDisallowedApplication(
                packageName
            )
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Unable to exclude application $packageName",
                e
            )
        }
    }
}
