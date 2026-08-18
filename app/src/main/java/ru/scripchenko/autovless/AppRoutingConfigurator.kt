package ru.scripchenko.autovless

import android.content.Context
import android.net.VpnService

object AppRoutingConfigurator {

    fun apply(
        context: Context,
        builder: VpnService.Builder,
        routing: RoutingSettings
    ) {
        /*
         * Intentionally empty.
         *
         * Every application is allowed into the Android VPN tunnel.
         * sing-box is the single place that decides whether a connection
         * uses the VLESS proxy or the direct outbound.
         *
         * This is required so domain rules work independently of which
         * browser or application opened the site.
         */
    }
}
