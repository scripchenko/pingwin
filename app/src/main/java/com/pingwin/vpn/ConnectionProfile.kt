package com.pingwin.vpn

enum class ConnectionProtocol(
    val displayName: String
) {
    VLESS("VLESS"),
    HYSTERIA2("Hysteria2"),
    TROJAN("Trojan"),
    TUIC("TUIC"),
    SHADOWSOCKS("Shadowsocks"),
    VMESS("VMess")
}

sealed interface ConnectionProfile {
    val protocol: ConnectionProtocol
    val host: String
    val port: Int
    val name: String?
}
