package br.edu.ifce.security.config

import br.edu.ifce.security.config.properties.FrontendProperties
import br.edu.ifce.security.model.application.interfaces.IAuthService
import br.edu.ifce.security.model.application.interfaces.ICookieService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class OAuth2LoginSuccessHandler(
    private val authService: IAuthService,
    private val cookieService: ICookieService,
    private val frontendProperties: FrontendProperties
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val email = authentication.name
        val tokensPair = authService.loginSuccess(email)!!

        response.addCookie(cookieService.criarCookieRefreshToken(tokensPair.refreshToken!!))

        val tokenEncoded = URLEncoder.encode(tokensPair.accessToken, StandardCharsets.UTF_8.toString())
        val frontendSuccess = frontendProperties.callbackSuccessUrl?.takeIf { it.isNotBlank() }

        val targetUrl = if (frontendSuccess != null) {
            "$frontendSuccess#token=$tokenEncoded"
        } else {
            "/callback.html#token=$tokenEncoded"
        }

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
