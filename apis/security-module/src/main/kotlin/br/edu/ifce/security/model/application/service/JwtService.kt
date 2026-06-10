package br.edu.ifce.security.model.application.service

import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JwtService(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder
) {
    fun gerarAccessToken(userId: Long, email: String, roles: List<String>): String {
        val agora = Instant.now()
        val expiracao = agora.plusSeconds(900L) // 15 minutos

        val claim = JwtClaimsSet.builder()
            .issuedAt(agora)
            .expiresAt(expiracao)
            .subject(userId.toString())
            .claim("email", email)
            .claim("roles", roles)
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(claim)).tokenValue
    }

    fun ehValido(token: String): Boolean {
        return try {
            jwtDecoder.decode(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun extrairUserId(token: String): Long? {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.subject.toLong()
        } catch (e: Exception) {
            null
        }
    }

    fun extrairEmail(token: String): String? {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.getClaimAsString("email")
        } catch (e: Exception) {
            null
        }
    }

    fun extrairRoles(token: String): List<String>? {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.getClaimAsStringList("roles")
        } catch (e: Exception) {
            null
        }
    }
}