package com.pingwin.vpn

import android.app.Activity
import android.content.Context
import android.net.VpnService
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess

class MacroDroidConnectRunner :
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
            state == VpnConnectionState.CONNECTED ||
            state == VpnConnectionState.CONNECTING
        ) {
            return TaskerPluginResultSucess()
        }

        if (
            VpnService.prepare(context) != null
        ) {
            throw IllegalStateException(
                context.getString(
                    R.string.macrodroid_vpn_permission_required
                )
            )
        }

        val connection =
            ConnectionStore.selected(
                context
            )
                ?: throw IllegalStateException(
                    context.getString(
                        R.string.macrodroid_no_server_selected
                    )
                )

        val profile =
            try {
                ConnectionProfileParser.parse(
                    connection.link
                )
            } catch (
                error: IllegalArgumentException
            ) {
                throw IllegalStateException(
                    error.localizedVpnMessage(
                        context
                    ),
                    error
                )
            }

        val routing =
            RoutingSettingsStore.load(
                context
            )

        val config =
            try {
                ConnectionConfigBuilder.build(
                    profile,
                    routing,
                    DiagnosticLogStore
                        .isDetailedEnabled(
                            context
                        )
                )
            } catch (
                error: IllegalArgumentException
            ) {
                throw IllegalStateException(
                    error.localizedVpnMessage(
                        context
                    ),
                    error
                )
            }

        DiagnosticLogStore.append(
            context,
            context.getString(
                R.string.macrodroid_log_connect
            )
        )

        AutoVlessVpnService.start(
            context,
            config
        )

        return TaskerPluginResultSucess()
    }
}

class MacroDroidConnectHelper(
    private val pluginConfig:
        TaskerPluginConfig<AutomationTokenInput>
) :
    TaskerPluginConfigHelperNoOutput<
        AutomationTokenInput,
        MacroDroidConnectRunner
    >(pluginConfig) {

    override val inputClass =
        AutomationTokenInput::class.java

    override val runnerClass =
        MacroDroidConnectRunner::class.java

    override fun addToStringBlurb(
        input: TaskerInput<AutomationTokenInput>,
        blurbBuilder: StringBuilder
    ) {
        blurbBuilder.append(
            pluginConfig.context.getString(
                R.string.macrodroid_blurb_connect
            )
        )
    }
}

class MacroDroidConnectActivity :
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
