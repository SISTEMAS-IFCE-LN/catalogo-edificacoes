package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.config.JwtProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.oauth2.jwt.*

@ExtendWith(MockitoExtension::class)
class JwtServiceTest {

    @Mock
    lateinit var jwtEncoder: JwtEncoder

    @Mock
    lateinit var jwtDecoder: JwtDecoder

    private val jwtProperties =
        JwtProperties(accessTokenExpiration = 900L, refreshExpiration = 43200L, cookieSecure = true)
    private lateinit var jwtService: JwtService

    @BeforeEach
    fun setup() {
        jwtService = JwtService(jwtEncoder, jwtDecoder, jwtProperties)
    }

    private fun jwtStub(
        tokenValue: String = "encoded-token",
        subject: String? = "1",
        email: String? = "u@ifce.edu.br",
        roles: List<String>? = listOf("ROLE_COLABORADOR")
    ): Jwt {
        val builder = Jwt.withTokenValue(tokenValue)
            .header("alg", "RS256")
        if (subject != null) builder.subject(subject)
        if (email != null) builder.claim("email", email)
        if (roles != null) builder.claim("roles", roles)
        return builder.build()
    }

    @Test
    fun `gerarAccessToken codifica claims com subject email e roles`() {
        `when`(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters::class.java)))
            .thenReturn(jwtStub(tokenValue = "encoded"))

        val token = jwtService.gerarAccessToken(7L, "user@ifce.edu.br", listOf("ROLE_X", "ROLE_Y"))

        assertEquals("encoded", token)
        val captor = ArgumentCaptor.forClass(JwtEncoderParameters::class.java)
        org.mockito.Mockito.verify(jwtEncoder).encode(captor.capture())
        val claims: JwtClaimsSet = captor.value.claims
        assertEquals("7", claims.subject)
        assertEquals("user@ifce.edu.br", claims.getClaim("email"))
        assertEquals(listOf("ROLE_X", "ROLE_Y"), claims.getClaim("roles"))
        assertNotNull(claims.issuedAt)
        assertNotNull(claims.expiresAt)
    }

    @Test
    fun `ehValido retorna true quando decoder nao lanca exception`() {
        `when`(jwtDecoder.decode("good")).thenReturn(jwtStub())

        assertTrue(jwtService.ehValido("good"))
    }

    @Test
    fun `ehValido retorna false quando decoder lanca exception`() {
        `when`(jwtDecoder.decode("bad")).thenThrow(BadJwtException("invalid"))

        assertFalse(jwtService.ehValido("bad"))
    }

    @Test
    fun `extrairUserId retorna subject convertido para Long`() {
        `when`(jwtDecoder.decode("t")).thenReturn(jwtStub(subject = "42"))

        assertEquals(42L, jwtService.extrairUserId("t"))
    }

    @Test
    fun `extrairUserId retorna null quando subject nao eh numerico`() {
        `when`(jwtDecoder.decode("t")).thenReturn(jwtStub(subject = "abc"))

        assertNull(jwtService.extrairUserId("t"))
    }

    @Test
    fun `extrairUserId retorna null quando decoder lanca exception`() {
        `when`(jwtDecoder.decode("t")).thenThrow(BadJwtException("err"))

        assertNull(jwtService.extrairUserId("t"))
    }

    @Test
    fun `extrairEmail retorna claim email`() {
        `when`(jwtDecoder.decode("t")).thenReturn(jwtStub(email = "x@ifce.edu.br"))

        assertEquals("x@ifce.edu.br", jwtService.extrairEmail("t"))
    }

    @Test
    fun `extrairEmail retorna null quando decoder lanca exception`() {
        `when`(jwtDecoder.decode("t")).thenThrow(BadJwtException("err"))

        assertNull(jwtService.extrairEmail("t"))
    }

    @Test
    fun `extrairRoles retorna lista de roles`() {
        `when`(jwtDecoder.decode("t")).thenReturn(jwtStub(roles = listOf("ROLE_A", "ROLE_B")))

        val result = jwtService.extrairRoles("t")

        assertNotNull(result)
        assertEquals(listOf("ROLE_A", "ROLE_B"), result)
    }

    @Test
    fun `extrairRoles retorna null quando decoder lanca exception`() {
        `when`(jwtDecoder.decode("t")).thenThrow(BadJwtException("err"))

        assertNull(jwtService.extrairRoles("t"))
    }
}
