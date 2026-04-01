package br.edu.ifce.ambientes_internos.integracao

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import jakarta.servlet.ServletException
import jakarta.validation.ConstraintViolationException
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de integração dos endpoints base de ambientes")
class AmbienteControllerBaseIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    private fun assertBadRequestOrValidationError(block: () -> Unit) {
        try {
            block()
        } catch (ex: ServletException) {
            assertTrue(ex.rootCause is ConstraintViolationException)
        }
    }

    // ========== Validações para listarAmbientesPorTipo ==========

    @Test
    fun `deve retornar 400 ao listar ambientes por tipo com tipo em branco`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/tipo")
                .param("tipo", "   ")
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao listar ambientes por tipo com tipo ausente`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/tipo")
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao listar ambientes por tipo com tipo maior que 50 caracteres`() {
        val tipoLongo = "A".repeat(51)
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/tipo")
                .param("tipo", tipoLongo)
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    // ========== Validações para listarAmbientesPorNome ==========

    @Test
    fun `deve retornar 400 ao listar ambientes por nome com nome em branco`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/nome")
                .param("nome", "   ")
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao listar ambientes por nome com nome ausente`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/nome")
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao listar ambientes por nome com nome maior que 50 caracteres`() {
        val nomeLongo = "A".repeat(51)
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/nome")
                .param("nome", nomeLongo)
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    // ========== Validações para listarAmbientesPorLocalizacao ==========

    @Test
    fun `deve retornar 400 ao listar ambientes por localizacao com bloco maior que 50 caracteres`() {
        val blocoLongo = "B".repeat(51)
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/localizacao")
                .param("bloco", blocoLongo)
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao listar ambientes por localizacao com unidade maior que 50 caracteres`() {
        val unidadeLonga = "U".repeat(51)
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/localizacao")
                .param("unidade", unidadeLonga)
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao listar ambientes por localizacao com andar negativo`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/localizacao")
                .param("andar", "-1")
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    // ========== Validações para obterAmbientePorId ==========

    @Test
    fun `deve retornar 400 ao obter ambiente com id igual a zero`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/0")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao obter ambiente com id negativo`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/nao-publicados/-1")
            ).andExpect(status().isBadRequest)
        }
    }

}




