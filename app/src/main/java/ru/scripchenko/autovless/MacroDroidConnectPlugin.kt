package ru.scripchenko.autovless

import android.app.Activity
import android.content.Context
import android.net.VpnService
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutputOrInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutputOrInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigNoInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess

class MacroDroidConnectRunner :
    TaskerPluginRunnerActionNoOutputOrInput() {

    override fun run(
        context: Context,
        input: TaskerInput<Unit>
    ): TaskerPluginResult<Unit> {

        val state =
            VpnStatus.state.value

        if (
            state == VpnConnectionState.CONNECTED ||
            state == VpnConnectionState.CONNECTING
        ) {
            return TaskerPluginResultSucess()
        }

        if (
            VpnService.prepare(context) != null
        ) {
            throw IllegalStateException(
                "Сначала откройте pingwin и разрешите VPN-подключение"
            )
        }

        val connection =
            ConnectionStore.selected(
                context
            )
                ?: throw IllegalStateException(
                    "В pingwin не выбран сервер"
                )

        val profile =
            VlessProfile.parse(
                connection.link
            )

        val routing =
            RoutingSettingsStore.load(
                context
            )

        val config =
            SingBoxConfigBuilder.build(
                profile,
                routing,
                DiagnosticLogStore
                    .isDetailedEnabled(
                        context
                    )
            )

        DiagnosticLogStore.append(
            context,
            "MacroDroid: включение VPN"
        )

        AutoVlessVpnService.start(
            context,
            config
        )

        return TaskerPluginResultSucess()
    }
}

class MacroDroidConnectHelper(
    config: TaskerPluginConfig<Unit>
) :
    TaskerPluginConfigHelperNoOutputOrInput<
        MacroDroidConnectRunner
    >(config) {

    override val runnerClass =
        MacroDroidConnectRunner::class.java

    override fun addToStringBlurb(
        input: TaskerInput<Unit>,
        blurbBuilder: StringBuilder
    ) {
        blurbBuilder.append(
            "Подключить pingwin к выбранному серверу"
        )
    }
}

class MacroDroidConnectActivity :
    Activity(),
    TaskerPluginConfigNoInput {

    override val context
        get() =
            applicationContext

    private val taskerHelper by lazy {
        MacroDroidConnectHelper(
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
