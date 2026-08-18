package ru.scripchenko.autovless

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private val link =
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

    @Test
    fun libboxAcceptsWorkingVlessConfig() {
        val profile = VlessProfile.parse(link)
        val config = SingBoxConfigBuilder.build(profile)

        assertLibboxAccepts(config)
    }

    @Test
    fun libboxAcceptsOnlySelectedSitesRouting() {
        val profile = VlessProfile.parse(link)
        val routing =
            RoutingSettings(
                siteMode = SiteRoutingMode.ONLY_SELECTED_VIA_VPN,
                domains = setOf("youtube.com", "*.googlevideo.com")
            )

        val config = SingBoxConfigBuilder.build(profile, routing)

        assertLibboxAccepts(config)
    }

    @Test
    fun libboxAcceptsExcludedSitesRouting() {
        val profile = VlessProfile.parse(link)
        val routing =
            RoutingSettings(
                siteMode = SiteRoutingMode.EXCLUDE_SELECTED_FROM_VPN,
                domains = setOf("example.com", ".example.org")
            )

        val config = SingBoxConfigBuilder.build(profile, routing)

        assertLibboxAccepts(config)
    }

    private fun assertLibboxAccepts(config: String) {
        val result = LibboxValidator.validate(config)

        assertTrue(
            result.exceptionOrNull()?.message ?: "libbox rejected config",
            result.isSuccess
        )
    }
}
