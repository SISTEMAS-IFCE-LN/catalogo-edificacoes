package br.edu.ifce.security.unitarios

import br.edu.ifce.security.config.properties.JwtProperties
import br.edu.ifce.security.model.application.service.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters

@ExtendWith(MockitoExtension::class)
class JwtServiceTest {

    @Mock
    lateinit var jwtEncoder: JwtEncoder

    private val jwtProperties =
        JwtProperties(accessTokenExpiration = 900L, refreshExpiration = 43200L, cookieSecure = true)
    private lateinit var jwtService: JwtService

    @BeforeEach
    fun setup() {
        jwtService = JwtService(jwtEncoder, jwtProperties)
    }

    private fun jwtStub(
        tokenValue: String = "encoded-token",
        subject: String? = "1",
        roles: List<String>? = listOf("ROLE_COLABORADOR")
    ): Jwt {
        val builder = Jwt.withTokenValue(tokenValue)
            .header("alg", "RS256")
        if (subject != null) builder.subject(subject)
        if (roles != null) builder.claim("roles", roles)
        return builder.build()
    }

    @Test
    fun `gerarAccessToken codifica claims com subject e roles`() {
        `when`(jwtEncoder.encode(ArgumentMatchers.any(JwtEncoderParameters::class.java)))
            .thenReturn(jwtStub(tokenValue = "encoded"))

        val token = jwtService.gerarAccessToken(7L, listOf("ROLE_X", "ROLE_Y"))

        assertEquals("encoded", token)
        val captor = ArgumentCaptor.forClass(JwtEncoderParameters::class.java)
        Mockito.verify(jwtEncoder).encode(captor.capture())
        val claims: JwtClaimsSet = captor.value.claims
        assertEquals("7", claims.subject)
        assertEquals(listOf("ROLE_X", "ROLE_Y"), claims.getClaim("roles"))
        assertNotNull(claims.issuedAt)
        assertNotNull(claims.expiresAt)
        assertEquals("catalogo-edificacoes-backend", claims.getClaim("iss"))
        assertNotNull(claims.id)
    }
}
