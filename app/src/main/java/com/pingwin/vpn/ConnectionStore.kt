package com.pingwin.vpn

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object ConnectionStore {

    private const val PREFS_NAME = "connections"
    private const val KEY_CONNECTIONS = "connections_json"
    private const val KEY_SELECTED_ID = "selected_connection_id"

    fun loadAll(
        context: Context
    ): List<SavedConnection> {
        val prefs =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val raw =
            prefs.getString(
                KEY_CONNECTIONS,
                null
            ) ?: return emptyList()

        return runCatching {
            val array = JSONArray(raw)

            buildList {
                for (
                    index in 0 until array.length()
                ) {
                    val item =
                        array.getJSONObject(index)

                    add(
                        SavedConnection(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            link = item.getString("link")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun selected(
        context: Context
    ): SavedConnection? {
        val prefs =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val selectedId =
            prefs.getString(
                KEY_SELECTED_ID,
                null
            )

        val connections =
            loadAll(context)

        return connections.firstOrNull {
            it.id == selectedId
        } ?: connections.firstOrNull()
    }

    fun add(
        context: Context,
        link: String,
        name: String? = null
    ): SavedConnection {
        val profile =
            try {
                ConnectionProfileParser.parse(link)
            } catch (error: IllegalArgumentException) {
                throw IllegalArgumentException(
                    error.localizedVpnMessage(context),
                    error
                )
            }

        val connection =
            SavedConnection(
                id = UUID.randomUUID().toString(),
                name =
                    name
                        ?.trim()
                        ?.takeIf(String::isNotEmpty)
                        ?: defaultName(profile),
                link = link.trim()
            )

        val updated =
            loadAll(context) + connection

        saveAll(
            context,
            updated
        )

        select(
            context,
            connection.id
        )

        return connection
    }

    fun select(
        context: Context,
        id: String
    ) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                KEY_SELECTED_ID,
                id
            )
            .apply()
    }

    fun remove(
        context: Context,
        id: String
    ) {
        val updated =
            loadAll(context)
                .filterNot {
                    it.id == id
                }

        saveAll(
            context,
            updated
        )

        val prefs =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        if (
            prefs.getString(
                KEY_SELECTED_ID,
                null
            ) == id
        ) {
            prefs.edit()
                .remove(KEY_SELECTED_ID)
                .apply()

            updated.firstOrNull()?.let {
                select(
                    context,
                    it.id
                )
            }
        }
    }

    private fun saveAll(
        context: Context,
        connections: List<SavedConnection>
    ) {
        val array =
            JSONArray().apply {
                connections.forEach { connection ->
                    put(
                        JSONObject().apply {
                            put(
                                "id",
                                connection.id
                            )
                            put(
                                "name",
                                connection.name
                            )
                            put(
                                "link",
                                connection.link
                            )
                        }
                    )
                }
            }

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                KEY_CONNECTIONS,
                array.toString()
            )
            .apply()
    }

    private fun defaultName(
        profile: ConnectionProfile
    ): String =
        buildString {
            append(profile.protocol.displayName)

            if (profile.host.isNotBlank()) {
                append(" · ")
                append(profile.host)
            }
        }
}
