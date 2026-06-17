package br.edu.ifce.security.config

import br.edu.ifce.security.model.application.interfaces.IAuthService
import br.edu.ifce.security.model.application.interfaces.ICookieService
import br.edu.ifce.security.model.dto.LoginResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2LoginSuccessHandler(
    private val authService: IAuthService,
    private val cookieService: ICookieService,
    private val jwtProperties: JwtProperties,
    private val objectMapper: ObjectMapper
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val email = authentication.name
        val tokensPair = authService.loginSuccess(email)

        if (tokensPair == null) {
            response.status = HttpStatus.UNAUTHORIZED.value()
            return
        }

        response.addCookie(cookieService.criarCookieRefreshToken(tokensPair.refreshToken))

        val loginResponse = LoginResponse(
            accessToken = tokensPair.accessToken,
            expiresIn = jwtProperties.accessTokenExpiration
        )

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpStatus.OK.value()
        objectMapper.writeValue(response.outputStream, loginResponse)
    }
}
