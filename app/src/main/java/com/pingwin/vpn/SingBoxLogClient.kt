package com.pingwin.vpn

import android.content.Context
import android.util.Log
import io.nekohasekai.libbox.CommandClient
import io.nekohasekai.libbox.CommandClientHandler
import io.nekohasekai.libbox.CommandClientOptions
import io.nekohasekai.libbox.ConnectionEvents
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.LogIterator
import io.nekohasekai.libbox.OutboundGroupIterator
import io.nekohasekai.libbox.StatusMessage
import io.nekohasekai.libbox.StringIterator
import java.util.concurrent.Executors

class SingBoxLogClient(
    context: Context
) : CommandClientHandler {

    companion object {
        private const val TAG =
            "SingBoxLogClient"
    }

    private val appContext =
        context.applicationContext

    private val executor =
        Executors.newSingleThreadExecutor()

    @Volatile
    private var commandClient:
        CommandClient? = null

    @Volatile
    private var stopped = false

    fun start() {
        if (
            !DiagnosticLogStore
                .isDetailedEnabled(
                    appContext
                )
        ) {
            return
        }

        executor.execute {
            synchronized(this) {
                if (
                    commandClient != null ||
                    stopped
                ) {
                    return@execute
                }
            }

            try {
                val options =
                    CommandClientOptions().apply {
                        addCommand(
                            Libbox.CommandLog
                        )
                    }

                val client =
                    CommandClient(
                        this,
                        options
                    )

                synchronized(this) {
                    if (stopped) {
                        runCatching {
                            client.disconnect()
                        }
                        return@execute
                    }

                    commandClient =
                        client
                }

                client.connect()

                DiagnosticLogStore.append(
                    appContext,
                    appContext.getString(
                        R.string.singbox_log_connected
                    )
                )
            } catch (e: Exception) {
                synchronized(this) {
                    commandClient = null
                }

                Log.e(
                    TAG,
                    "Failed to connect log client",
                    e
                )

                DiagnosticLogStore.append(
                    appContext,
                    appContext.getString(
                        R.string.singbox_log_connect_error,
                        e.localizedVpnMessage(
                            appContext
                        )
                    )
                )
            }
        }
    }

    fun stop() {
        val client =
            synchronized(this) {
                stopped = true

                val current =
                    commandClient

                commandClient = null
                current
            }

        executor.execute {
            runCatching {
                client?.disconnect()
            }

            executor.shutdown()
        }
    }

    override fun connected() = Unit

    override fun disconnected(
        message: String?
    ) {
        synchronized(this) {
            commandClient = null
        }

        if (
            !stopped &&
            !message.isNullOrBlank()
        ) {
            DiagnosticLogStore.append(
                appContext,
                appContext.getString(
                    R.string.singbox_log_disconnected,
                    message
                )
            )
        }
    }

    override fun clearLogs() = Unit

    override fun setDefaultLogLevel(
        level: Int
    ) = Unit

    override fun writeLogs(
        message: LogIterator?
    ) {
        if (
            message == null ||
            !DiagnosticLogStore
                .isDetailedEnabled(
                    appContext
                )
        ) {
            return
        }

        val lines =
            mutableListOf<String>()

        while (message.hasNext()) {
            val entry =
                message.next()

            val line =
                entry.message
                    ?.trimEnd()
                    .orEmpty()

            if (line.isNotBlank()) {
                lines += line
            }
        }

        DiagnosticLogStore
            .appendDetailedBatch(
                appContext,
                lines
            )
    }

    override fun initializeClashMode(
        modeList: StringIterator,
        currentMode: String
    ) = Unit

    override fun updateClashMode(
        newMode: String
    ) = Unit

    override fun writeConnectionEvents(
        events: ConnectionEvents?
    ) = Unit

    override fun writeGroups(
        message: OutboundGroupIterator?
    ) = Unit

    override fun writeStatus(
        message: StatusMessage
    ) = Unit
}
