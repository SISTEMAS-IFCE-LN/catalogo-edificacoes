package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.TestApplication
import br.edu.ifce.ambientes_internos.TestSecurityConfig
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
@DisplayName("Testes de integração do GlobalExceptionHandler")
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    // ========== IllegalArgumentException → 400 ==========

    @Test
    fun `IllegalArgumentException retorna 400 com mensagem do lancamento`() {
        mockMvc.perform(get("/test/excecoes/illegal-argument"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensagem").value("Já existe um ambiente com esse nome nessa localização"))
            .andExpect(jsonPath("$.dataHora").isNotEmpty)
    }

    // ========== NoSuchElementException → 404 ==========

    @Test
    fun `NoSuchElementException retorna 404 com mensagem do lancamento`() {
        mockMvc.perform(get("/test/excecoes/no-such-element"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.mensagem").value("Ambiente não encontrado"))
            .andExpect(jsonPath("$.dataHora").isNotEmpty)
    }

    // ========== ResponseStatusException → status preservado ==========

    @Test
    fun `ResponseStatusException NOT_FOUND retorna 404 com motivo`() {
        mockMvc.perform(get("/test/excecoes/response-status-404"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"))
    }

    @Test
    fun `ResponseStatusException CONFLICT retorna 409 com motivo`() {
        mockMvc.perform(get("/test/excecoes/response-status-409"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.mensagem").value("Ação negada: lockout"))
    }

    // ========== MethodArgumentNotValidException → 400 ==========

    @Test
    fun `MethodArgumentNotValidException retorna 400 com primeira mensagem de campo`() {
        mockMvc.perform(
            post("/test/excecoes/metodo-valid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome": ""}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensagem").value("O nome é obrigatório"))
    }

    @Test
    fun `MethodArgumentNotValidException sem field errors retorna 400 com mensagem generica`() {
        mockMvc.perform(
            post("/test/excecoes/metodo-valid-vazio")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensagem").isNotEmpty)
    }

    // ========== ConstraintViolationException → 400 ==========

    @Test
    fun `ConstraintViolationException retorna 400 com mensagem da violacao`() {
        mockMvc.perform(get("/test/excecoes/constraint-violation/   "))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensagem").isNotEmpty)
    }

    // ========== HttpMessageNotReadableException → 400 ==========

    @Test
    fun `HttpMessageNotReadableException retorna 400 com mensagem fixa`() {
        mockMvc.perform(
            post("/test/excecoes/not-readable")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido ou mal formatado."))
    }

    // ========== HttpRequestMethodNotSupportedException → 405 ==========

    @Test
    fun `HttpRequestMethodNotSupportedException retorna 405 com metodo e suportados`() {
        mockMvc.perform(delete("/test/excecoes/illegal-argument"))
            .andExpect(status().isMethodNotAllowed)
            .andExpect(jsonPath("$.status").value(405))
            .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("DELETE")))
    }

    // ========== HttpMediaTypeNotSupportedException → 415 ==========

    @Test
    fun `HttpMediaTypeNotSupportedException retorna 415 com tipo de midia`() {
        mockMvc.perform(
            post("/test/excecoes/metodo-valid")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text body")
        )
            .andExpect(status().isUnsupportedMediaType)
            .andExpect(jsonPath("$.status").value(415))
            .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("text/plain")))
    }

    // ========== MissingServletRequestParameterException → 400 ==========

    @Test
    fun `MissingServletRequestParameterException retorna 400 com nome do parametro`() {
        mockMvc.perform(get("/test/excecoes/missing-param"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("nome")))
    }

    // ========== DataIntegrityViolationException → 400 ==========

    @Test
    fun `DataIntegrityViolationException com rootCause retorna 400 com mensagem da causa raiz`() {
        mockMvc.perform(get("/test/excecoes/data-integrity"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensagem").value("duplicate key value violates unique constraint"))
    }

    @Test
    fun `DataIntegrityViolationException sem rootCause retorna 400 com mensagem da excecao`() {
        mockMvc.perform(get("/test/excecoes/data-integrity-no-cause"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensagem").value("violação genérica de integridade"))
    }

    // ========== Exception (fallback) → 500 ==========

    @Test
    fun `Exception generica retorna 500 com mensagem generica`() {
        mockMvc.perform(get("/test/excecoes/general"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.mensagem").value("Serviço indisponível no momento. Tente novamente mais tarde."))
    }

    // ========== Sanidade: dataHora sempre preenchido ==========

    @Test
    fun `dataHora esta preenchido em todas as respostas de erro`() {
        mockMvc.perform(get("/test/excecoes/illegal-argument"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.dataHora").isNotEmpty)
            .andExpect(jsonPath("$.dataHora").value(org.hamcrest.Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }
}
