package com.pingwin.vpn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private val link =
        "vless://11111111-1111-4111-8111-111111111111@203.0.113.10:443" +
                "?encryption=none" +
                "&flow=xtls-rprx-vision" +
                "&fp=chrome" +
                "&pbk=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "&security=reality" +
                "&sid=0123456789abcdef" +
                "&sni=example.com" +
                "&spx=%2Ftest-spider" +
                "&type=tcp" +
                "#Test-Reality"

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
                siteMode = RoutingMode.ONLY_SELECTED_VIA_VPN,
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
                siteMode = RoutingMode.EXCLUDE_SELECTED_FROM_VPN,
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
