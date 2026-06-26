package br.edu.ifce.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var accessTokenExpiration: Long = 900L,
    var refreshExpiration: Long = 43200L,
    var cookieSecure: Boolean = true
)
