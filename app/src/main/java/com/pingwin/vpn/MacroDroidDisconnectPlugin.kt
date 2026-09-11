package com.pingwin.vpn

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess

class MacroDroidDisconnectRunner :
    TaskerPluginRunnerActionNoOutput<
        AutomationTokenInput
    >() {

    override fun run(
        context: Context,
        input: TaskerInput<AutomationTokenInput>
    ): TaskerPluginResult<Unit> {

        if (
            !AutomationPluginSecurity.isValid(
                context,
                input.regular
            )
        ) {
            throw SecurityException(
                "Unauthorized automation request"
            )
        }

        val state =
            VpnStatus.state.value

        if (
            state == VpnConnectionState.DISCONNECTED ||
            state == VpnConnectionState.ERROR
        ) {
            return TaskerPluginResultSucess()
        }

        DiagnosticLogStore.append(
            context,
            context.getString(
                R.string.macrodroid_log_disconnect
            )
        )

        AutoVlessVpnService.stop(
            context
        )

        return TaskerPluginResultSucess()
    }
}

class MacroDroidDisconnectHelper(
    private val pluginConfig:
        TaskerPluginConfig<AutomationTokenInput>
) :
    TaskerPluginConfigHelperNoOutput<
        AutomationTokenInput,
        MacroDroidDisconnectRunner
    >(pluginConfig) {

    override val inputClass =
        AutomationTokenInput::class.java

    override val runnerClass =
        MacroDroidDisconnectRunner::class.java

    override fun addToStringBlurb(
        input: TaskerInput<AutomationTokenInput>,
        blurbBuilder: StringBuilder
    ) {
        blurbBuilder.append(
            pluginConfig.context.getString(
                R.string.macrodroid_blurb_disconnect
            )
        )
    }
}

class MacroDroidDisconnectActivity :
    Activity(),
    TaskerPluginConfig<AutomationTokenInput> {

    override val context
        get() =
            applicationContext

    override fun assignFromInput(
        input: TaskerInput<AutomationTokenInput>
    ) = Unit

    override val inputForTasker:
        TaskerInput<AutomationTokenInput>
        get() =
            TaskerInput(
                AutomationTokenInput().apply {
                    token =
                        AutomationPluginSecurity
                            .getOrCreateToken(
                                applicationContext
                            )
                }
            )

    private val taskerHelper by lazy {
        MacroDroidDisconnectHelper(
            this
        )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        if (
            !AutomationPluginSecurity
                .isTrustedConfigCaller(this)
        ) {
            setResult(
                RESULT_CANCELED
            )
            finish()
            return
        }

        taskerHelper.finishForTasker()
    }
}
