package ru.scripchenko.autovless

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ServerLocation(
    val countryCode: String,
    val flagEmoji: String
)

object ServerLocationResolver {

    private const val PREFS_NAME = "server_locations"

    fun resolve(
        context: Context,
        host: String
    ): ServerLocation? {
        val cached =
            loadCached(
                context,
                host
            )

        if (cached != null) {
            return cached
        }

        val connection =
            (
                URL(
                    "https://ipwho.is/$host" +
                        "?fields=success,country_code,flag.emoji"
                )
                    .openConnection() as HttpURLConnection
            ).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                setRequestProperty(
                    "Accept",
                    "application/json"
                )
            }

        return try {
            if (connection.responseCode !in 200..299) {
                return null
            }

            val body =
                connection.inputStream
                    .bufferedReader()
                    .use {
                        it.readText()
                    }

            val json =
                JSONObject(body)

            if (!json.optBoolean("success", false)) {
                return null
            }

            val countryCode =
                json.optString("country_code")
                    .trim()
                    .uppercase()

            val flagEmoji =
                json.optJSONObject("flag")
                    ?.optString("emoji")
                    ?.trim()
                    .orEmpty()

            if (countryCode.isBlank()) {
                return null
            }

            val location =
                ServerLocation(
                    countryCode = countryCode,
                    flagEmoji =
                        flagEmoji.ifBlank {
                            countryCodeToEmoji(
                                countryCode
                            )
                        }
                )

            saveCached(
                context,
                host,
                location
            )

            location
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun loadCached(
        context: Context,
        host: String
    ): ServerLocation? {
        val raw =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
                .getString(
                    host,
                    null
                )
                ?: return null

        return runCatching {
            val json =
                JSONObject(raw)

            ServerLocation(
                countryCode =
                    json.getString(
                        "countryCode"
                    ),
                flagEmoji =
                    json.getString(
                        "flagEmoji"
                    )
            )
        }.getOrNull()
    }

    private fun saveCached(
        context: Context,
        host: String,
        location: ServerLocation
    ) {
        val raw =
            JSONObject()
                .put(
                    "countryCode",
                    location.countryCode
                )
                .put(
                    "flagEmoji",
                    location.flagEmoji
                )
                .toString()

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                host,
                raw
            )
            .apply()
    }

    private fun countryCodeToEmoji(
        countryCode: String
    ): String {
        if (countryCode.length != 2) {
            return "🌐"
        }

        return countryCode
            .uppercase()
            .map {
                Character.toChars(
                    0x1F1E6 +
                        (it.code - 'A'.code)
                )
                    .concatToString()
            }
            .joinToString("")
    }
}
