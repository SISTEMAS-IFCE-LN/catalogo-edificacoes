package br.edu.ifce.security.controller

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.application.interfaces.IUsuarioService
import br.edu.ifce.security.model.dto.UsuarioRes
import br.edu.ifce.security.model.dto.UsuariosPaginadosRes
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val MSG_OBRIGATORIO = "é obrigatório(a)."
private const val MSG_MAX_CARACTERES_EMAIL = "deve ter no máximo 255 caracteres."
private const val MSG_MAX_CARACTERES_NOME = "deve ter no máximo 100 caracteres."

@Validated
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
class UsuarioController(private val service: IUsuarioService) {

    @PatchMapping("/{id}/perfis")
    fun atualizarPerfis(
        @PathVariable @NotNull @Positive id: Long,
        @RequestBody @NotEmpty perfis: Set<Perfil>
    ): ResponseEntity<Void> {
        service.atualizarPerfis(id, perfis)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/desativar")
    fun desativarUsuario(@PathVariable @NotNull @Positive id: Long): ResponseEntity<Void> {
        service.desativarUsuario(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun listarUsuarios(pageable: Pageable): ResponseEntity<UsuariosPaginadosRes> =
        ResponseEntity.ok(service.listarUsuarios(pageable))

    @GetMapping("/email/{email}")
    fun obterUsuarioPorEmail(
        @PathVariable
        @NotBlank(message = "O email $MSG_OBRIGATORIO")
        @Size(max = 255, message = "O email $MSG_MAX_CARACTERES_EMAIL")
        email: String
    ): ResponseEntity<UsuarioRes> =
        ResponseEntity.ok(service.obterUsuarioPorEmail(email))

    @GetMapping("/nomes/{nome}")
    fun listarUsuariosPorNome(
        @PathVariable
        @NotBlank(message = "O nome $MSG_OBRIGATORIO")
        @Size(max = 100, message = "O nome $MSG_MAX_CARACTERES_NOME")
        nome: String,
        pageable: Pageable
    ): ResponseEntity<UsuariosPaginadosRes> =
        ResponseEntity.ok(service.listarUsuariosPorNome(nome, pageable))
}
