package com.pingwin.vpn

object ConnectionConfigBuilder {

    fun build(
        profile: ConnectionProfile,
        routing: RoutingSettings = RoutingSettings(),
        detailedLogging: Boolean = false
    ): String =
        when (profile) {
            is VlessProfile ->
                SingBoxConfigBuilder.build(
                    profile = profile,
                    routing = routing,
                    detailedLogging = detailedLogging
                )

            is Hysteria2Profile ->
                Hysteria2ConfigBuilder.build(
                    profile = profile,
                    routing = routing,
                    detailedLogging = detailedLogging
                )

            is TrojanProfile ->
                TrojanConfigBuilder.build(
                    profile = profile,
                    routing = routing,
                    detailedLogging = detailedLogging
                )

            is TuicProfile ->
                TuicConfigBuilder.build(
                    profile = profile,
                    routing = routing,
                    detailedLogging = detailedLogging
                )

            is ShadowsocksProfile ->
                ShadowsocksConfigBuilder.build(
                    profile = profile,
                    routing = routing,
                    detailedLogging = detailedLogging
                )

            is VmessProfile ->
                VmessConfigBuilder.build(
                    profile = profile,
                    routing = routing,
                    detailedLogging = detailedLogging
                )
        }
}
