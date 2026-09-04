package com.pingwin.vpn

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

data class UpdateDownloadState(
    val status: Int,
    val progress: Int?,
    val apkFile: File?
)

object UpdateInstaller {

    private const val PREFS_NAME =
        "pingwin_update_download"

    private const val KEY_DOWNLOAD_ID =
        "download_id"

    private const val NO_DOWNLOAD =
        -1L

    fun startDownload(
        context: Context,
        apkUrl: String
    ): Long {
        cancelCurrentDownload(
            context
        )

        val apkFile =
            getDownloadFile(
                context
            )

        apkFile.parentFile
            ?.mkdirs()

        if (apkFile.exists()) {
            apkFile.delete()
        }

        val request =
            DownloadManager.Request(
                Uri.parse(
                    apkUrl
                )
            )
                .setTitle(
                    "pingwin"
                )
                .setDescription(
                    "Downloading update"
                )
                .setMimeType(
                    "application/vnd.android.package-archive"
                )
                .setNotificationVisibility(
                    DownloadManager.Request
                        .VISIBILITY_VISIBLE
                )
                .setAllowedOverMetered(
                    true
                )
                .setAllowedOverRoaming(
                    true
                )
                .setDestinationInExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    "updates/pingwin-update.apk"
                )

        val manager =
            context.getSystemService(
                DownloadManager::class.java
            )

        val downloadId =
            manager.enqueue(
                request
            )

        prefs(context)
            .edit()
            .putLong(
                KEY_DOWNLOAD_ID,
                downloadId
            )
            .apply()

        return downloadId
    }

    fun getDownloadState(
        context: Context
    ): UpdateDownloadState? {
        val downloadId =
            getDownloadId(
                context
            )

        if (downloadId == NO_DOWNLOAD) {
            return null
        }

        val manager =
            context.getSystemService(
                DownloadManager::class.java
            )

        val query =
            DownloadManager.Query()
                .setFilterById(
                    downloadId
                )

        manager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) {
                clearStoredDownload(
                    context
                )

                return null
            }

            val status =
                cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                        DownloadManager.COLUMN_STATUS
                    )
                )

            val downloadedBytes =
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        DownloadManager
                            .COLUMN_BYTES_DOWNLOADED_SO_FAR
                    )
                )

            val totalBytes =
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        DownloadManager
                            .COLUMN_TOTAL_SIZE_BYTES
                    )
                )

            val progress =
                if (
                    totalBytes > 0L &&
                    downloadedBytes >= 0L
                ) {
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
                } else {
                    null
                }

            val apkFile =
                if (
                    status ==
                        DownloadManager.STATUS_SUCCESSFUL
                ) {
                    getDownloadFile(
                        context
                    )
                } else {
                    null
                }

            return UpdateDownloadState(
                status = status,
                progress = progress,
                apkFile = apkFile
            )
        }
    }

    fun getDownloadId(
        context: Context
    ): Long =
        prefs(context)
            .getLong(
                KEY_DOWNLOAD_ID,
                NO_DOWNLOAD
            )

    fun clearStoredDownload(
        context: Context
    ) {
        prefs(context)
            .edit()
            .remove(
                KEY_DOWNLOAD_ID
            )
            .apply()
    }

    fun cancelCurrentDownload(
        context: Context
    ) {
        val downloadId =
            getDownloadId(
                context
            )

        if (downloadId != NO_DOWNLOAD) {
            context
                .getSystemService(
                    DownloadManager::class.java
                )
                .remove(
                    downloadId
                )
        }

        clearStoredDownload(
            context
        )
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

        context.startActivity(
            intent
        )
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

        context.startActivity(
            intent
        )
    }

    private fun getDownloadFile(
        context: Context
    ): File =
        File(
            context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS
            ),
            "updates/pingwin-update.apk"
        )

    private fun prefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
}
