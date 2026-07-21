package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.config.properties.JwtProperties
import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.RefreshToken
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    lateinit var jwtService: JwtService

    @Mock
    lateinit var jwtProperties: JwtProperties

    @Mock
    lateinit var refreshTokenService: RefreshTokenService

    @Mock
    lateinit var usuarioRepository: UsuarioRepository

    @InjectMocks
    lateinit var authService: AuthService

    @Test
    fun `loginSuccess retorna TokensPair quando email existe`() {
        val usuario = Usuario(id = 1, email = "user@ifce.edu.br", nome = "User").apply {
            perfis = mutableSetOf(Perfil.ROLE_COLABORADOR)
        }
        val novoRefresh =
            RefreshToken(token = "refresh-xyz", usuario = usuario, expiraEm = java.time.LocalDateTime.now().plusDays(1))
        `when`(usuarioRepository.findByEmail("user@ifce.edu.br")).thenReturn(usuario)
        `when`(jwtService.gerarAccessToken(1L, listOf("ROLE_COLABORADOR"))).thenReturn("access-xyz")
        `when`(refreshTokenService.gerarRefreshToken(usuario)).thenReturn(novoRefresh)

        val result = authService.loginSuccess("user@ifce.edu.br")

        assertNotNull(result)
        assertEquals("access-xyz", result!!.accessToken)
        assertEquals("refresh-xyz", result.refreshToken)
    }

    @Test
    fun `loginSuccess retorna null quando email nao existe`() {
        `when`(usuarioRepository.findByEmail("inexistente@ifce.edu.br")).thenReturn(null)

        val result = authService.loginSuccess("inexistente@ifce.edu.br")

        assertNull(result)
        verifyNoInteractions(jwtService)
        verifyNoInteractions(refreshTokenService)
    }

    @Test
    fun `refresh retorna TokensPair quando cookie eh valido`() {
        val usuario = Usuario(id = 5, email = "user@ifce.edu.br", nome = "User").apply {
            perfis = mutableSetOf(Perfil.ROLE_COLABORADOR)
        }
        val refreshToken =
            RefreshToken(token = "old-refresh", usuario = usuario, expiraEm = java.time.LocalDateTime.now().plusDays(1))
        `when`(jwtProperties.accessTokenExpiration).thenReturn(900L)
        `when`(refreshTokenService.validarRefreshToken("cookie-abc")).thenReturn(refreshToken)
        `when`(jwtService.gerarAccessToken(5L, listOf("ROLE_COLABORADOR"))).thenReturn("access-new")

        val result = authService.refresh("cookie-abc")

        assertNotNull(result)
        assertEquals("access-new", result!!.accessToken)
        assertNull(result.refreshToken)
        verify(refreshTokenService, never()).gerarRefreshToken(usuario)
    }

    @Test
    fun `refresh retorna null quando cookie eh invalido`() {
        `when`(refreshTokenService.validarRefreshToken("invalido")).thenReturn(null)

        val result = authService.refresh("invalido")

        assertNull(result)
        verify(jwtService, never()).gerarAccessToken(
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.anyList()
        )
    }

    @Test
    fun `refresh renova refresh token quando tempo restante eh menor que access token expiration`() {
        val usuario = Usuario(id = 7, email = "user@ifce.edu.br", nome = "User").apply {
            perfis = mutableSetOf(Perfil.ROLE_COLABORADOR)
        }
        val refreshToken = RefreshToken(
            token = "old-refresh",
            usuario = usuario,
            expiraEm = LocalDateTime.now().plusSeconds(600)
        )
        val novoRefresh = RefreshToken(
            token = "new-refresh",
            usuario = usuario,
            expiraEm = LocalDateTime.now().plusSeconds(3600)
        )
        `when`(jwtProperties.accessTokenExpiration).thenReturn(900L)
        `when`(refreshTokenService.validarRefreshToken("cookie-abc")).thenReturn(refreshToken)
        `when`(jwtService.gerarAccessToken(7L, listOf("ROLE_COLABORADOR"))).thenReturn("access-new")
        `when`(refreshTokenService.gerarRefreshToken(usuario)).thenReturn(novoRefresh)

        val result = authService.refresh("cookie-abc")

        assertNotNull(result)
        assertEquals("access-new", result!!.accessToken)
        assertEquals("new-refresh", result.refreshToken)
        verify(refreshTokenService).gerarRefreshToken(usuario)
    }

    @Test
    fun `logout revoga token quando cookie nao eh null`() {
        authService.logout("cookie-abc")

        verify(refreshTokenService).revogarRefreshToken("cookie-abc")
    }

    @Test
    fun `logout nao faz nada quando cookie eh null`() {
        authService.logout(null)

        verify(refreshTokenService, never()).revogarRefreshToken(org.mockito.ArgumentMatchers.anyString())
    }

    @Test
    fun `loginSuccess inclui multiplos perfis no access token`() {
        val usuario = Usuario(id = 2, email = "admin@ifce.edu.br", nome = "Admin").apply {
            perfis = mutableSetOf(Perfil.ROLE_ADMINISTRADOR, Perfil.ROLE_COLABORADOR)
        }
        val novoRefresh =
            RefreshToken(token = "refresh-xyz", usuario = usuario, expiraEm = java.time.LocalDateTime.now().plusDays(1))
        val captorRoles = arrayOfNulls<Any>(1)
        `when`(usuarioRepository.findByEmail("admin@ifce.edu.br")).thenReturn(usuario)
        org.mockito.Mockito.doAnswer {
            captorRoles[0] = it.arguments[1]
            "access-xyz"
        }.`when`(jwtService).gerarAccessToken(
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.anyList()
        )
        `when`(refreshTokenService.gerarRefreshToken(usuario)).thenReturn(novoRefresh)

        authService.loginSuccess("admin@ifce.edu.br")

        verify(jwtService, times(1)).gerarAccessToken(
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.anyList()
        )
        @Suppress("UNCHECKED_CAST")
        val roles = captorRoles[0] as List<String>
        assertEquals(2, roles.size)
        assertTrue(roles.contains("ROLE_ADMINISTRADOR"))
        assertTrue(roles.contains("ROLE_COLABORADOR"))
    }
}
