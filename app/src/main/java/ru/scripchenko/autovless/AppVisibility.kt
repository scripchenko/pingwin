package ru.scripchenko.autovless

object AppVisibility {

    @Volatile
    private var foreground =
        false

    fun setForeground(
        value: Boolean
    ) {
        foreground = value
    }

    fun isForeground(): Boolean =
        foreground
}
