package com.pingwin.vpn

import android.app.Activity
import android.content.Context
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import java.util.UUID

@TaskerInputRoot
class AutomationTokenInput {
    @field:TaskerInputField(
        key = "pingwin_automation_token",
        ignoreInStringBlurb = true
    )
    var token: String = ""
}

object AutomationPluginSecurity {
    private const val PREFS_NAME = "pingwin_automation_plugin"
    private const val KEY_TOKEN = "token"

    private val trustedConfigPackages =
        setOf(
            "com.arlosoft.macrodroid",
            "net.dinglisch.android.taskerm"
        )

    fun isTrustedConfigCaller(
        activity: Activity
    ): Boolean {
        val caller =
            activity.callingPackage
                ?: activity.callingActivity?.packageName
                ?: return false

        return caller in trustedConfigPackages
    }

    fun getOrCreateToken(
        context: Context
    ): String {
        val prefs =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val existing =
            prefs.getString(
                KEY_TOKEN,
                null
            )

        if (!existing.isNullOrBlank()) {
            return existing
        }

        val token =
            UUID.randomUUID().toString()

        prefs.edit()
            .putString(
                KEY_TOKEN,
                token
            )
            .apply()

        return token
    }

    fun isValid(
        context: Context,
        input: AutomationTokenInput
    ): Boolean {
        val expected =
            getOrCreateToken(context)

        return input.token.isNotBlank() &&
            input.token == expected
    }
}
