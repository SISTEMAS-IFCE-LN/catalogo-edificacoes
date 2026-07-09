package br.edu.ifce.security.controller

import br.edu.ifce.common.config.ApiPaths.USUARIOS_PATH
import br.edu.ifce.common.config.MsgsSpringValidation.MSG_MAX_CARACTERES
import br.edu.ifce.common.config.MsgsSpringValidation.MSG_POSITIVO
import br.edu.ifce.security.model.application.interfaces.IUsuarioService
import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.dto.UsuarioRes
import br.edu.ifce.security.model.dto.UsuariosPaginadosRes
import jakarta.validation.constraints.*
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping(USUARIOS_PATH)
class UsuarioController(private val service: IUsuarioService) {

    @PatchMapping("/{id}/perfis")
    fun atualizarPerfis(
        @PathVariable @NotNull @Positive(message = MSG_POSITIVO) id: Long,
        @RequestBody @NotEmpty perfis: Set<Perfil>
    ): ResponseEntity<Void> {
        service.atualizarPerfis(id, perfis)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/desativar")
    fun desativarUsuario(@PathVariable @NotNull @Positive(message = MSG_POSITIVO) id: Long): ResponseEntity<Void> {
        service.desativarUsuario(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun listarUsuarios(pageable: Pageable): ResponseEntity<UsuariosPaginadosRes> =
        ResponseEntity.ok(service.listarUsuarios(pageable))

    @GetMapping("/email/{email}")
    fun obterUsuarioPorEmail(
        @PathVariable
        @NotBlank(message = "O email do usuário é obrigatório.")
        @Size(max = 255, message = MSG_MAX_CARACTERES)
        email: String
    ): ResponseEntity<UsuarioRes> =
        ResponseEntity.ok(service.obterUsuarioPorEmail(email))

    @GetMapping("/nomes/{nome}")
    fun listarUsuariosPorNome(
        @PathVariable
        @NotBlank(message = "O nome do usuário é obrigatório.")
        @Size(max = 100, message = MSG_MAX_CARACTERES)
        nome: String,
        pageable: Pageable
    ): ResponseEntity<UsuariosPaginadosRes> =
        ResponseEntity.ok(service.listarUsuariosPorNome(nome, pageable))
}
