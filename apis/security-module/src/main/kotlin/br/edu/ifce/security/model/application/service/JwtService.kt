package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.config.properties.JwtProperties
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class JwtService(
    private val jwtEncoder: JwtEncoder,
    private val jwtProperties: JwtProperties
) {
    fun gerarAccessToken(userId: Long, roles: List<String>): String {
        val agora = Instant.now()
        val expiracao = agora.plusSeconds(jwtProperties.accessTokenExpiration)

        val claim = JwtClaimsSet.builder()
            .issuer("catalogo-edificacoes-backend")
            .issuedAt(agora)
            .expiresAt(expiracao)
            .id(UUID.randomUUID().toString())
            .subject(userId.toString())
            .claim("roles", roles)
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(claim)).tokenValue
    }
}