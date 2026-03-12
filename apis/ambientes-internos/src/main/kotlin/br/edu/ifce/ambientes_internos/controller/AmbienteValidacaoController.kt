package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbientePublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteValidacaoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasAmbientesPaginadosRes
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

const val AMBIENTE_VALIDACAO_PATH = "/api/ambientes/validacao"

@RestController
@RequestMapping(AMBIENTE_VALIDACAO_PATH)
class AmbienteValidacaoController(val useCases: IAmbienteValidacaoUseCases) {

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

    @PatchMapping("/{id}/publicar")
    fun publicarAmbiente(@PathVariable id: Long): ResponseEntity<StatusAmbiente> {
        return ResponseEntity.ok(useCases.publicarAmbiente(id))
    }

    @PatchMapping("/{id}/privar")
    fun privarAmbiente(@PathVariable id: Long): ResponseEntity<StatusAmbiente> {
        return ResponseEntity.ok(useCases.privarAmbiente(id))
    }

}