package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.model.domain.RefreshToken
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.RefreshTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    @field:Value($$"${jwt.refresh-expiration:43200000}") // 12 horas em milissegundos
    private val refreshTokenExpiration: Long
) {
    @Transactional
    fun gerarRefreshToken(usuario: Usuario): RefreshToken {
        // Revogar tokens antigos
        val tokenAntigo = refreshTokenRepository.findByUsuarioAndRevogadoFalse(usuario)
        if (tokenAntigo != null) {
            tokenAntigo.revogado = true
            refreshTokenRepository.save(tokenAntigo)
        }

        val novoToken = RefreshToken(
            token = UUID.randomUUID().toString(),
            usuario = usuario,
            expiraEm = LocalDateTime.now().plusSeconds(refreshTokenExpiration.div(1000))
        )

        return refreshTokenRepository.save(novoToken)
    }

    @Transactional(readOnly = true)
    fun validarRefreshToken(token: String): Boolean {
        val refreshToken = refreshTokenRepository.findByToken(token) ?: return false
        return !refreshToken.revogado && refreshToken.expiraEm.isAfter(LocalDateTime.now())
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
    fun limparTokensExpirados(usuario: Usuario) {
        refreshTokenRepository.deleteByUsuarioAndExpiraEmBefore(usuario, LocalDateTime.now())
    }
}