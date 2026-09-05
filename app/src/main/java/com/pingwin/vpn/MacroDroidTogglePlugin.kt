package com.pingwin.vpn

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
                    context.getString(
                        R.string.macrodroid_log_toggle_disconnect
                    )
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
                        error: VlessParseException
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
                        error: SingBoxConfigException
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
                        R.string.macrodroid_log_toggle_connect
                    )
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
    private val pluginConfig: TaskerPluginConfig<Unit>
) :
    TaskerPluginConfigHelperNoOutputOrInput<
        MacroDroidToggleRunner
    >(pluginConfig) {

    override val runnerClass =
        MacroDroidToggleRunner::class.java

    override fun addToStringBlurb(
        input: TaskerInput<Unit>,
        blurbBuilder: StringBuilder
    ) {
        blurbBuilder.append(
            pluginConfig.context.getString(
                R.string.macrodroid_blurb_toggle
            )
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
