package br.edu.ifce.security.model.repository

import br.edu.ifce.security.model.domain.RefreshToken
import br.edu.ifce.security.model.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface RefreshTokenRepository: JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?
    fun findByUsuarioAndRevogadoFalse(usuario: Usuario): RefreshToken?
    fun deleteByExpiraEmBefore(dataExpiracao: LocalDateTime): Int
}