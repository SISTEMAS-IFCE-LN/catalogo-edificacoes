package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.*
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.ListaGeometriasAmbienteRes
import jakarta.validation.Valid
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.net.URI

private const val AMBIENTE_NAO_PUBLICADO_PATH = "/api/ambientes/nao-publicados"
private const val MSG_VAL_PD = "O pé direito deve ser positivo"
private const val MSG_VAL_PD_ESCALA = "O pé direito deve ter no máximo 7 dígitos inteiros e 2 casas decimais."

@Validated
@RestController
@RequestMapping(AMBIENTE_NAO_PUBLICADO_PATH)
class AmbienteNaoPublicadoController(
    private val useCasesNaoPublicado: IAmbienteNaoPublicadoUseCases
) : BaseController<AmbienteRes>(useCasesNaoPublicado) {

    @PostMapping
    fun cadastrarAmbiente(@RequestBody @Valid ambienteReq: AmbienteReq): ResponseEntity<AmbienteRes> {
        val ambienteRes = useCasesNaoPublicado.cadastrarAmbiente(ambienteReq)
        return ResponseEntity
            .created(URI.create("${AMBIENTE_NAO_PUBLICADO_PATH}/${ambienteRes.id}"))
            .body(ambienteRes)
    }

    @PatchMapping("/{id}/dados-basicos")
    fun atualizarDadosBasicosAmbiente(
        @PathVariable @Positive(message = MSG_VAL_ID)
        id: Long,
        @RequestBody @Valid
        ambienteAtualizado: AmbienteBasicoReq
    ): ResponseEntity<AmbienteBasicoRes> {
        return ResponseEntity
            .ok(useCasesNaoPublicado.atualizarDadosBasicosAmbiente(id, ambienteAtualizado))
    }

    @PatchMapping("/{id}/geometrias/incluir")
    fun incluirGeometriasAmbiente(
        @PathVariable @Positive(message = MSG_VAL_ID)
        id: Long,
        @RequestBody @NotEmpty(message = "$MSG_LISTA_VAZIA uma geometria.")
        geometriasAdd: Set<@Valid GeometriaAmbienteReq>
    ): ResponseEntity<ListaGeometriasAmbienteRes> {
        return ResponseEntity.ok(useCasesNaoPublicado.incluirGeometriasAmbiente(id, geometriasAdd))
    }

    @PatchMapping("/{id}/geometrias/atualizar")
    fun atualizarGeometriasAmbiente(
        @PathVariable @Positive(message = MSG_VAL_ID)
        id: Long,
        @RequestBody @NotEmpty(message = "$MSG_LISTA_VAZIA uma geometria.")
        geometriasAtualizadas: Set<@Valid GeometriaAmbienteReq>
    ): ResponseEntity<ListaGeometriasAmbienteRes> {
        return ResponseEntity.ok(useCasesNaoPublicado.atualizarGeometriasAmbiente(id, geometriasAtualizadas))
    }

    @PatchMapping("/{id}/pes-direitos/incluir")
    fun incluirPesDireitosAmbiente(
        @PathVariable @Positive(message = MSG_VAL_ID)
        id: Long,
        @RequestBody @NotEmpty(message = "$MSG_LISTA_VAZIA um pé direito.")
        pesDireitos: Set<
            @Positive(message = MSG_VAL_PD)
            @Digits(integer = 7, fraction = 2, message = MSG_VAL_PD_ESCALA)
            BigDecimal
        >
    ): ResponseEntity<Set<BigDecimal>> {
        return ResponseEntity.ok(useCasesNaoPublicado.incluirPesDireitosAmbiente(id, pesDireitos))
    }

    @PatchMapping("/{id}/pes-direitos/atualizar")
    fun atualizarPesDireitosAmbiente(
        @PathVariable @Positive(message = MSG_VAL_ID)
        id: Long,
        @RequestBody @NotEmpty(message = "$MSG_LISTA_VAZIA um pé direito.")
        pesDireitos: Set<
            @Positive(message = MSG_VAL_PD)
            @Digits(integer = 7, fraction = 2, message = MSG_VAL_PD_ESCALA)
            BigDecimal
        >
    ): ResponseEntity<Set<BigDecimal>> {
        return ResponseEntity.ok(useCasesNaoPublicado.atualizarPesDireitosAmbiente(id, pesDireitos))
    }

    @PatchMapping("/{id}/esquadrias/incluir")
    fun incluirEsquadriasAmbiente(
        @PathVariable @Positive(message = MSG_VAL_ID)
        id: Long,
        @RequestBody @NotEmpty(message = "$MSG_LISTA_VAZIA uma esquadria.")
        esquadrias: Set<@Valid EsquadriaReq>
    ): ResponseEntity<EsquadriasDetalhesRes> {
        return ResponseEntity.ok(useCasesNaoPublicado.incluirEsquadriasAmbiente(id, esquadrias))
    }

    @PatchMapping("/{id}/esquadrias/atualizar")
    fun atualizarEsquadriasAmbiente(
        @PathVariable @Positive(message = MSG_VAL_ID)
        id: Long,
        @RequestBody @NotEmpty(message = "$MSG_LISTA_VAZIA uma esquadria.")
        esquadrias: Set<@Valid EsquadriaReq>
    ): ResponseEntity<EsquadriasDetalhesRes> {
        return ResponseEntity.ok(useCasesNaoPublicado.atualizarEsquadriasAmbiente(id, esquadrias))
    }

    @PatchMapping("/{id}/informacao-adicional")
    fun atualizarInformacaoAdicionalAmbiente(
        @PathVariable @Positive(message = MSG_VAL_ID)
        id: Long,
        @RequestBody @Size(
            max = 255,
            message = MSG_MAX_CARACTERES
        )
        informacaoAdicional: String
    ): ResponseEntity<String> {
        return ResponseEntity
            .ok(useCasesNaoPublicado.atualizarInformacaoAdicionalAmbiente(id, informacaoAdicional))
    }

    @PostMapping("/{id}")
    fun alterarTipoDadosAmbiente(
        @PathVariable @Positive(message = MSG_VAL_ID)
        id: Long,
        @RequestBody @Valid
        ambiente: AmbienteReq
    ): ResponseEntity<AmbienteRes> {
        val ambienteRes = useCasesNaoPublicado.alterarTipoDadosAmbiente(id, ambiente)
        return ResponseEntity
            .created(URI.create("${AMBIENTE_NAO_PUBLICADO_PATH}/${ambienteRes.id}"))
            .body(ambienteRes)
    }

    @PostMapping("/{id}/duplicar")
    fun duplicarAmbiente(
        @PathVariable @Positive(message = MSG_VAL_ID)
        id: Long,
        @RequestBody @Valid
        dados: AmbienteNomeLocalizacaoReq
    ): ResponseEntity<AmbienteRes> {
        val ambienteRes = useCasesNaoPublicado.duplicarAmbiente(id, dados)
        return ResponseEntity
            .created(URI.create("${AMBIENTE_NAO_PUBLICADO_PATH}/${ambienteRes.id}"))
            .body(ambienteRes)
    }

    @PatchMapping("/validar")
    fun enviarValidacaoAmbientes(
        @RequestBody @NotEmpty(message = "$MSG_LISTA_VAZIA um ID.")
        ids: Set<@Positive(message = MSG_VAL_ID) Long>
    ): ResponseEntity<Void> {
        useCasesNaoPublicado.enviarValidacaoAmbientes(ids)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping
    fun deletarAmbientes(
        @RequestBody @NotEmpty(message = "$MSG_LISTA_VAZIA um ID.")
        ids: Set<@Positive(message = MSG_VAL_ID) Long>
    ): ResponseEntity<Void> {
        useCasesNaoPublicado.deletarAmbientes(ids)
        return ResponseEntity.noContent().build()
    }

}