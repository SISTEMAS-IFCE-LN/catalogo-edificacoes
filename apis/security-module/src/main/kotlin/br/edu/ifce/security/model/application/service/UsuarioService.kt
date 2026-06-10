package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.application.interfaces.IUsuarioService
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UsuarioService(private val repository: UsuarioRepository) : IUsuarioService {

    @Transactional
    override fun atualizarPerfis(id: Long, novosPerfis: Set<Perfil>) {
        val usuario = repository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado") }

        if (usuario.perfis.contains(Perfil.ROLE_ADMINISTRADOR) && !novosPerfis.contains(Perfil.ROLE_ADMINISTRADOR))
            verificarExclusaoAdm()

        val perfisFinais = novosPerfis.toMutableSet()
        perfisFinais.add(Perfil.ROLE_COLABORADOR)

        usuario.perfis = perfisFinais
        repository.save(usuario)
    }

    @Transactional
    override fun desativarUsuario(id: Long) {
        val usuario = repository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado") }

        if (usuario.perfis.contains(Perfil.ROLE_ADMINISTRADOR)) verificarExclusaoAdm()

        usuario.ativo = false
        repository.save(usuario)
    }

    private fun verificarExclusaoAdm() {
        val totalAdmins = repository.countByAtivoTrueAndPerfisContains(Perfil.ROLE_ADMINISTRADOR)
        if (totalAdmins <= 1)
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ação negada: Não é possível remover/desativar o último Administrador do sistema."
            )
    }
}
