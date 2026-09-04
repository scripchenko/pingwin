package com.pingwin.vpn

import android.os.Build
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateRelease(
    val version: String,
    val apkUrl: String,
    val releaseUrl: String,
    val releaseNotes: String
)

object UpdateChecker {

    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/scripchenko/pingwin/releases/latest"

    private data class ApkAsset(
        val name: String,
        val url: String
    )

    fun getLatestRelease(): UpdateRelease {
        val connection =
            (URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 15_000
                setRequestProperty(
                    "Accept",
                    "application/vnd.github+json"
                )
                setRequestProperty(
                    "User-Agent",
                    "pingwin-android"
                )
                setRequestProperty(
                    "X-GitHub-Api-Version",
                    "2022-11-28"
                )
            }

        try {
            val responseCode =
                connection.responseCode

            if (responseCode !in 200..299) {
                error(
                    "GitHub API returned HTTP $responseCode"
                )
            }

            val json =
                JSONObject(
                    connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }
                )

            val version =
                json.getString("tag_name")
                    .removePrefix("v")

            val assetsJson =
                json.getJSONArray("assets")

            val apkAssets =
                buildList {
                    for (
                        index in 0 until
                            assetsJson.length()
                    ) {
                        val asset =
                            assetsJson.getJSONObject(
                                index
                            )

                        val name =
                            asset.getString(
                                "name"
                            )

                        if (
                            name.endsWith(
                                ".apk",
                                ignoreCase = true
                            )
                        ) {
                            add(
                                ApkAsset(
                                    name = name,
                                    url =
                                        asset.getString(
                                            "browser_download_url"
                                        )
                                )
                            )
                        }
                    }
                }

            require(
                apkAssets.isNotEmpty()
            ) {
                "The latest GitHub release does not contain an APK"
            }

            val selectedAsset =
                selectApkAsset(
                    assets = apkAssets,
                    supportedAbis =
                        Build.SUPPORTED_ABIS
                            .toList()
                )

            return UpdateRelease(
                version = version,
                apkUrl = selectedAsset.url,
                releaseUrl =
                    json.getString(
                        "html_url"
                    ),
                releaseNotes =
                    json.optString(
                        "body"
                    )
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun selectApkAsset(
        assets: List<ApkAsset>,
        supportedAbis: List<String>
    ): ApkAsset {
        if (assets.size == 1) {
            return assets.first()
        }

        fun findByName(
            part: String
        ): ApkAsset? =
            assets.firstOrNull {
                it.name.contains(
                    part,
                    ignoreCase = true
                )
            }

        if (
            supportedAbis.any {
                it.equals(
                    "arm64-v8a",
                    ignoreCase = true
                )
            }
        ) {
            findByName(
                "arm64-v8a"
            )?.let {
                return it
            }
        }

        if (
            supportedAbis.any {
                it.equals(
                    "armeabi-v7a",
                    ignoreCase = true
                )
            }
        ) {
            findByName(
                "armeabi-v7a"
            )?.let {
                return it
            }
        }

        findByName(
            "universal"
        )?.let {
            return it
        }

        return assets.first()
    }

    fun isNewerVersion(
        remoteVersion: String,
        currentVersion: String
    ): Boolean {
        val remote =
            versionNumbers(
                remoteVersion
            )

        val current =
            versionNumbers(
                currentVersion
            )

        val size =
            maxOf(
                remote.size,
                current.size
            )

        for (index in 0 until size) {
            val remotePart =
                remote.getOrElse(index) {
                    0
                }

            val currentPart =
                current.getOrElse(index) {
                    0
                }

            if (remotePart > currentPart) {
                return true
            }

            if (remotePart < currentPart) {
                return false
            }
        }

        return false
    }

    private fun versionNumbers(
        version: String
    ): List<Int> =
        Regex("""\d+""")
            .findAll(version)
            .map {
                it.value.toInt()
            }
            .toList()
}
