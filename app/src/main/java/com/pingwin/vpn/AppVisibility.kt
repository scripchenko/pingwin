package com.pingwin.vpn

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
