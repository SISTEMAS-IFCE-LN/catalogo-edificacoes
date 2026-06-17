package br.edu.ifce.security.model.dto

import br.edu.ifce.security.model.domain.Usuario
import org.springframework.data.domain.Page

data class UsuariosPaginadosRes(
    val usuarios: List<UsuarioRes>,
    val dadosPaginacao: DadosPaginacao
) {
    companion object {
        fun from(page: Page<Usuario>): UsuariosPaginadosRes = UsuariosPaginadosRes(
            usuarios = page.content.map { UsuarioRes.from(it) },
            dadosPaginacao = DadosPaginacao.from(page)
        )
    }
}
