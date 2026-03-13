package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteValidacaoUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val AMBIENTE_VALIDACAO_PATH = "/api/ambientes/validacao"

@RestController
@RequestMapping(AMBIENTE_VALIDACAO_PATH)
class AmbienteValidacaoController(
    private val useCasesValidacao: IAmbienteValidacaoUseCases
) : BaseController<AmbienteRes>(useCasesValidacao) {

    @PatchMapping("/{id}/publicar")
    fun publicarAmbiente(@PathVariable id: Long): ResponseEntity<Void> {
        useCasesValidacao.publicarAmbiente(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/privar")
    fun privarAmbiente(@PathVariable id: Long): ResponseEntity<Void> {
        useCasesValidacao.privarAmbiente(id)
        return ResponseEntity.noContent().build()
    }

}