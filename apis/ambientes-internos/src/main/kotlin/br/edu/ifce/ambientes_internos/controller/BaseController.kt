package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

const val MSG_VAL_ID = "O ID deve ser positivo."
const val MSG_LISTA_VAZIA = "Deve conter pelo menos "
const val MSG_MAX_CARACTERES = "Deve conter no máximo {max} caracteres."
private const val MSG_OBRIGATORIO = "do ambiente é obrigatório."

@Validated
abstract class BaseController<RES>(protected val useCases: IAmbienteUseCases<RES>) {

    @GetMapping
    fun listarAmbientes(pageable: Pageable): ResponseEntity<AmbientesBasicosPaginadosRes> {
        return ResponseEntity.ok(useCases.listarAmbientes(pageable))
    }

    @GetMapping("/tipo")
    fun listarAmbientesPorTipo(
        @RequestParam
        @NotBlank(message = "O tipo $MSG_OBRIGATORIO")
        @Size(
            max = 50,
            message = MSG_MAX_CARACTERES
        )
        tipo: String,
        pageable: Pageable
    ): ResponseEntity<AmbientesBasicosPaginadosRes> {
        return ResponseEntity.ok(useCases.listarAmbientesPorTipo(tipo, pageable))
    }

    @GetMapping("/nome")
    fun listarAmbientesPorNome(
        @RequestParam
        @NotBlank(message = "O nome $MSG_OBRIGATORIO")
        @Size(
            max = 50,
            message = MSG_MAX_CARACTERES
        )
        nome: String,
        pageable: Pageable
    ): ResponseEntity<AmbientesBasicosPaginadosRes> {
        return ResponseEntity.ok(useCases.listarAmbientesPorNome(nome, pageable))
    }

    @GetMapping("/localizacao")
    fun listarAmbientesPorLocalizacao(
        @RequestParam
        @Size(
            max = 50,
            message = MSG_MAX_CARACTERES
        )
        bloco: String?,
        @RequestParam
        @Size(
            max = 50,
            message = MSG_MAX_CARACTERES
        )
        unidade: String?,
        @RequestParam
        @Min(
            value = 0,
            message = "O andar deve ser maior ou igual a 0."
        )
        andar: Int?,
        pageable: Pageable
    ): ResponseEntity<AmbientesBasicosPaginadosRes> {
        val localizacao = LocalizacaoPesquisaReq(bloco, unidade, andar)
        return ResponseEntity.ok(useCases.listarAmbientesPorLocalizacao(localizacao, pageable))
    }

    @GetMapping("/{id}")
    fun obterAmbientePorId(@PathVariable @Positive(message = MSG_VAL_ID) id: Long): ResponseEntity<RES> {
        return ResponseEntity.ok(useCases.obterAmbientePorId(id))
    }

}

