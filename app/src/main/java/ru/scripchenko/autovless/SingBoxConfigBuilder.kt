package ru.scripchenko.autovless

object SingBoxConfigBuilder {

    fun build(profile: VlessProfile): String {
        require(profile.security == "reality") {
            "Пока поддерживается только security=reality"
        }

        require(profile.network == "tcp") {
            "Пока поддерживается только type=tcp"
        }

        require(!profile.publicKey.isNullOrBlank()) {
            "Для Reality отсутствует public key (pbk)"
        }

        require(!profile.serverName.isNullOrBlank()) {
            "Для Reality отсутствует server name (sni)"
        }

        return """
            {
              "log": {
                "level": "info",
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
                  "stack": "system"
                }
              ],
              "outbounds": [
                {
                  "type": "vless",
                  "tag": "proxy",
                  "server": "${jsonEscape(profile.host)}",
                  "server_port": ${profile.port},
                  "uuid": "${jsonEscape(profile.uuid)}",
                  "flow": "${jsonEscape(profile.flow ?: "")}",
                  "tls": {
                    "enabled": true,
                    "server_name": "${jsonEscape(profile.serverName)}",
                    "utls": {
                      "enabled": true,
                      "fingerprint": "${jsonEscape(profile.fingerprint ?: "chrome")}"
                    },
                    "reality": {
                      "enabled": true,
                      "public_key": "${jsonEscape(profile.publicKey)}",
                      "short_id": "${jsonEscape(profile.shortId ?: "")}"
                    }
                  }
                },
                {
                  "type": "direct",
                  "tag": "direct"
                }
              ],
              "route": {
                "auto_detect_interface": true,
                "final": "proxy"
              }
            }
        """.trimIndent()
    }

    private fun jsonEscape(value: String): String =
        buildString {
            value.forEach { character ->
                when (character) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (character.code < 0x20) {
                            append("\\u%04x".format(character.code))
                        } else {
                            append(character)
                        }
                    }
                }
            }
        }
}