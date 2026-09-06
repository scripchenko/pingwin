package com.pingwin.vpn

object HostPrivacyMasker {
    fun mask(host: String): String {
        val value = host.trim()

        if (value.isEmpty()) {
            return value
        }

        val ipv4Parts = value.split('.')
        if (
            ipv4Parts.size == 4 &&
            ipv4Parts.all { part ->
                part.toIntOrNull()?.let { it in 0..255 } == true
            }
        ) {
            return "${ipv4Parts[0]}.***.***.${ipv4Parts[3]}"
        }

        if (value.contains(':')) {
            val parts = value.split(':')
            val visible =
                parts
                    .filter { it.isNotBlank() }
                    .take(2)

            return if (visible.isNotEmpty()) {
                visible.joinToString(":") + ":****:****"
            } else {
                "****:****"
            }
        }

        val labels =
            value
                .split('.')
                .filter { it.isNotBlank() }

        return when {
            labels.size >= 3 ->
                "${labels.first()}.***.${labels.last()}"

            labels.size == 2 ->
                "***.${labels.last()}"

            else ->
                "***"
        }
    }

    fun displayConnectionName(
        name: String,
        profile: ConnectionProfile?
    ): String {
        if (profile == null || profile.host.isBlank()) {
            return name
        }

        val defaultName =
            "${profile.protocol.displayName} · ${profile.host}"

        return if (name == defaultName) {
            "${profile.protocol.displayName} · ${mask(profile.host)}"
        } else {
            name
        }
    }
}
