package com.pingwin.vpn

import android.content.Context

fun Throwable.localizedVpnMessage(
    context: Context
): String =
    when (this) {
        is ConnectionParseException ->
            context.getString(
                R.string.connection_error_unsupported_protocol
            )

        is VlessParseException ->
            context.getString(
                when (error) {
                    VlessParseError.INVALID_SCHEME ->
                        R.string.vless_error_invalid_scheme

                    VlessParseError.MISSING_UUID ->
                        R.string.vless_error_missing_uuid

                    VlessParseError.MISSING_HOST ->
                        R.string.vless_error_missing_host
                }
            )

        is Hysteria2ParseException ->
            context.getString(
                when (error) {
                    Hysteria2ParseError.INVALID_SCHEME ->
                        R.string.hysteria2_error_invalid_scheme

                    Hysteria2ParseError.MISSING_HOST ->
                        R.string.hysteria2_error_missing_host

                    Hysteria2ParseError.INVALID_PORT ->
                        R.string.hysteria2_error_invalid_port
                }
            )

        is SingBoxConfigException ->
            context.getString(
                when (error) {
                    SingBoxConfigError.UNSUPPORTED_SECURITY ->
                        R.string.singbox_error_unsupported_security

                    SingBoxConfigError.UNSUPPORTED_NETWORK ->
                        R.string.singbox_error_unsupported_network

                    SingBoxConfigError.MISSING_PUBLIC_KEY ->
                        R.string.singbox_error_missing_public_key

                    SingBoxConfigError.MISSING_SERVER_NAME ->
                        R.string.singbox_error_missing_server_name
                }
            )

        is Hysteria2ConfigException ->
            context.getString(
                when (error) {
                    Hysteria2ConfigError.UNSUPPORTED_OBFS ->
                        R.string.hysteria2_error_unsupported_obfs

                    Hysteria2ConfigError.MISSING_OBFS_PASSWORD ->
                        R.string.hysteria2_error_missing_obfs_password

                    Hysteria2ConfigError.UNSUPPORTED_CERTIFICATE_PIN ->
                        R.string.hysteria2_error_unsupported_pin

                    Hysteria2ConfigError.UNSUPPORTED_ECH ->
                        R.string.hysteria2_error_unsupported_ech
                }
            )

        else ->
            message
                ?: javaClass.simpleName
    }
