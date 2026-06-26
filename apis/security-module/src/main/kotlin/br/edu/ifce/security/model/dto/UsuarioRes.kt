package br.edu.ifce.security.model.dto

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.Usuario
import java.time.LocalDateTime

data class UsuarioRes(
    val id: Long?,
    val email: String,
    val nome: String,
    val ativo: Boolean,
    val criadoEm: LocalDateTime,
    val perfis: Set<Perfil>
) {
    companion object {
        fun from(usuario: Usuario): UsuarioRes = UsuarioRes(
            id = usuario.id,
            email = usuario.email,
            nome = usuario.nome,
            ativo = usuario.ativo,
            criadoEm = usuario.criadoEm,
            perfis = usuario.perfis.toSet()
        )
    }
}
