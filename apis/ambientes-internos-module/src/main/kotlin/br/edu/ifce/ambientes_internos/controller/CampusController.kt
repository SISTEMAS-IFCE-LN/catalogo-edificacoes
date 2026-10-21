package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.ICampusUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.CampusRes
import br.edu.ifce.common.config.ApiPaths.CAMPUS_PATH
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(CAMPUS_PATH)
class CampusController(private val useCases: ICampusUseCases) {

    @GetMapping
    fun listar(): ResponseEntity<List<CampusRes>> =
        ResponseEntity.ok(useCases.listar())

}
