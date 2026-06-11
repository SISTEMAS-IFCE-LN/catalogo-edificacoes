package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.model.application.interfaces.IAuthService
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.dto.TokensPair
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val usuarioRepository: UsuarioRepository
): IAuthService {

    override fun loginSuccess(email: String): TokensPair? {
        val usuario = usuarioRepository.findByEmail(email)
            ?: return null
        return gerarTokensPair(usuario)
    }

    override fun refresh(refreshTokenCookie: String): TokensPair? {
        val refreshToken = refreshTokenService.validarRefreshToken(refreshTokenCookie)
            ?: return null
        val usuario = refreshToken.usuario
        return gerarTokensPair(usuario)
    }

    override fun logout(refreshTokenCookie: String?) {
        if (refreshTokenCookie != null) refreshTokenService.revogarRefreshToken(refreshTokenCookie)
    }

    private fun gerarTokensPair(usuario: Usuario): TokensPair {
        val roles = usuario.perfis.map { it.name }
        return TokensPair(
            accessToken = jwtService.gerarAccessToken(usuario.id!!, usuario.email, roles),
            refreshToken = refreshTokenService.gerarRefreshToken(usuario).token
        )
    }

}
