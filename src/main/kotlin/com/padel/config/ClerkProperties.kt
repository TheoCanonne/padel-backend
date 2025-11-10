package com.padel.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "external.clerk")
data class ClerkProperties(
    var issuerUrl: String = "",
    var jwksUrl: String = "",
    var webhookSecret: String = ""
)
