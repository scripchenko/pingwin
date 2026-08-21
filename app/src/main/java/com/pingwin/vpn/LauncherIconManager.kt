package com.pingwin.vpn

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object LauncherIconManager {

    private const val BLUE_ALIAS =
        "com.pingwin.vpn.LauncherBlue"

    private const val GREEN_ALIAS =
        "com.pingwin.vpn.LauncherGreen"

    fun showBlue(
        context: Context
    ) {
        setState(
            context = context,
            blueEnabled = true,
            greenEnabled = false
        )
    }

    fun showGreen(
        context: Context
    ) {
        setState(
            context = context,
            blueEnabled = false,
            greenEnabled = true
        )
    }

    private fun setState(
        context: Context,
        blueEnabled: Boolean,
        greenEnabled: Boolean
    ) {
        val packageManager =
            context.packageManager

        setComponentState(
            packageManager = packageManager,
            componentName =
                ComponentName(
                    context,
                    BLUE_ALIAS
                ),
            enabled = blueEnabled
        )

        setComponentState(
            packageManager = packageManager,
            componentName =
                ComponentName(
                    context,
                    GREEN_ALIAS
                ),
            enabled = greenEnabled
        )
    }

    private fun setComponentState(
        packageManager: PackageManager,
        componentName: ComponentName,
        enabled: Boolean
    ) {
        val targetState =
            if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

        if (
            packageManager.getComponentEnabledSetting(
                componentName
            ) == targetState
        ) {
            return
        }

        packageManager.setComponentEnabledSetting(
            componentName,
            targetState,
            PackageManager.DONT_KILL_APP
        )
    }
}
