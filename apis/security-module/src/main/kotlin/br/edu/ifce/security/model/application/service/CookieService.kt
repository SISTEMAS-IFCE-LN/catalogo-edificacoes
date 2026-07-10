package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.config.JwtProperties
import br.edu.ifce.security.model.application.interfaces.ICookieService
import jakarta.servlet.http.Cookie
import org.springframework.stereotype.Service

@Service
class CookieService(
    private val jwtProperties: JwtProperties
): ICookieService {

    private val sameSite = if (jwtProperties.cookieSecure) "None" else "Lax"

    override fun criarCookieRefreshToken(token: String): Cookie {
        return Cookie("refreshToken", token).apply {
            isHttpOnly = true
            secure = jwtProperties.cookieSecure
            path = "/"
            maxAge = jwtProperties.refreshExpiration.toInt()
            setAttribute("SameSite", sameSite)
        }

    }

    override fun criarCookieRefreshToken(): Cookie {
        return Cookie("refreshToken", "").apply {
            isHttpOnly = true
            secure = jwtProperties.cookieSecure
            path = "/"
            maxAge = 0
            setAttribute("SameSite", sameSite)
        }
    }
}