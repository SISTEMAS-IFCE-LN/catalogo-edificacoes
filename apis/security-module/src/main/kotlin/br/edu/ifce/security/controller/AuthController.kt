package br.edu.ifce.security.controller

import br.edu.ifce.security.config.JwtProperties
import br.edu.ifce.security.model.application.interfaces.IAuthService
import br.edu.ifce.security.model.dto.LoginResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: IAuthService,
    private val jwtProperties: JwtProperties
) {

    @PostMapping("/login/success", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun loginSuccess(
        authentication: Authentication,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val email = authentication.name
        val tokensPair = authService.loginSuccess(email)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        response.addCookie(criarCookieRefreshToken(tokensPair.refreshToken))
        return ResponseEntity.ok(
            LoginResponse(
                accessToken = tokensPair.accessToken,
                expiresIn = jwtProperties.accessTokenExpiration
            )
        )
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(value = "refreshToken", required = false) refreshTokenCookie: String?,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val tokenCookie = refreshTokenCookie
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val tokensPair = authService.refresh(tokenCookie)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        response.addCookie(criarCookieRefreshToken(tokensPair.refreshToken))
        return ResponseEntity.ok(
            LoginResponse(
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
        response.addCookie(criarCookieRefreshTokenVazio())
        return ResponseEntity.noContent().build()
    }

    private fun criarCookieRefreshToken(token: String): Cookie {
        return Cookie("refreshToken", token).apply {
            isHttpOnly = true
            secure = jwtProperties.cookieSecure
            path = "/"
            maxAge = jwtProperties.refreshExpiration.toInt()
            setAttribute("SameSite", "Strict")
        }
    }

    private fun criarCookieRefreshTokenVazio(): Cookie {
        return Cookie("refreshToken", "").apply {
            isHttpOnly = true
            secure = jwtProperties.cookieSecure
            path = "/"
            maxAge = 0
            setAttribute("SameSite", "Strict")
        }
    }

}
