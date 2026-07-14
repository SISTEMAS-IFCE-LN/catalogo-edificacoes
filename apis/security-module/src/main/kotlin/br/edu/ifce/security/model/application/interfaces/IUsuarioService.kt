package br.edu.ifce.security.model.application.interfaces

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.dto.UsuarioRes
import br.edu.ifce.security.model.dto.UsuariosPaginadosRes
import org.springframework.data.domain.Pageable

interface IUsuarioService {

    fun atualizarPerfis(id: Long, novosPerfis: Set<Perfil>)

    fun desativarUsuario(id: Long)

    fun ativarUsuario(id: Long)

    fun listarUsuarios(pageable: Pageable): UsuariosPaginadosRes

    fun obterUsuarioPorEmail(email: String): UsuarioRes

    fun listarUsuariosPorNome(nome: String, pageable: Pageable): UsuariosPaginadosRes

}
