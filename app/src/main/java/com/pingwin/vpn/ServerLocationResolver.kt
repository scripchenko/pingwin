package com.pingwin.vpn

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.util.Locale

data class ServerLocation(
    val countryCode: String,
    val countryName: String,
    val flagEmoji: String
)

object ServerLocationResolver {

    private const val PREFS_NAME = "server_locations_v2"
    private const val CACHE_TTL_MS = 24L * 60L * 60L * 1000L

    fun resolve(
        context: Context,
        host: String
    ): ServerLocation? {
        val normalizedHost =
            host.trim()
                .removePrefix("[")
                .removeSuffix("]")
                .lowercase()

        if (normalizedHost.isBlank()) {
            return null
        }

        loadCached(
            context,
            normalizedHost
        )?.let {
            return it
        }

        val ipAddress =
            runCatching {
                InetAddress
                    .getByName(normalizedHost)
                    .hostAddress
            }.getOrNull()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return null

        val countryCodes =
            listOfNotNull(
                resolveWithIpWho(ipAddress),
                resolveWithIpApi(ipAddress),
                resolveWithCountryIs(ipAddress)
            )

        val countryCode =
            chooseCountryCode(countryCodes)
                ?: return null

        val location =
            buildLocation(countryCode)

        saveCached(
            context,
            normalizedHost,
            location
        )

        return location
    }

    private fun resolveWithIpWho(
        ipAddress: String
    ): String? {
        val body =
            httpGet(
                "https://ipwho.is/$ipAddress" +
                    "?fields=success,country_code"
            ) ?: return null

        return runCatching {
            val json =
                JSONObject(body)

            if (!json.optBoolean("success", false)) {
                null
            } else {
                normalizeCountryCode(
                    json.optString("country_code")
                )
            }
        }.getOrNull()
    }

    private fun resolveWithIpApi(
        ipAddress: String
    ): String? {
        val body =
            httpGet(
                "https://ipapi.co/$ipAddress/country/"
            ) ?: return null

        return normalizeCountryCode(body)
    }

    private fun resolveWithCountryIs(
        ipAddress: String
    ): String? {
        val body =
            httpGet(
                "https://api.country.is/$ipAddress"
            ) ?: return null

        return runCatching {
            normalizeCountryCode(
                JSONObject(body)
                    .optString("country")
            )
        }.getOrNull()
    }

    private fun httpGet(
        url: String
    ): String? {
        val connection =
            (
                URL(url)
                    .openConnection() as HttpURLConnection
            ).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                setRequestProperty(
                    "Accept",
                    "application/json,text/plain"
                )
                setRequestProperty(
                    "User-Agent",
                    "pingwin/${BuildConfig.VERSION_NAME}"
                )
            }

        return try {
            if (connection.responseCode !in 200..299) {
                null
            } else {
                connection.inputStream
                    .bufferedReader()
                    .use {
                        it.readText()
                    }
                    .trim()
                    .takeIf {
                        it.isNotBlank()
                    }
            }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun chooseCountryCode(
        countryCodes: List<String>
    ): String? {
        val valid =
            countryCodes
                .mapNotNull {
                    normalizeCountryCode(it)
                }

        if (valid.isEmpty()) {
            return null
        }

        val counts =
            valid.groupingBy {
                it
            }.eachCount()

        val winner =
            counts.maxByOrNull {
                it.value
            }

        if (
            winner != null &&
            winner.value >= 2
        ) {
            return winner.key
        }

        return if (valid.size == 1) {
            valid.first()
        } else {
            null
        }
    }

    private fun normalizeCountryCode(
        value: String
    ): String? {
        val countryCode =
            value.trim()
                .uppercase(Locale.US)

        return countryCode
            .takeIf {
                it.length == 2 &&
                    it.all(Char::isLetter)
            }
    }

    private fun buildLocation(
        countryCode: String
    ): ServerLocation {
        val countryName =
            runCatching {
                Locale.Builder()
                    .setRegion(countryCode)
                    .build()
                    .getDisplayCountry(
                        Locale.getDefault()
                    )
            }.getOrNull()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: countryCode

        return ServerLocation(
            countryCode = countryCode,
            countryName = countryName,
            flagEmoji =
                countryCodeToEmoji(
                    countryCode
                )
        )
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

            val savedAt =
                json.optLong(
                    "savedAt",
                    0L
                )

            if (
                savedAt <= 0L ||
                System.currentTimeMillis() - savedAt >
                    CACHE_TTL_MS
            ) {
                return@runCatching null
            }

            val countryCode =
                normalizeCountryCode(
                    json.getString(
                        "countryCode"
                    )
                ) ?: return@runCatching null

            buildLocation(countryCode)
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
                    "savedAt",
                    System.currentTimeMillis()
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
            .uppercase(Locale.US)
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
