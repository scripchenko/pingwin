package com.pingwin.vpn

import android.content.Context
import io.nekohasekai.libbox.CommandClient
import io.nekohasekai.libbox.CommandClientHandler
import io.nekohasekai.libbox.CommandClientOptions
import io.nekohasekai.libbox.ConnectionEvents
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.LogIterator
import io.nekohasekai.libbox.OutboundGroupIterator
import io.nekohasekai.libbox.StatusMessage
import io.nekohasekai.libbox.StringIterator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

object ServerPingMeasurer {

    private const val GROUP_TAG = "proxy"
    private const val OUTBOUND_TAG = "proxy-out"

    private const val INITIAL_GROUP_TIMEOUT_MS =
        1_500L

    private const val RESULT_TIMEOUT_MS =
        8_000L

    @Suppress("UNUSED_PARAMETER")
    suspend fun measure(
        context: Context,
        host: String,
        port: Int,
        attempts: Int = 3
    ): Int? =
        withContext(Dispatchers.IO) {
            val initialGroupSeen =
                CompletableDeferred<Unit>()

            val result =
                CompletableDeferred<Int>()

            val awaitingResult =
                AtomicBoolean(false)

            val handler =
                object : CommandClientHandler {

                    override fun connected() = Unit

                    override fun disconnected(
                        message: String?
                    ) {
                        if (
                            awaitingResult.get() &&
                            !result.isCompleted
                        ) {
                            result.completeExceptionally(
                                IllegalStateException(
                                    message
                                        ?: "Command client disconnected"
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
                    ) = Unit

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
                    ) {
                        if (message == null) {
                            return
                        }

                        while (message.hasNext()) {
                            val group =
                                message.next()

                            if (group.tag != GROUP_TAG) {
                                continue
                            }

                            if (!initialGroupSeen.isCompleted) {
                                initialGroupSeen.complete(Unit)
                            }

                            val items =
                                group.items

                            while (items.hasNext()) {
                                val item =
                                    items.next()

                                if (
                                    item.tag !=
                                    OUTBOUND_TAG
                                ) {
                                    continue
                                }

                                if (!awaitingResult.get()) {
                                    continue
                                }

                                val delay =
                                    item.urlTestDelay

                                if (
                                    delay > 0 &&
                                    !result.isCompleted
                                ) {
                                    result.complete(delay)
                                }
                            }
                        }
                    }

                    override fun writeStatus(
                        message: StatusMessage
                    ) = Unit
                }

            val options =
                CommandClientOptions().apply {
                    addCommand(
                        Libbox.CommandGroup
                    )
                }

            val client =
                CommandClient(
                    handler,
                    options
                )

            try {
                client.connect()

                client.setGroupExpand(
                    GROUP_TAG,
                    true
                )

                withTimeoutOrNull(
                    INITIAL_GROUP_TIMEOUT_MS
                ) {
                    initialGroupSeen.await()
                }

                awaitingResult.set(true)

                client.urlTest(
                    GROUP_TAG
                )

                withTimeoutOrNull(
                    RESULT_TIMEOUT_MS
                ) {
                    result.await()
                }
            } catch (_: Exception) {
                null
            } finally {
                awaitingResult.set(false)

                runCatching {
                    client.disconnect()
                }
            }
        }
}
