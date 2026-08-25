package com.pingwin.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AutomationBootReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                AutomationService.sync(context)
            }
        }
    }
}
