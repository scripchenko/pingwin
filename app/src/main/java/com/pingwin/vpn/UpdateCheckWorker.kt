package com.pingwin.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class UpdateCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(
    appContext,
    workerParams
) {

    override fun doWork(): Result {
        if (
            !UpdateSettingsStore.isAutoCheckEnabled(
                applicationContext
            )
        ) {
            return Result.success()
        }

        return runCatching {
            val release =
                UpdateChecker.getLatestRelease()

            val updateAvailable =
                UpdateChecker.isNewerVersion(
                    remoteVersion =
                        release.version,
                    currentVersion =
                        BuildConfig.VERSION_NAME
                )

            if (updateAvailable) {
                UpdateSettingsStore.setAvailableVersion(
                    applicationContext,
                    release.version
                )
            } else {
                UpdateSettingsStore.clearAvailableVersion(
                    applicationContext
                )
            }

            val lastNotifiedVersion =
                UpdateSettingsStore.getLastNotifiedVersion(
                    applicationContext
                )

            if (
                updateAvailable &&
                lastNotifiedVersion != release.version
            ) {
                val notificationShown =
                    showUpdateNotification(
                        release.version
                    )

                if (notificationShown) {
                    UpdateSettingsStore.setLastNotifiedVersion(
                        applicationContext,
                        release.version
                    )
                }
            }

            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    private fun showUpdateNotification(
        version: String
    ): Boolean {
        createNotificationChannel()

        val intent =
            Intent(
                applicationContext,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP

                putExtra(
                    EXTRA_OPEN_UPDATES,
                    true
                )
            }

        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.stat_sys_download_done
                )
                .setContentTitle(
                    "Доступно обновление pingwin"
                )
                .setContentText(
                    "Новая версия: $version"
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .setContentIntent(
                    pendingIntent
                )
                .build()

        return try {
            NotificationManagerCompat
                .from(
                    applicationContext
                )
                .notify(
                    NOTIFICATION_ID,
                    notification
                )

            true
        } catch (
            _: SecurityException
        ) {
            false
        }
    }

    private fun createNotificationChannel() {
        val manager =
            applicationContext
                .getSystemService(
                    NotificationManager::class.java
                )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Обновления pingwin",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    companion object {
        const val WORK_NAME =
            "pingwin_update_check"

        const val EXTRA_OPEN_UPDATES =
            "open_updates"

        private const val CHANNEL_ID =
            "pingwin_updates"

        private const val NOTIFICATION_ID =
            3001
    }
}
