package br.edu.ifce.security.controller

import br.edu.ifce.common.config.ApiPaths.AUTH_PATH
import br.edu.ifce.security.config.properties.JwtProperties
import br.edu.ifce.security.model.application.interfaces.IAuthService
import br.edu.ifce.security.model.application.interfaces.ICookieService
import br.edu.ifce.security.model.dto.LoginRes
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
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

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(value = "refreshToken", required = false) refreshTokenCookie: String?,
        response: HttpServletResponse
    ): ResponseEntity<LoginRes> {
        val tokenCookie = refreshTokenCookie
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val tokensPair = authService.refresh(tokenCookie)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
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
