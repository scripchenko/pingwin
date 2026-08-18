package ru.scripchenko.autovless

import android.content.Context
import android.content.Intent
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

                InstalledApp(
                    label = label,
                    packageName = packageName
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
