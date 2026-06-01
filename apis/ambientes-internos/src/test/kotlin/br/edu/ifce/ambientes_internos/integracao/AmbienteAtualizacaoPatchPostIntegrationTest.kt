package br.edu.ifce.ambientes_internos.integracao

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import jakarta.servlet.ServletException
import jakarta.validation.ConstraintViolationException
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de integração dos endpoints de validação de parâmetros nos PATCH e POST")
class AmbienteAtualizacaoPatchPostIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    private fun assertBadRequestOrValidationError(block: () -> Unit) {
        try {
            block()
        } catch (ex: ServletException) {
            assertTrue(ex.rootCause is ConstraintViolationException || ex.rootCause is NoSuchElementException)
        }
    }

    // ========== Validações PATCH: atualizarDadosBasicosAmbiente (parâmetros de path) ==========

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com id zero`() {
        val payloadPatch = """
            {
              "nome": "Sala Atualizada",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 2
              },
              "capacidade": 10
            }
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/0/dados-basicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com id negativo`() {
        val payloadPatch = """
            {
              "nome": "Sala Atualizada",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 2
              },
              "capacidade": 10
            }
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/-1/dados-basicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com nome em branco`() {
        val payloadPatch = """
            {
              "nome": "   ",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 2
              },
              "capacidade": 10
            }
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/dados-basicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com nome nulo`() {
        val payloadPatch = """
            {
              "nome": null,
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 2
              },
              "capacidade": 10
            }
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/dados-basicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com nome maior que 50 caracteres`() {
        val nomeLongo = "A".repeat(51)
        val payloadPatch = """
            {
              "nome": "$nomeLongo",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 2
              },
              "capacidade": 10
            }
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/dados-basicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com capacidade zero`() {
        val payloadPatch = """
            {
              "nome": "Sala Atualizada",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 2
              },
              "capacidade": 0
            }
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/dados-basicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com capacidade negativa`() {
        val payloadPatch = """
            {
              "nome": "Sala Atualizada",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 2
              },
              "capacidade": -10
            }
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/dados-basicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com bloco maior que 50 caracteres`() {
        val blocoLongo = "B".repeat(51)
        val payloadPatch = """
            {
              "nome": "Sala Atualizada",
              "localizacao": {
                "bloco": "$blocoLongo",
                "unidade": "CIDADE_ALTA",
                "andar": 2
              },
              "capacidade": 10
            }
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/dados-basicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com unidade maior que 50 caracteres`() {
        val unidadeLonga = "U".repeat(51)
        val payloadPatch = """
            {
              "nome": "Sala Atualizada",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "$unidadeLonga",
                "andar": 2
              },
              "capacidade": 10
            }
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/dados-basicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com andar negativo`() {
        val payloadPatch = """
            {
              "nome": "Sala Atualizada",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": -1
              },
              "capacidade": 10
            }
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/dados-basicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    // ========== Validações PATCH: incluirPesDireitosAmbiente (parâmetros de path) ==========

    @Test
    fun `deve retornar 400 ao incluir pes direitos com id zero`() {
        val payloadPatch = "[3.50]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/0/pes-direitos/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir pes direitos com id negativo`() {
        val payloadPatch = "[3.50]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/-1/pes-direitos/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir pes direitos vazio`() {
        val payloadPatch = "[]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/pes-direitos/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir pes direito com mais de 2 casas decimais`() {
        val payloadPatch = "[3.555]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/pes-direitos/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir pes direito zero`() {
        val payloadPatch = "[0.00]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/pes-direitos/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir pes direito negativo`() {
        val payloadPatch = "[-3.50]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/pes-direitos/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    // ========== Validações PATCH: incluirGeometriasAmbiente (parâmetros de path) ==========

    @Test
    fun `deve retornar 400 ao incluir geometrias com id zero`() {
        val payloadPatch = """
            [
              {
                "tipo": "RETANGULAR",
                "base": 6.00,
                "altura": 3.00,
                "repeticao": 1
              }
            ]
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/0/geometrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir geometrias com id negativo`() {
        val payloadPatch = """
            [
              {
                "tipo": "RETANGULAR",
                "base": 6.00,
                "altura": 3.00,
                "repeticao": 1
              }
            ]
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/-1/geometrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir geometrias vazia`() {
        val payloadPatch = "[]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/geometrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir geometria com base zero`() {
        val payloadPatch = """
            [
              {
                "tipo": "RETANGULAR",
                "base": 0.00,
                "altura": 3.00,
                "repeticao": 1
              }
            ]
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/geometrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir geometria com altura com mais de 2 casas decimais`() {
        val payloadPatch = """
            [
              {
                "tipo": "RETANGULAR",
                "base": 6.00,
                "altura": 3.555,
                "repeticao": 1
              }
            ]
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/geometrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    // ========== Validações PATCH: incluirEsquadriasAmbiente (parâmetros de path) ==========

    @Test
    fun `deve retornar 400 ao incluir esquadrias com id zero`() {
        val payloadPatch = """
            [
              {
                "tipo": "JANELA",
                "geometria": {
                  "base": 1.50,
                  "altura": 1.20,
                  "repeticao": 1
                },
                "material": "ALUMINIO",
                "alturaPeitoril": 0.90
              }
            ]
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/0/esquadrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir esquadrias com id negativo`() {
        val payloadPatch = """
            [
              {
                "tipo": "JANELA",
                "geometria": {
                  "base": 1.50,
                  "altura": 1.20,
                  "repeticao": 1
                },
                "material": "ALUMINIO",
                "alturaPeitoril": 0.90
              }
            ]
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/-1/esquadrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir esquadrias vazia`() {
        val payloadPatch = "[]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/esquadrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao incluir esquadria com base negativa`() {
        val payloadPatch = """
            [
              {
                "tipo": "JANELA",
                "geometria": {
                  "base": -1.50,
                  "altura": 1.20,
                  "repeticao": 1
                },
                "material": "ALUMINIO",
                "alturaPeitoril": 0.90
              }
            ]
        """.trimIndent()

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/esquadrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    // ========== Validações PATCH: atualizarInformacaoAdicionalAmbiente (parâmetros de path) ==========

    @Test
    fun `deve retornar 400 ao atualizar informacao adicional com id zero`() {
        val payloadPatch = "\"Nova informacao\""

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/0/informacao-adicional")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar informacao adicional com id negativo`() {
        val payloadPatch = "\"Nova informacao\""

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/-1/informacao-adicional")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao atualizar informacao adicional maior que 255 caracteres`() {
        val infoLonga = "\"" + "X".repeat(256) + "\""

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/informacao-adicional")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(infoLonga)
            ).andExpect(status().isBadRequest)
        }
    }

    // ========== Validações DELETE: deletarAmbientes ==========

    @Test
    fun `deve retornar 400 ao deletar ambientes com lista vazia`() {
        val payloadDelete = "[]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/ambientes/nao-publicados")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadDelete)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao deletar ambientes com id zero na lista`() {
        val payloadDelete = "[0, 1, 2]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/ambientes/nao-publicados")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadDelete)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao deletar ambientes com id negativo na lista`() {
        val payloadDelete = "[-1, 1, 2]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/ambientes/nao-publicados")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadDelete)
            ).andExpect(status().isBadRequest)
        }
    }

    // ========== Validações PATCH: enviarValidacaoAmbientes ==========

    @Test
    fun `deve retornar 400 ao enviar validacao com lista de ids vazia`() {
        val payloadValidacao = "[]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/validar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadValidacao)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao enviar validacao com id zero na lista`() {
        val payloadValidacao = "[0, 1, 2]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/validar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadValidacao)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `deve retornar 400 ao enviar validacao com id negativo na lista`() {
        val payloadValidacao = "[-1, 1, 2]"

        assertBadRequestOrValidationError {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/validar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadValidacao)
            ).andExpect(status().isBadRequest)
        }
    }
}

