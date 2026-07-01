package br.edu.ifce.ambientes_internos.testcontroller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.NoSuchElementException

private const val TEST_PATH = "/test/excecoes"

@Validated
@RestController
@RequestMapping(TEST_PATH)
class TestExceptionController {

    @GetMapping("/illegal-argument")
    fun illegalArgument() {
        throw IllegalArgumentException("Já existe um ambiente com esse nome nessa localização")
    }

    @GetMapping("/no-such-element")
    fun noSuchElement() {
        throw NoSuchElementException("Ambiente não encontrado")
    }

    @GetMapping("/response-status-404")
    fun responseStatus404() {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")
    }

    @GetMapping("/response-status-409")
    fun responseStatus409() {
        throw ResponseStatusException(HttpStatus.CONFLICT, "Ação negada: lockout")
    }

    @PostMapping("/metodo-valid", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun methodValid(@RequestBody @Valid body: TestReq): TestRes {
        return TestRes(body.nome)
    }

    @PostMapping("/metodo-valid-vazio", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun methodValidVazio(@RequestBody @Valid body: TestReq): TestRes {
        return TestRes(body.nome)
    }

    @GetMapping("/constraint-violation/{nome}")
    fun constraintViolation(@PathVariable @NotBlank(message = "O nome é obrigatório") nome: String): String {
        return nome
    }

    @PostMapping("/not-readable", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun notReadable(@RequestBody body: Any): Any {
        return body
    }

    @GetMapping("/missing-param")
    fun missingParam(
        @RequestParam("nome")
        @NotBlank(message = "O nome é obrigatório")
        nome: String
    ): String = nome

    @GetMapping("/data-integrity")
    fun dataIntegrity() {
        val rootCause = RuntimeException("duplicate key value violates unique constraint")
        throw DataIntegrityViolationException("could not execute statement", rootCause)
    }

    @GetMapping("/data-integrity-no-cause")
    fun dataIntegrityNoCause() {
        throw DataIntegrityViolationException("violação genérica de integridade")
    }

    @GetMapping("/general")
    fun general() {
        throw RuntimeException("boom")
    }
}

data class TestReq(
    @field:NotBlank(message = "O nome é obrigatório")
    val nome: String
)

data class TestRes(val nome: String)
