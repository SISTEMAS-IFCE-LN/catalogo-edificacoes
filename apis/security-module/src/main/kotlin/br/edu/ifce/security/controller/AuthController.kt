package br.edu.ifce.security.controller

import br.edu.ifce.common.config.ApiPaths.AUTH_PATH
import br.edu.ifce.security.config.properties.JwtProperties
import br.edu.ifce.security.model.application.interfaces.IAuthService
import br.edu.ifce.security.model.application.interfaces.ICookieService
import br.edu.ifce.security.model.dto.LoginRes
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AUTH_PATH)
class AuthController(
    private val authService: IAuthService,
    private val cookieService: ICookieService,
    private val jwtProperties: JwtProperties
) {

    @GetMapping("/csrf-token")
    fun csrfToken(request: HttpServletRequest): ResponseEntity<Map<String, String>> {
        val csrfToken = request.getAttribute(CsrfToken::class.java.name) as? CsrfToken
            ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        return ResponseEntity.ok(mapOf("token" to csrfToken.token))
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(value = "refreshToken", required = false) refreshTokenCookie: String?,
        response: HttpServletResponse
    ): ResponseEntity<LoginRes> {
        val tokenCookie = refreshTokenCookie
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val tokensPair = authService.refresh(tokenCookie)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        if (tokensPair.refreshToken != null)
            response.addCookie(cookieService.criarCookieRefreshToken(tokensPair.refreshToken))
        return ResponseEntity.ok(
            LoginRes(
                accessToken = tokensPair.accessToken,
                expiresIn = jwtProperties.accessTokenExpiration
            )
        )
    }

    @PostMapping("/logout")
    fun logout(
        @CookieValue(value = "refreshToken", required = false) refreshTokenCookie: String?,
        response: HttpServletResponse
    ): ResponseEntity<Void> {
        authService.logout(refreshTokenCookie)
        response.addCookie(cookieService.criarCookieRefreshToken())
        return ResponseEntity.noContent().build()
    }

}
