package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.*
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.ListaGeometriasAmbienteRes
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.net.URI

const val AMBIENTE_NAO_PUBLICADO_PATH = "/api/ambientes/nao-publicados"

@RestController
@RequestMapping(AMBIENTE_NAO_PUBLICADO_PATH)
class AmbienteNaoPublicadoController(val useCases: IAmbienteNaoPublicadoUseCases) {

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

    @PostMapping
    fun cadastrarAmbiente(@RequestBody ambienteReq: AmbienteReq): ResponseEntity<AmbienteRes> {
        val ambienteRes = useCases.cadastrarAmbiente(ambienteReq)
        return ResponseEntity.created(URI.create("${AMBIENTE_NAO_PUBLICADO_PATH}/${ambienteRes.id}")).body(ambienteRes)
    }

    @PatchMapping("/{id}/dados-basicos")
    fun atualizarDadosBasicosAmbiente(
        @PathVariable id: Long,
        @RequestBody ambienteAtualizado: AmbienteBasicoReq
    ): ResponseEntity<AmbienteBasicoRes> {
        return ResponseEntity.ok(useCases.atualizarDadosBasicosAmbiente(id, ambienteAtualizado))
    }

    @PatchMapping("/{id}/geometrias/incluir")
    fun incluirGeometriasAmbiente(
        @PathVariable id: Long,
        @RequestBody geometriasAdd: Set<GeometriaAmbienteReq>
    ): ResponseEntity<ListaGeometriasAmbienteRes> {
        return ResponseEntity.ok(useCases.incluirGeometriasAmbiente(id, geometriasAdd))
    }

    @PatchMapping("/{id}/geometrias/atualizar")
    fun atualizarGeometriasAmbiente(
        @PathVariable id: Long,
        @RequestBody geometriasAtualizadas: Set<GeometriaAmbienteReq>
    ): ResponseEntity<ListaGeometriasAmbienteRes> {
        return ResponseEntity.ok(useCases.atualizarGeometriasAmbiente(id, geometriasAtualizadas))
    }

    @PatchMapping("/{id}/pes-direitos/incluir")
    fun incluirPesDireitosAmbiente(
        @PathVariable id: Long,
        @RequestBody pesDireitos: Set<BigDecimal>
    ): ResponseEntity<Set<BigDecimal>> {
        return ResponseEntity.ok(useCases.incluirPesDireitosAmbiente(id, pesDireitos))
    }

    @PatchMapping("/{id}/pes-direitos/atualizar")
    fun atualizarPesDireitosAmbiente(
        @PathVariable id: Long,
        @RequestBody pesDireitos: Set<BigDecimal>
    ): ResponseEntity<Set<BigDecimal>> {
        return ResponseEntity.ok(useCases.atualizarPesDireitosAmbiente(id, pesDireitos))
    }

    @PatchMapping("/{id}/esquadrias/incluir")
    fun incluirEsquadriasAmbiente(
        @PathVariable id: Long,
        @RequestBody esquadrias: Set<EsquadriaReq>
    ): ResponseEntity<EsquadriasDetalhesRes> {
        return ResponseEntity.ok(useCases.incluirEsquadriasAmbiente(id, esquadrias))
    }

    @PatchMapping("/{id}/esquadrias/atualizar")
    fun atualizarEsquadriasAmbiente(
        @PathVariable id: Long,
        @RequestBody esquadrias: Set<EsquadriaReq>
    ): ResponseEntity<EsquadriasDetalhesRes> {
        return ResponseEntity.ok(useCases.atualizarEsquadriasAmbiente(id, esquadrias))
    }

    @PatchMapping("/{id}/informacao-adicional")
    fun atualizarInformacaoAdicionalAmbiente(
        @PathVariable id: Long,
        @RequestBody informacaoAdicional: String
    ): ResponseEntity<String> {
        return ResponseEntity.ok(useCases.atualizarInformacaoAdicionalAmbiente(id, informacaoAdicional))
    }

    @PostMapping("/{id}")
    fun alterarTipoDadosAmbiente(
        @PathVariable id: Long,
        @RequestBody ambiente: AmbienteReq
    ): ResponseEntity<AmbienteRes> {
        val ambienteRes = useCases.alterarTipoDadosAmbiente(id, ambiente)
        return ResponseEntity.created(URI.create("${AMBIENTE_NAO_PUBLICADO_PATH}/${ambienteRes.id}")).body(ambienteRes)
    }

    @PostMapping("/{id}/duplicar")
    fun duplicarAmbiente(
        @PathVariable id: Long,
        @RequestBody dados: AmbienteNomeLocalizacaoReq
    ): ResponseEntity<AmbienteRes> {
        val ambienteRes = useCases.duplicarAmbiente(id, dados)
        return ResponseEntity.created(URI.create("${AMBIENTE_NAO_PUBLICADO_PATH}/${ambienteRes.id}")).body(ambienteRes)
    }

    @PatchMapping("/validar")
    fun enviarValidacaoAmbientes(@RequestBody ids: Set<Long>): ResponseEntity<Void> {
        useCases.enviarValidacaoAmbientes(ids)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping
    fun deletarAmbientes(@RequestBody ids: Set<Long>): ResponseEntity<Void> {
        useCases.deletarAmbientes(ids)
        return ResponseEntity.noContent().build()
    }

}