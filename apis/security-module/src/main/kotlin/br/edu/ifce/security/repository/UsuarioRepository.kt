package br.edu.ifce.security.repository

import br.edu.ifce.security.domain.Perfil
import br.edu.ifce.security.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository

interface UsuarioRepository: JpaRepository<Usuario, Long> {
    fun findByEmail(email: String): Usuario?
    fun countByAtivoTrueAndPerfisContains(perfil: Perfil): Long
}