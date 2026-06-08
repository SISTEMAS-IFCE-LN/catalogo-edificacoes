package br.edu.ifce.security.repository

import br.edu.ifce.security.domain.RefreshToken
import br.edu.ifce.security.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface RefreshTokenRepository: JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?
    fun findByUsuarioAndRevogadoFalse(usuario: Usuario): RefreshToken?
    fun deleteByUsuarioAndExpiraEmBefore(usuario: Usuario, dataExpiracao: LocalDateTime)
}