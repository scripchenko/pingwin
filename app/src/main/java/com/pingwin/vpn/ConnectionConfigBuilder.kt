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
        }
}
