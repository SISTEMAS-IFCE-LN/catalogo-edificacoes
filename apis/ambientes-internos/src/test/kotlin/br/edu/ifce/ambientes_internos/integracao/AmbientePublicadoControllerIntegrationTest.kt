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
@DisplayName("Testes de integração dos endpoints de ambientes publicados")
class AmbientePublicadoControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    private fun assertBadRequestOrValidationError(block: () -> Unit) {
        try {
            block()
        } catch (ex: ServletException) {
            assertTrue(ex.rootCause is ConstraintViolationException)
        }
    }

    // ========== Validações para endpoints do controlador publicado (herdados do BaseController) ==========

    @Test
    fun `deve retornar 400 ao listar ambientes publicados por tipo com tipo em branco`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/publicados/tipo")
                .param("tipo", "   ")
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao listar ambientes publicados por nome com nome em branco`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/publicados/nome")
                .param("nome", "   ")
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao listar ambientes publicados por localizacao com andar negativo`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/publicados/localizacao")
                .param("andar", "-5")
                .param("page", "0")
                .param("size", "20")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao obter ambiente publicado com id zero`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/publicados/0")
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao obter ambiente publicado com id negativo`() {
        assertBadRequestOrValidationError {
            mockMvc.perform(
            get("/api/ambientes/publicados/-15")
            ).andExpect(status().isBadRequest)
        }
    }

}




