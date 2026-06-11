package br.edu.ifce.security.model.application.interfaces

import br.edu.ifce.security.model.dto.TokensPair

interface IAuthService {

    fun loginSuccess(email: String): TokensPair?

    fun refresh(refreshTokenCookie: String): TokensPair?

    fun logout(refreshTokenCookie: String?)

}
