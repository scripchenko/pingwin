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

class MacroDroidToggleRunner :
    TaskerPluginRunnerActionNoOutputOrInput() {

    override fun run(
        context: Context,
        input: TaskerInput<Unit>
    ): TaskerPluginResult<Unit> {

        when (VpnStatus.state.value) {
            VpnConnectionState.CONNECTED,
            VpnConnectionState.CONNECTING -> {
                DiagnosticLogStore.append(
                    context,
                    "MacroDroid: переключение VPN — отключение"
                )

                AutoVlessVpnService.stop(
                    context
                )
            }

            VpnConnectionState.DISCONNECTED,
            VpnConnectionState.ERROR -> {
                if (
                    VpnService.prepare(context) !=
                    null
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
                    "MacroDroid: переключение VPN — включение"
                )

                AutoVlessVpnService.start(
                    context,
                    config
                )
            }
        }

        return TaskerPluginResultSucess()
    }
}

class MacroDroidToggleHelper(
    config: TaskerPluginConfig<Unit>
) :
    TaskerPluginConfigHelperNoOutputOrInput<
        MacroDroidToggleRunner
    >(config) {

    override val runnerClass =
        MacroDroidToggleRunner::class.java

    override fun addToStringBlurb(
        input: TaskerInput<Unit>,
        blurbBuilder: StringBuilder
    ) {
        blurbBuilder.append(
            "Переключить состояние VPN pingwin"
        )
    }
}

class MacroDroidToggleActivity :
    Activity(),
    TaskerPluginConfigNoInput {

    override val context
        get() =
            applicationContext

    private val taskerHelper by lazy {
        MacroDroidToggleHelper(
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
