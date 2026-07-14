package br.edu.ifce.security.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bootstrap")
data class BootstrapProperties(
    val adminEmail: String = "",
    val allowReactivate: Boolean = true
)