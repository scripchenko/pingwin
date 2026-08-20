package ru.scripchenko.autovless

data class InstalledApp(
    val label: String,
    val packageName: String,
    val isSystem: Boolean
)
