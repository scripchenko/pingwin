package com.pingwin.vpn

import io.nekohasekai.libbox.Libbox

object LibboxValidator {

    fun validate(config: String): Result<Unit> =
        runCatching {
            Libbox.checkConfig(config)
        }
}