package ru.scripchenko.autovless

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutputOrInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutputOrInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigNoInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess

class MacroDroidDisconnectRunner :
    TaskerPluginRunnerActionNoOutputOrInput() {

    override fun run(
        context: Context,
        input: TaskerInput<Unit>
    ): TaskerPluginResult<Unit> {

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
    private val pluginConfig: TaskerPluginConfig<Unit>
) :
    TaskerPluginConfigHelperNoOutputOrInput<
        MacroDroidDisconnectRunner
    >(pluginConfig) {

    override val runnerClass =
        MacroDroidDisconnectRunner::class.java

    override fun addToStringBlurb(
        input: TaskerInput<Unit>,
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
    TaskerPluginConfigNoInput {

    override val context
        get() =
            applicationContext

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

        taskerHelper.finishForTasker()
    }
}
