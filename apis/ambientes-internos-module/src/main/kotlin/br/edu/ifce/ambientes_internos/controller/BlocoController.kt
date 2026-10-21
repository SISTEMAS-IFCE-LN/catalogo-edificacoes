package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IBlocoUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.BlocoRes
import br.edu.ifce.common.config.ApiPaths.BLOCOS_PATH
import br.edu.ifce.common.config.MsgsSpringValidation.MSG_POSITIVO
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping(BLOCOS_PATH)
class BlocoController(private val useCases: IBlocoUseCases) {

    @GetMapping
    fun listarPorUnidade(
        @RequestParam
        @Positive(message = MSG_POSITIVO)
        unidadeId: Long
    ): ResponseEntity<List<BlocoRes>> = ResponseEntity.ok(useCases.listarPorUnidade(unidadeId))

}
