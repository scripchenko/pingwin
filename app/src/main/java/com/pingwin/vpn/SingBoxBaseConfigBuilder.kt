package com.pingwin.vpn

object SingBoxBaseConfigBuilder {

    fun build(
        proxyOutbound: String,
        routing: RoutingSettings = RoutingSettings(),
        detailedLogging: Boolean = false
    ): String {
        val logLevel =
            if (detailedLogging) {
                "debug"
            } else {
                "info"
            }

        val outbound =
            indentBlock(
                proxyOutbound,
                4
            )

        return """
            {
              "log": {
                "level": "$logLevel",
                "timestamp": true
              },
              "dns": {
                "servers": [
                  {
                    "type": "local",
                    "tag": "local"
                  }
                ]
              },
              "inbounds": [
                {
                  "type": "tun",
                  "tag": "tun-in",
                  "address": [
                    "172.19.0.1/30"
                  ],
                  "auto_route": true,
                  "strict_route": true,
                  "stack": "gvisor"
                }
              ],
              "outbounds": [
$outbound,
                {
                  "type": "direct",
                  "tag": "direct"
                }
              ],
              "route": ${SingBoxRoutingConfigBuilder.build(routing)}
            }
        """.trimIndent()
    }

    private fun indentBlock(
        value: String,
        spaces: Int
    ): String {
        val indent =
            " ".repeat(spaces)

        return value
            .lineSequence()
            .joinToString("\n") {
                indent + it
            }
    }
}
