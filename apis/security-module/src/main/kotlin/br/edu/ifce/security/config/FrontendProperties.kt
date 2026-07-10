package br.edu.ifce.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "frontend")
data class FrontendProperties(
    var callbackSuccessUrl: String? = null,
    var callbackErrorUrl: String? = null
)
