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
        when (routing.appMode) {
            AppRoutingMode.ALL_VIA_VPN -> {
                // No allowed/disallowed list:
                // Android routes all applications through the VPN.
                return
            }

            AppRoutingMode.ONLY_SELECTED_VIA_VPN -> {
                val allowedPackages =
                    (routing.packages + routing.siteRulePackages)
                        .asSequence()
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .distinct()
                        .sorted()
                        .toList()

                /*
                 * An empty Android allowed-app list means "all applications".
                 * Until MainActivity gets an explicit "nothing selected"
                 * guard, keep the VPN scoped to AutoVLESS itself so that
                 * zero selected apps does not accidentally capture the
                 * whole phone.
                 */
                if (allowedPackages.isEmpty()) {
                    allowApplication(
                        builder,
                        context.packageName
                    )
                    return
                }

                allowedPackages.forEach { packageName ->
                    allowApplication(
                        builder,
                        packageName
                    )
                }
            }

            AppRoutingMode.EXCLUDE_SELECTED_FROM_VPN -> {
                routing.packages
                    .asSequence()
                    .map(String::trim)
                    .filter(String::isNotEmpty)
                    .distinct()
                    .sorted()
                    .forEach { packageName ->
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
