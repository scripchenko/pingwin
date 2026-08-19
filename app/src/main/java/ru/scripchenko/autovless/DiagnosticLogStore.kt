package ru.scripchenko.autovless

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DiagnosticLogStore {

    private const val PREFS_NAME =
        "pingwin_diagnostic_log"

    private const val KEY_ENTRIES =
        "entries"

    private const val KEY_DETAILED =
        "detailed_enabled"

    private const val MAX_ENTRIES =
        200

    fun isDetailedEnabled(
        context: Context
    ): Boolean =
        prefs(context).getBoolean(
            KEY_DETAILED,
            false
        )

    fun setDetailedEnabled(
        context: Context,
        enabled: Boolean
    ) {
        prefs(context)
            .edit()
            .putBoolean(
                KEY_DETAILED,
                enabled
            )
            .apply()
    }

    fun append(
        context: Context,
        message: String
    ) {
        appendBatch(
            context,
            listOf(message)
        )
    }

    fun appendDetailed(
        context: Context,
        message: String
    ) {
        if (
            !isDetailedEnabled(context)
        ) {
            return
        }

        append(
            context,
            message
        )
    }

    fun appendDetailedBatch(
        context: Context,
        messages: List<String>
    ) {
        if (
            !isDetailedEnabled(context) ||
            messages.isEmpty()
        ) {
            return
        }

        appendBatch(
            context,
            messages
        )
    }

    fun entries(
        context: Context
    ): List<String> {
        val raw =
            prefs(context)
                .getString(
                    KEY_ENTRIES,
                    ""
                )
                .orEmpty()

        if (raw.isBlank()) {
            return emptyList()
        }

        return raw.lines()
            .filter {
                it.isNotBlank()
            }
    }

    fun clear(
        context: Context
    ) {
        prefs(context)
            .edit()
            .remove(
                KEY_ENTRIES
            )
            .apply()
    }

    private fun appendBatch(
        context: Context,
        messages: List<String>
    ) {
        if (messages.isEmpty()) {
            return
        }

        val current =
            entries(context)
                .toMutableList()

        val time =
            timestamp()

        messages.forEach { message ->
            if (message.isNotBlank()) {
                current +=
                    "$time  $message"
            }
        }

        val trimmed =
            current.takeLast(
                MAX_ENTRIES
            )

        prefs(context)
            .edit()
            .putString(
                KEY_ENTRIES,
                trimmed.joinToString("\n")
            )
            .apply()
    }

    private fun prefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private fun timestamp(): String =
        SimpleDateFormat(
            "dd.MM HH:mm:ss",
            Locale.getDefault()
        ).format(
            Date()
        )
}
