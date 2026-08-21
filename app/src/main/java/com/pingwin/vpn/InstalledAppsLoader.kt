package com.pingwin.vpn

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

object InstalledAppsLoader {

    fun load(context: Context): List<InstalledApp> {
        val packageManager = context.packageManager

        val intent =
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

        val activities =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(
                    intent,
                    0
                )
            }

        return activities
            .asSequence()
            .map { resolveInfo ->
                val packageName =
                    resolveInfo.activityInfo.packageName

                val label =
                    resolveInfo.loadLabel(packageManager)
                        ?.toString()
                        ?.trim()
                        .orEmpty()
                        .ifEmpty { packageName }

                val applicationInfo =
                    resolveInfo.activityInfo.applicationInfo

                val systemFlags =
                    ApplicationInfo.FLAG_SYSTEM or
                        ApplicationInfo.FLAG_UPDATED_SYSTEM_APP

                val isSystem =
                    applicationInfo.flags and systemFlags != 0

                InstalledApp(
                    label = label,
                    packageName = packageName,
                    isSystem = isSystem
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}