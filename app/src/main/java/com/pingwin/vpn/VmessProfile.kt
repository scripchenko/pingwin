package com.pingwin.vpn

import android.util.Base64
import org.json.JSONObject

enum class VmessParseError {
    INVALID_SCHEME,
    INVALID_DATA,
    MISSING_UUID,
    MISSING_HOST,
    INVALID_PORT
}

class VmessParseException(
    val error: VmessParseError
) : IllegalArgumentException()

data class VmessProfile(
    val uuid: String,
    override val host: String,
    override val port: Int,
    val alterId: Int,
    val security: String,
    val network: String?,
    val transportHost: String?,
    val path: String?,
    val serviceName: String?,
    val tls: String?,
    val serverName: String?,
    val fingerprint: String?,
    val alpn: String?,
    override val name: String?
) : ConnectionProfile {

    override val protocol =
        ConnectionProtocol.VMESS

    companion object {

        fun parse(
            link: String
        ): VmessProfile {
            val trimmed =
                link.trim()

            if (
                !trimmed.startsWith(
                    "vmess://",
                    ignoreCase = true
                )
            ) {
                throw VmessParseException(
                    VmessParseError.INVALID_SCHEME
                )
            }

            val payload =
                trimmed.substringAfter("://")
                    .trim()

            val jsonText =
                decodeBase64(payload)
                    ?: throw VmessParseException(
                        VmessParseError.INVALID_DATA
                    )

            val json =
                runCatching {
                    JSONObject(jsonText)
                }.getOrElse {
                    throw VmessParseException(
                        VmessParseError.INVALID_DATA
                    )
                }

            val uuid =
                json.optString("id")
                    .trim()
                    .takeIf(String::isNotEmpty)
                    ?: throw VmessParseException(
                        VmessParseError.MISSING_UUID
                    )

            val host =
                json.optString("add")
                    .trim()
                    .takeIf(String::isNotEmpty)
                    ?: throw VmessParseException(
                        VmessParseError.MISSING_HOST
                    )

            val port =
                json.optString("port")
                    .toIntOrNull()
                    ?.takeIf { it in 1..65535 }
                    ?: throw VmessParseException(
                        VmessParseError.INVALID_PORT
                    )

            val network =
                json.optString("net")
                    .trim()
                    .takeIf(String::isNotEmpty)

            val path =
                json.optString("path")
                    .takeIf(String::isNotEmpty)

            return VmessProfile(
                uuid = uuid,
                host = host,
                port = port,
                alterId =
                    json.optString("aid")
                        .toIntOrNull()
                        ?: 0,
                security =
                    json.optString("scy")
                        .trim()
                        .takeIf(String::isNotEmpty)
                        ?: "auto",
                network = network,
                transportHost =
                    json.optString("host")
                        .takeIf(String::isNotEmpty),
                path = path,
                serviceName =
                    if (
                        network.equals(
                            "grpc",
                            ignoreCase = true
                        )
                    ) {
                        path
                    } else {
                        null
                    },
                tls =
                    json.optString("tls")
                        .trim()
                        .takeIf(String::isNotEmpty),
                serverName =
                    json.optString("sni")
                        .trim()
                        .takeIf(String::isNotEmpty),
                fingerprint =
                    json.optString("fp")
                        .trim()
                        .takeIf(String::isNotEmpty),
                alpn =
                    json.optString("alpn")
                        .trim()
                        .takeIf(String::isNotEmpty),
                name =
                    json.optString("ps")
                        .trim()
                        .takeIf(String::isNotEmpty)
            )
        }

        private fun decodeBase64(
            value: String
        ): String? =
            runCatching {
                val normalized =
                    value
                        .replace('-', '+')
                        .replace('_', '/')

                val padded =
                    normalized +
                        "=".repeat(
                            (4 - normalized.length % 4) % 4
                        )

                String(
                    Base64.decode(
                        padded,
                        Base64.DEFAULT
                    ),
                    Charsets.UTF_8
                )
            }.getOrNull()
    }
}
