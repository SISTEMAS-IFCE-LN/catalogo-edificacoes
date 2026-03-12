package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbientePublicadoUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasAmbientesPaginadosRes
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

const val AMBIENTE_PUBLICADO_PATH = "/api/ambientes/publicados"

@RestController
@RequestMapping(AMBIENTE_PUBLICADO_PATH)
class AmbientePublicadoController(val useCases: IAmbientePublicadoUseCases) {

    @GetMapping
    fun listarAmbientes(pageable: Pageable): ResponseEntity<AmbientesBasicosPaginadosRes> {
        return ResponseEntity.ok(useCases.listarAmbientes(pageable))
    }

    @GetMapping("/tipo")
    fun listarAmbientesPorTipo(
        @RequestParam tipo: String,
        pageable: Pageable
    ): ResponseEntity<AmbientesBasicosPaginadosRes> {
        return ResponseEntity.ok(useCases.listarAmbientesPorTipo(tipo, pageable))
    }

    @GetMapping("/nome")
    fun listarAmbientesPorNome(
        @RequestParam nome: String,
        pageable: Pageable
    ): ResponseEntity<AmbientesBasicosPaginadosRes> {
        return ResponseEntity.ok(useCases.listarAmbientesPorNome(nome, pageable))
    }

    @GetMapping("/localizacao")
    fun listarAmbientesPorLocalizacao(
        @RequestParam bloco: String?,
        @RequestParam unidade: String?,
        @RequestParam andar: Int?,
        pageable: Pageable
    ): ResponseEntity<AmbientesBasicosPaginadosRes> {
        val localizacao = LocalizacaoPesquisaReq(bloco, unidade, andar)
        return ResponseEntity.ok(useCases.listarAmbientesPorLocalizacao(localizacao, pageable))
    }

    @GetMapping("/{id}")
    fun obterAmbientePorId(@PathVariable id: Long): ResponseEntity<AmbienteRes> {
        return ResponseEntity.ok(useCases.obterAmbientePorId(id))
    }

    @GetMapping("/esquadrias")
    fun listarEsquadriasAmbientes(
        @RequestParam ids: Set<Long>,
        pageable: Pageable
    ): ResponseEntity<EsquadriasAmbientesPaginadosRes> {
        return ResponseEntity.ok(useCases.listarEsquadriasAmbientes(ids, pageable))
    }

}