package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.application.interfaces.IUsuarioService
import br.edu.ifce.security.model.dto.UsuarioRes
import br.edu.ifce.security.model.dto.UsuariosPaginadosRes
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import kotlin.math.min

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

    @Transactional
    override fun ativarUsuario(id: Long) {
        val usuario = repository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado") }

        usuario.ativo = true
        repository.save(usuario)
    }

    @Transactional(readOnly = true)
    override fun listarUsuarios(pageable: Pageable): UsuariosPaginadosRes {
        val page = repository.findAll(limitarPageable(pageable))
        return UsuariosPaginadosRes.from(page)
    }

    @Transactional(readOnly = true)
    override fun obterUsuarioPorEmail(email: String): UsuarioRes {
        val usuario = repository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")
        return UsuarioRes.from(usuario)
    }

    @Transactional(readOnly = true)
    override fun listarUsuariosPorNome(nome: String, pageable: Pageable): UsuariosPaginadosRes {
        val page = repository.findByNomeContainingIgnoreCase(nome, limitarPageable(pageable))
        return UsuariosPaginadosRes.from(page)
    }

    private fun verificarExclusaoAdm() {
        val totalAdmins = repository.countByAtivoTrueAndPerfisContains(Perfil.ROLE_ADMINISTRADOR)
        if (totalAdmins <= 1)
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ação negada: Não é possível remover/desativar o último Administrador do sistema."
            )
    }

    private fun limitarPageable(pageable: Pageable): Pageable {
        if (pageable.isUnpaged) {
            return PageRequest.of(0, PAGE_SIZE_MAX)
        }
        return PageRequest.of(pageable.pageNumber, min(pageable.pageSize, PAGE_SIZE_MAX), pageable.sort)
    }

    companion object {
        private const val PAGE_SIZE_MAX = 100
    }
}
