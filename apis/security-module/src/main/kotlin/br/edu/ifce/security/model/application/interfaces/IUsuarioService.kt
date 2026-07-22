package br.edu.ifce.security.model.application.interfaces

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.dto.UsuarioRes
import br.edu.ifce.security.model.dto.UsuariosPaginadosRes
import org.springframework.data.domain.Pageable

interface IUsuarioService {

    fun atualizarPerfis(id: Long, novosPerfis: Set<Perfil>)

    fun desativar(id: Long)

    fun ativar(id: Long)

    fun listar(pageable: Pageable): UsuariosPaginadosRes

    fun obterPorId(id: Long): UsuarioRes

    fun obterPorEmail(email: String): UsuarioRes

    fun listarPorNome(nome: String, pageable: Pageable): UsuariosPaginadosRes

}
