package br.edu.ifce.security.model.application.interfaces

import jakarta.servlet.http.Cookie

interface ICookieService {

    fun criarCookieRefreshToken(token: String): Cookie

    fun criarCookieRefreshToken(): Cookie
}