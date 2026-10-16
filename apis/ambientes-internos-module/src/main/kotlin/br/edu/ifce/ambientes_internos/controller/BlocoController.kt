package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IBlocoUseCases
import br.edu.ifce.ambientes_internos.model.dto.bloco.BlocoRes
import br.edu.ifce.common.config.ApiPaths.BLOCOS_PATH
import br.edu.ifce.common.config.MsgsSpringValidation.MSG_MAX_CARACTERES
import jakarta.validation.constraints.Size
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
        @Size(max = 50, message = MSG_MAX_CARACTERES)
        unidade: String
    ): ResponseEntity<List<BlocoRes>> = ResponseEntity.ok(useCases.listarPorUnidade(unidade))

}