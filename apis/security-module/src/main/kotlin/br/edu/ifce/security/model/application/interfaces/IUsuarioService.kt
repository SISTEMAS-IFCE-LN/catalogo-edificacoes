package br.edu.ifce.security.model.application.interfaces

import br.edu.ifce.security.model.domain.Perfil

interface IUsuarioService {

    fun atualizarPerfis(id: Long, novosPerfis: Set<Perfil>)

    fun desativarUsuario(id: Long)

}
