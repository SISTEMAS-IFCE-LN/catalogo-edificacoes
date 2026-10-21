package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IUnidadeUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.UnidadeRes
import br.edu.ifce.common.config.ApiPaths.UNIDADES_PATH
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
@RequestMapping(UNIDADES_PATH)
class UnidadeController(private val useCases: IUnidadeUseCases) {

    @GetMapping
    fun listarPorCampus(
        @RequestParam
        @Positive(message = MSG_POSITIVO)
        campusId: Long
    ): ResponseEntity<List<UnidadeRes>> =
        ResponseEntity.ok(useCases.listarPorCampus(campusId))

}
