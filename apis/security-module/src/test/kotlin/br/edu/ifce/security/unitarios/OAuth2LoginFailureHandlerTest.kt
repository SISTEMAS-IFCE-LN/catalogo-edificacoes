package br.edu.ifce.security.unitarios

import br.edu.ifce.security.config.OAuth2LoginFailureHandler
import br.edu.ifce.security.config.properties.FrontendProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error

class OAuth2LoginFailureHandlerTest {

    private fun redirecionar(errorCode: String, errorUrl: String? = "http://localhost:8000/login"): String {
        val handler = OAuth2LoginFailureHandler(FrontendProperties(callbackErrorUrl = errorUrl))
        val response = MockHttpServletResponse()

        handler.onAuthenticationFailure(
            MockHttpServletRequest(), response, OAuth2AuthenticationException(OAuth2Error(errorCode))
        )
        return response.redirectedUrl!!
    }

    @Test
    fun `user_inactive redireciona com erro=inativo`() {
        assertEquals("http://localhost:8000/login?erro=inativo", redirecionar("user_inactive"))
    }

    @Test
    fun `unauthorized_domain redireciona com erro=dominio`() {
        assertEquals("http://localhost:8000/login?erro=dominio", redirecionar("unauthorized_domain"))
    }

    @Test
    fun `codigo desconhecido redireciona com erro=falha`() {
        assertEquals("http://localhost:8000/login?erro=falha", redirecionar("access_denied"))
    }

    @Test
    fun `sem callbackErrorUrl usa fallback failure html`() {
        assertEquals("/failure.html?erro=falha", redirecionar("access_denied", errorUrl = null))
    }
}
