package ru.scripchenko.autovless

import android.content.Context

fun Throwable.localizedVpnMessage(
    context: Context
): String =
    when (this) {
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

        else ->
            message
                ?: javaClass.simpleName
    }
