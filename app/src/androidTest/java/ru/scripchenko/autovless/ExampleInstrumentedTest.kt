package ru.scripchenko.autovless

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun libboxAcceptsWorkingVlessConfig() {
        val link =
            "vless://79ffee41-ab87-44c2-8f53-fd0afe05fa11@95.164.93.212:443" +
                    "?encryption=none" +
                    "&flow=xtls-rprx-vision" +
                    "&fp=chrome" +
                    "&pbk=lSPik24zP7ion8DrfLkvoTrVnT7J2VOEbouTq3uTcho" +
                    "&security=reality" +
                    "&sid=50" +
                    "&sni=amazon.com" +
                    "&spx=%2F4b01c62261fac66" +
                    "&type=tcp" +
                    "#VLESS-Reality-dmitry_scripchenko"

        val profile = VlessProfile.parse(link)
        val config = SingBoxConfigBuilder.build(profile)

        val result = LibboxValidator.validate(config)

        assertTrue(
            result.exceptionOrNull()?.message ?: "libbox rejected config",
            result.isSuccess
        )
    }
}