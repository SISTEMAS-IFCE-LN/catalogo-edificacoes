package br.edu.ifce.security.unitarios

import br.edu.ifce.security.config.properties.JwtProperties
import br.edu.ifce.security.model.application.service.CookieService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CookieServiceTest {

    private fun props(secure: Boolean = true, sameSite: String = "None") =
        JwtProperties(accessTokenExpiration = 900L, refreshExpiration = 43200L, cookieSecure = secure, sameSite = sameSite)

    @Test
    fun `criarCookieRefreshToken com token retorna cookie configurado`() {
        val service = CookieService(props(secure = true))

        val cookie = service.criarCookieRefreshToken("token-abc")

        assertEquals("refreshToken", cookie.name)
        assertEquals("token-abc", cookie.value)
        assertTrue(cookie.isHttpOnly)
        assertTrue(cookie.secure)
        assertEquals("/", cookie.path)
        assertEquals(43200, cookie.maxAge)
        assertEquals("None", cookie.getAttribute("SameSite"))
    }

    @Test
    fun `criarCookieRefreshToken sem token retorna cookie de limpeza com maxAge zero`() {
        val service = CookieService(props(secure = true))

        val cookie = service.criarCookieRefreshToken()

        assertEquals("refreshToken", cookie.name)
        assertEquals("", cookie.value)
        assertTrue(cookie.isHttpOnly)
        assertTrue(cookie.secure)
        assertEquals("/", cookie.path)
        assertEquals(0, cookie.maxAge)
        assertEquals("None", cookie.getAttribute("SameSite"))
    }

    @Test
    fun `criarCookieRefreshToken respeita cookieSecure false`() {
        val service = CookieService(props(secure = false))

        val cookie = service.criarCookieRefreshToken("token-abc")

        assertFalse(cookie.secure)
    }
}
