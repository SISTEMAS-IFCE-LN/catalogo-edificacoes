package br.edu.ifce.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.ResourceUtils
import java.nio.file.Files
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@ConfigurationProperties(prefix = "rsa")
data class RsaKeyProperties(
    var publicKeyPath: String? = null,
    var privateKeyPath: String? = null,
) {

    val publicKey: RSAPublicKey? by lazy { loadPublicKey() }
    val privateKey: RSAPrivateKey? by lazy { loadPrivateKey() }

    private fun loadPublicKey(): RSAPublicKey? {
        val path = publicKeyPath ?: return null
        val pem = readPem(path)
        val der = decodePemBody(pem)
        val spec = X509EncodedKeySpec(der)
        return KeyFactory.getInstance("RSA").generatePublic(spec) as RSAPublicKey
    }

    private fun loadPrivateKey(): RSAPrivateKey? {
        val path = privateKeyPath ?: return null
        val pem = readPem(path)
        val der = decodePemBody(pem)
        val spec = PKCS8EncodedKeySpec(der)
        return KeyFactory.getInstance("RSA").generatePrivate(spec) as RSAPrivateKey
    }

    private fun readPem(path: String): String {
        val resource = ResourceUtils.getFile(path)
        return Files.readString(resource.toPath())
    }

    private fun decodePemBody(pem: String): ByteArray {
        val body = pem
            .lineSequence()
            .filter { it.isNotBlank() && !it.startsWith("-----BEGIN") && !it.startsWith("-----END") }
            .joinToString("")
        return Base64.getDecoder().decode(body)
    }
}
