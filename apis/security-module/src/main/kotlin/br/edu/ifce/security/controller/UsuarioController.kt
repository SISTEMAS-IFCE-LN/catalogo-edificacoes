package br.edu.ifce.security.controller

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.application.interfaces.IUsuarioService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
class UsuarioController(private val service: IUsuarioService) {

    @PatchMapping("/{id}/perfis")
    fun atualizarPerfis(
        @PathVariable id: Long,
        @RequestBody perfis: Set<Perfil>
    ): ResponseEntity<Void> {
        service.atualizarPerfis(id, perfis)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/desativar")
    fun desativarUsuario(@PathVariable id: Long): ResponseEntity<Void> {
        service.desativarUsuario(id)
        return ResponseEntity.noContent().build()
    }
}
