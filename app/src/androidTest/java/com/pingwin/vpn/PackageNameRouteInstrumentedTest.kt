package com.pingwin.vpn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PackageNameRouteInstrumentedTest {

    @Test
    fun libboxAcceptsPackageNameRouteRule() {
        val link =
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

        val profile =
            VlessProfile.parse(link)

        val baseConfig =
            SingBoxConfigBuilder.build(profile)

        val packageRule =
            """
            "rules": [
              {
                "package_name": [
                  "org.telegram.messenger"
                ],
                "action": "route",
                "outbound": "proxy"
              }
            ],
            """.trimIndent()

        val config =
            baseConfig.replace(
                "\"auto_detect_interface\": true,",
                "\"auto_detect_interface\": true,\n$packageRule",
                ignoreCase = false
            )

        val result =
            LibboxValidator.validate(config)

        assertTrue(
            result.exceptionOrNull()?.message
                ?: "libbox rejected package_name route rule",
            result.isSuccess
        )
    }
}
