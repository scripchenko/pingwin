package com.pingwin.vpn

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object UpdateScheduler {

    fun sync(
        context: Context
    ) {
        if (
            UpdateSettingsStore.isAutoCheckEnabled(
                context
            )
        ) {
            schedule(
                context
            )
        } else {
            cancel(
                context
            )
        }
    }

    private fun schedule(
        context: Context
    ) {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val request =
            PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                1,
                TimeUnit.DAYS
            )
                .setConstraints(
                    constraints
                )
                .build()

        WorkManager
            .getInstance(
                context
            )
            .enqueueUniquePeriodicWork(
                UpdateCheckWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    private fun cancel(
        context: Context
    ) {
        WorkManager
            .getInstance(
                context
            )
            .cancelUniqueWork(
                UpdateCheckWorker.WORK_NAME
            )
    }
}
