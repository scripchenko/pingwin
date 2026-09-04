package com.pingwin.vpn

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateInstaller {

    fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Int?) -> Unit = {}
    ): File {
        val updatesDir =
            File(
                context.cacheDir,
                "updates"
            ).apply {
                mkdirs()
            }

        val apkFile =
            File(
                updatesDir,
                "pingwin-update.apk"
            )

        val connection =
            (URL(apkUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 60_000
                instanceFollowRedirects = true
                setRequestProperty(
                    "User-Agent",
                    "pingwin-android"
                )
            }

        try {
            val responseCode =
                connection.responseCode

            if (responseCode !in 200..299) {
                error(
                    "APK download returned HTTP $responseCode"
                )
            }

            val totalBytes =
                connection.contentLengthLong

            var downloadedBytes =
                0L

            var lastProgress =
                -1

            onProgress(
                if (totalBytes > 0L) {
                    0
                } else {
                    null
                }
            )

            connection.inputStream.use { input ->
                apkFile.outputStream().use { output ->
                    val buffer =
                        ByteArray(
                            64 * 1024
                        )

                    while (true) {
                        val read =
                            input.read(
                                buffer
                            )

                        if (read < 0) {
                            break
                        }

                        output.write(
                            buffer,
                            0,
                            read
                        )

                        downloadedBytes +=
                            read

                        if (totalBytes > 0L) {
                            val progress =
                                (
                                    downloadedBytes *
                                        100L /
                                        totalBytes
                                    )
                                    .toInt()
                                    .coerceIn(
                                        0,
                                        100
                                    )

                            if (
                                progress !=
                                    lastProgress
                            ) {
                                lastProgress =
                                    progress

                                onProgress(
                                    progress
                                )
                            }
                        }
                    }
                }
            }

            require(
                apkFile.exists() &&
                    apkFile.length() > 0L
            ) {
                "Downloaded APK is empty"
            }

            if (totalBytes > 0L) {
                onProgress(
                    100
                )
            }

            return apkFile
        } finally {
            connection.disconnect()
        }
    }

    fun canInstallPackages(
        context: Context
    ): Boolean {
        return if (
            Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
        ) {
            context.packageManager
                .canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermission(
        context: Context
    ) {
        if (
            Build.VERSION.SDK_INT <
                Build.VERSION_CODES.O
        ) {
            return
        }

        val intent =
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse(
                    "package:${context.packageName}"
                )
            ).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

        context.startActivity(intent)
    }

    fun installApk(
        context: Context,
        apkFile: File
    ) {
        val apkUri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

        val intent =
            Intent(
                Intent.ACTION_VIEW
            ).apply {
                setDataAndType(
                    apkUri,
                    "application/vnd.android.package-archive"
                )
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

        context.startActivity(intent)
    }
}
