package br.edu.ifce.security.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.server.Cookie

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var accessTokenExpiration: Long = 900L,
    var refreshExpiration: Long = 3600L,
    var cookieSecure: Boolean = true,
    var sameSite: String = "Lax"
)
