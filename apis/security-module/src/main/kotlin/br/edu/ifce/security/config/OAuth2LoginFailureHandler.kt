package br.edu.ifce.security.config

import br.edu.ifce.security.config.properties.FrontendProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.web.util.UriComponentsBuilder

class OAuth2LoginFailureHandler(
    private val frontendProperties: FrontendProperties
) : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val erro = when ((exception as? OAuth2AuthenticationException)?.error?.errorCode) {
            "user_inactive" -> "inativo"
            "unauthorized_domain" -> "dominio"
            else -> "falha"
        }

        val destino = frontendProperties.callbackErrorUrl?.takeIf { it.isNotBlank() }
            ?: "/failure.html"

        val url = UriComponentsBuilder.fromUriString(destino)
            .queryParam("erro", erro)
            .build()
            .toUriString()

        response.sendRedirect(url)
    }
}
