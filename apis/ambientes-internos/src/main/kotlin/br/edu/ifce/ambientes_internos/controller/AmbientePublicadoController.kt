package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbientePublicadoUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasAmbientesPaginadosRes
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private const val AMBIENTE_PUBLICADO_PATH = "/api/ambientes/publicados"

@RestController
@RequestMapping(AMBIENTE_PUBLICADO_PATH)
class AmbientePublicadoController(
    private val useCasesPublicado: IAmbientePublicadoUseCases
) : BaseController<AmbienteRes>(useCasesPublicado) {

    @GetMapping("/esquadrias")
    fun listarEsquadriasAmbientes(
        @RequestParam ids: Set<Long>,
        pageable: Pageable
    ): ResponseEntity<EsquadriasAmbientesPaginadosRes> {
        return ResponseEntity.ok(useCasesPublicado.listarEsquadriasAmbientes(ids, pageable))
    }

}