package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteValidacaoUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.common.config.ApiPaths.AMBIENTES_VALIDACAO_PATH
import br.edu.ifce.common.config.MsgsSpringValidation.MSG_POSITIVO
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping(AMBIENTES_VALIDACAO_PATH)
class AmbienteValidacaoController(
    private val useCasesValidacao: IAmbienteValidacaoUseCases
) : BaseController<AmbienteRes>(useCasesValidacao) {

    @PatchMapping("/{id}/publicar")
    fun publicarAmbiente(@PathVariable @Positive(message = MSG_POSITIVO) id: Long): ResponseEntity<Void> {
        useCasesValidacao.publicarAmbiente(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/privar")
    fun privarAmbiente(@PathVariable @Positive(message = MSG_POSITIVO) id: Long): ResponseEntity<Void> {
        useCasesValidacao.privarAmbiente(id)
        return ResponseEntity.noContent().build()
    }

}