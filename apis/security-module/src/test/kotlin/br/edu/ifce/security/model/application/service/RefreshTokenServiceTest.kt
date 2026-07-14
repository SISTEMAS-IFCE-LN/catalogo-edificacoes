package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.config.properties.JwtProperties
import br.edu.ifce.security.model.domain.RefreshToken
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.RefreshTokenRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class RefreshTokenServiceTest {

    @Mock
    lateinit var refreshTokenRepository: RefreshTokenRepository

    private val jwtProperties =
        JwtProperties(accessTokenExpiration = 900L, refreshExpiration = 3600L, cookieSecure = true)
    private lateinit var refreshTokenService: RefreshTokenService

    @BeforeEach
    fun setup() {
        refreshTokenService = RefreshTokenService(refreshTokenRepository, jwtProperties)
    }

    private fun criarUsuario() = Usuario(id = 1, email = "user@ifce.edu.br", nome = "User")

    @Test
    fun `gerarRefreshToken revoga token antigo e cria novo quando ja existe um ativo`() {
        val usuario = criarUsuario()
        val tokenAntigo = RefreshToken(
            token = "antigo",
            usuario = usuario,
            expiraEm = LocalDateTime.now().plusHours(1),
            revogado = false
        )
        `when`(refreshTokenRepository.findByUsuarioAndRevogadoFalse(usuario)).thenReturn(tokenAntigo)
        `when`(refreshTokenRepository.save(any(RefreshToken::class.java))).thenAnswer { it.arguments[0] }

        val novo = refreshTokenService.gerarRefreshToken(usuario)

        assertTrue(tokenAntigo.revogado)
        verify(refreshTokenRepository).save(tokenAntigo)
        assertNotNull(novo)
        assertNotNull(novo.token)
        assertEquals(usuario, novo.usuario)
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken::class.java))
    }

    @Test
    fun `gerarRefreshToken cria novo sem revogar quando nao ha token antigo`() {
        val usuario = criarUsuario()
        `when`(refreshTokenRepository.findByUsuarioAndRevogadoFalse(usuario)).thenReturn(null)
        `when`(refreshTokenRepository.save(any(RefreshToken::class.java))).thenAnswer { it.arguments[0] }

        val novo = refreshTokenService.gerarRefreshToken(usuario)

        assertNotNull(novo)
        assertNotNull(novo.token)
        try {
            UUID.fromString(novo.token)
        } catch (e: Exception) {
            assert(false) { "token deveria ser UUID: ${novo.token}" }
        }
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken::class.java))
    }

    @Test
    fun `validarRefreshToken retorna token quando nao revogado e nao expirado`() {
        val token = RefreshToken(
            token = "t",
            usuario = criarUsuario(),
            expiraEm = LocalDateTime.now().plusHours(1),
            revogado = false
        )
        `when`(refreshTokenRepository.findByToken("t")).thenReturn(token)

        val result = refreshTokenService.validarRefreshToken("t")

        assertNotNull(result)
        assertEquals("t", result!!.token)
    }

    @Test
    fun `validarRefreshToken retorna null quando token nao existe`() {
        `when`(refreshTokenRepository.findByToken("nope")).thenReturn(null)

        val result = refreshTokenService.validarRefreshToken("nope")

        assertNull(result)
    }

    @Test
    fun `validarRefreshToken retorna null quando token esta revogado`() {
        val token = RefreshToken(
            token = "t",
            usuario = criarUsuario(),
            expiraEm = LocalDateTime.now().plusHours(1),
            revogado = true
        )
        `when`(refreshTokenRepository.findByToken("t")).thenReturn(token)

        val result = refreshTokenService.validarRefreshToken("t")

        assertNull(result)
    }

    @Test
    fun `validarRefreshToken retorna null quando token esta expirado`() {
        val token = RefreshToken(
            token = "t",
            usuario = criarUsuario(),
            expiraEm = LocalDateTime.now().minusHours(1),
            revogado = false
        )
        `when`(refreshTokenRepository.findByToken("t")).thenReturn(token)

        val result = refreshTokenService.validarRefreshToken("t")

        assertNull(result)
    }

    @Test
    fun `revogarRefreshToken marca como revogado quando token existe`() {
        val token = RefreshToken(
            token = "t",
            usuario = criarUsuario(),
            expiraEm = LocalDateTime.now().plusHours(1),
            revogado = false
        )
        `when`(refreshTokenRepository.findByToken("t")).thenReturn(token)

        refreshTokenService.revogarRefreshToken("t")

        assertTrue(token.revogado)
        verify(refreshTokenRepository).save(token)
    }

    @Test
    fun `revogarRefreshToken nao faz nada quando token nao existe`() {
        `when`(refreshTokenRepository.findByToken("nope")).thenReturn(null)

        refreshTokenService.revogarRefreshToken("nope")

        verify(refreshTokenRepository, never()).save(any(RefreshToken::class.java))
    }

    @Test
    fun `limparTokensInuteis deleta tokens expirados e retorna quantidade`() {
        doReturn(5).`when`(refreshTokenRepository).deleteByExpiraEmBeforeOrRevogadoTrue(any<LocalDateTime>() ?: LocalDateTime.now())

        val resultado = refreshTokenService.limparTokensInuteis()

        assertEquals(5, resultado)
        verify(refreshTokenRepository).deleteByExpiraEmBeforeOrRevogadoTrue(any<LocalDateTime>() ?: LocalDateTime.now())
    }
}
