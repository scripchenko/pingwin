package com.pingwin.vpn

object DiagnosticLogRedactor {

    private val uriUserInfoRegex =
        Regex(
            """(?i)([a-z][a-z0-9+.-]*://)([^@\s/]+)@"""
        )

    private val sensitiveParameterRegex =
        Regex(
            """(?i)\b(password|passwd|token|access_token|auth|authorization|secret|private_key|uuid)\s*=\s*([^&\s,;]+)"""
        )

    private val authorizationHeaderRegex =
        Regex(
            """(?i)\b(authorization|proxy-authorization)\s*:\s*(?:bearer\s+|basic\s+)?[^\s,;]+"""
        )

    private val uuidRegex =
        Regex(
            """(?i)\b[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\b"""
        )

    private val ipv4Regex =
        Regex(
            """(?<!\d)(?:\d{1,3}\.){3}\d{1,3}(?!\d)"""
        )

    private val ipv6CandidateRegex =
        Regex(
            """(?i)(?<![0-9a-f:])(?:[0-9a-f]{0,4}:){2,7}[0-9a-f]{0,4}(?![0-9a-f:])"""
        )

    private val domainRegex =
        Regex(
            """(?i)(?<![@\w.-])(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,63}(?![\w.-])"""
        )

    fun redact(message: String): String {
        var result =
            uriUserInfoRegex.replace(
                message
            ) { match ->
                "${match.groupValues[1]}***@"
            }

        result =
            authorizationHeaderRegex.replace(
                result
            ) { match ->
                "${match.groupValues[1]}: ***"
            }

        result =
            sensitiveParameterRegex.replace(
                result
            ) { match ->
                "${match.groupValues[1]}=***"
            }

        result =
            uuidRegex.replace(
                result,
                "***"
            )

        result =
            ipv4Regex.replace(
                result
            ) { match ->
                val value = match.value

                if (
                    value.split('.').all { part ->
                        part.toIntOrNull()
                            ?.let { it in 0..255 } == true
                    }
                ) {
                    HostPrivacyMasker.mask(
                        value
                    )
                } else {
                    value
                }
            }

        result =
            ipv6CandidateRegex.replace(
                result
            ) { match ->
                val value = match.value
                val colonCount =
                    value.count {
                        it == ':'
                    }

                if (
                    value.contains("::") ||
                    colonCount >= 4
                ) {
                    HostPrivacyMasker.mask(
                        value
                    )
                } else {
                    value
                }
            }

        result =
            domainRegex.replace(
                result
            ) { match ->
                HostPrivacyMasker.mask(
                    match.value
                )
            }

        return result
    }
}
