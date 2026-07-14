package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.config.properties.JwtProperties
import br.edu.ifce.security.model.domain.RefreshToken
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties
) {
    @Transactional
    fun gerarRefreshToken(usuario: Usuario): RefreshToken {
        val tokenAntigo = refreshTokenRepository.findByUsuarioAndRevogadoFalse(usuario)
        if (tokenAntigo != null) {
            tokenAntigo.revogado = true
            refreshTokenRepository.save(tokenAntigo)
        }

        val novoToken = RefreshToken(
            token = UUID.randomUUID().toString(),
            usuario = usuario,
            expiraEm = LocalDateTime.now().plusSeconds(jwtProperties.refreshExpiration)
        )

        return refreshTokenRepository.save(novoToken)
    }

    @Transactional(readOnly = true)
    fun validarRefreshToken(token: String): RefreshToken? {
        val refreshToken = refreshTokenRepository.findByToken(token) ?: return null
        return when {
            !refreshToken.revogado && refreshToken.expiraEm.isAfter(LocalDateTime.now()) -> refreshToken
            else -> null
        }
    }

    @Transactional
    fun revogarRefreshToken(token: String) {
        val refreshToken = refreshTokenRepository.findByToken(token)
        if (refreshToken != null) {
            refreshToken.revogado = true
            refreshTokenRepository.save(refreshToken)
        }
    }

    @Transactional
    fun limparTokensExpirados(): Int {
        return refreshTokenRepository.deleteByExpiraEmBefore(LocalDateTime.now())
    }
}
