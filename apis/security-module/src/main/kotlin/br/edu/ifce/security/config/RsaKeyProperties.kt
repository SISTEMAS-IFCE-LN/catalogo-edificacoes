package br.edu.ifce.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@ConfigurationProperties(prefix = "rsa")
data class RsaKeyProperties(
    var publicKey: RSAPublicKey? = null,
    var privateKey: RSAPrivateKey? = null,
)
