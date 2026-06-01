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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import jakarta.servlet.ServletException
import jakarta.validation.ConstraintViolationException
import kotlin.test.assertTrue
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de integração do controller de ambientes não publicados")
class AmbienteNaoPublicadoControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    private fun assertBadRequestOrValidationError(block: () -> Unit) {
        try {
            block()
        } catch (ex: ServletException) {
            assertTrue(ex.rootCause is ConstraintViolationException || ex.rootCause is NoSuchElementException)
        }
    }

    // ========== Validações de precisão decimal ==========

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com base da geometria com mais de duas casas decimais`() {
        val payload = payloadCadastroAmbiente(baseGeometria = "6.123")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com pe direito com mais de duas casas decimais`() {
        val payload = payloadCadastroAmbiente(peDireito = "3.333")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com altura do peitoril com mais de duas casas decimais`() {
        val payload = payloadCadastroAmbiente(alturaPeitoril = "0.901")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    // ========== Validações de campo obrigatório (NotBlank) ==========

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com nome em branco`() {
        val payload = """
            {
              "tipo": "SALA_AULA",
              "nome": "   ",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 1
              },
              "capacidade": 30,
              "geometrias": [
                {
                  "tipo": "RETANGULAR",
                  "base": 6.00,
                  "altura": 3.00,
                  "repeticao": 1
                }
              ],
              "pesDireitos": [3.00],
              "esquadrias": [
                {
                  "tipo": "JANELA",
                  "geometria": {
                    "base": 1.50,
                    "altura": 1.20,
                    "repeticao": 1
                  },
                  "material": "ALUMINIO",
                  "alturaPeitoril": 0.90,
                  "informacaoAdicional": "Janela"
                }
              ],
              "informacaoAdicional": "Teste de validacao"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com nome nulo`() {
        val payload = """
            {
              "tipo": "SALA_AULA",
              "nome": null,
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 1
              },
              "capacidade": 30,
              "geometrias": [
                {
                  "tipo": "RETANGULAR",
                  "base": 6.00,
                  "altura": 3.00,
                  "repeticao": 1
                }
              ],
              "pesDireitos": [3.00],
              "esquadrias": [
                {
                  "tipo": "JANELA",
                  "geometria": {
                    "base": 1.50,
                    "altura": 1.20,
                    "repeticao": 1
                  },
                  "material": "ALUMINIO",
                  "alturaPeitoril": 0.90,
                  "informacaoAdicional": "Janela"
                }
              ],
              "informacaoAdicional": "Teste de validacao"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    // ========== Validações de tamanho de string (Size) ==========

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com nome maior que 50 caracteres`() {
        val nomeLongo = "A".repeat(51)
        val payload = """
            {
              "tipo": "SALA_AULA",
              "nome": "$nomeLongo",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 1
              },
              "capacidade": 30,
              "geometrias": [
                {
                  "tipo": "RETANGULAR",
                  "base": 6.00,
                  "altura": 3.00,
                  "repeticao": 1
                }
              ],
              "pesDireitos": [3.00],
              "esquadrias": [
                {
                  "tipo": "JANELA",
                  "geometria": {
                    "base": 1.50,
                    "altura": 1.20,
                    "repeticao": 1
                  },
                  "material": "ALUMINIO",
                  "alturaPeitoril": 0.90,
                  "informacaoAdicional": "Janela"
                }
              ],
              "informacaoAdicional": "Teste de validacao"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com informacao adicional maior que 255 caracteres`() {
        val infoLonga = "X".repeat(256)
        val payload = payloadCadastroAmbiente().replace("\"informacaoAdicional\": \"Teste de validacao\"", "\"informacaoAdicional\": \"$infoLonga\"")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    // ========== Validações de positividade (Positive) ==========

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com capacidade zero`() {
        val payload = payloadCadastroAmbiente().replace("\"capacidade\": 30", "\"capacidade\": 0")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com capacidade negativa`() {
        val payload = payloadCadastroAmbiente().replace("\"capacidade\": 30", "\"capacidade\": -5")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com base da geometria zero`() {
        val payload = payloadCadastroAmbiente(baseGeometria = "0.00")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com altura da geometria negativa`() {
        val payload = payloadCadastroAmbiente().replace("\"altura\": 3.00", "\"altura\": -2.50")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com pe direito zero`() {
        val payload = payloadCadastroAmbiente(peDireito = "0.00")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com pe direito negativo`() {
        val payload = payloadCadastroAmbiente(peDireito = "-3.50")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    // ========== Validações de intervalo (Min) ==========

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com repeticao de geometria menor que 1`() {
        val payload = payloadCadastroAmbiente().replace("\"repeticao\": 1", "\"repeticao\": 0")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com altura do peitoril negativa`() {
        val payload = payloadCadastroAmbiente(alturaPeitoril = "-0.50")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    // ========== Validações de coleção não vazia (NotEmpty) ==========

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com lista de geometrias vazia`() {
        val payload = payloadCadastroAmbienteComColecoes(geometrias = "[]")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com lista de pes direitos vazia`() {
        val payload = payloadCadastroAmbiente().replace("\"pesDireitos\": [3.00]", "\"pesDireitos\": []")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com lista de esquadrias vazia`() {
        val payload = payloadCadastroAmbienteComColecoes(esquadrias = "[]")

        mockMvc.perform(
            post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    // ========== Validações PATCH: atualizarDadosBasicosAmbiente ==========

    @Test
    fun `deve retornar 400 ao atualizar dados basicos com capacidade zero no endpoint correto`() {
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

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/dados-basicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadPatch)
        ).andExpect(status().isBadRequest)
    }

    // ========== Validações PATCH: incluirPesDireitosAmbiente ==========

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

    // ========== Validações PATCH: incluirGeometriasAmbiente ==========

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

    // ========== Validações PATCH: incluirEsquadriasAmbiente ==========

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

    // ========== Validações PATCH: atualizarInformacaoAdicionalAmbiente ==========

    @Test
    fun `deve retornar 400 ao atualizar informacao adicional maior que 255 caracteres`() {
        val infoLonga = "\"" + "Y".repeat(256) + "\""

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

    private fun payloadCadastroAmbiente(
        baseGeometria: String = "6.00",
        peDireito: String = "3.00",
        alturaPeitoril: String = "0.90"
    ): String {
        val nomeUnico = "SalaInt-${UUID.randomUUID().toString().take(8)}"
        return """
            {
              "tipo": "SALA_AULA",
              "nome": "$nomeUnico",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 1
              },
              "capacidade": 30,
              "geometrias": [
                {
                  "tipo": "RETANGULAR",
                  "base": $baseGeometria,
                  "altura": 3.00,
                  "repeticao": 1
                }
              ],
              "pesDireitos": [$peDireito],
              "esquadrias": [
                {
                  "tipo": "JANELA",
                  "geometria": {
                    "base": 1.50,
                    "altura": 1.20,
                    "repeticao": 1
                  },
                  "material": "ALUMINIO",
                  "alturaPeitoril": $alturaPeitoril,
                  "informacaoAdicional": "Janela"
                },
                {
                  "tipo": "PORTA",
                  "geometria": {
                    "base": 0.90,
                    "altura": 2.10,
                    "repeticao": 1
                  },
                  "material": "MADEIRA_FICHA",
                  "informacaoAdicional": "Porta"
                }
              ],
              "informacaoAdicional": "Teste de validacao"
            }
        """.trimIndent()
    }

    private fun payloadCadastroAmbienteComColecoes(
        geometrias: String = """
            [
              {
                "tipo": "RETANGULAR",
                "base": 6.00,
                "altura": 3.00,
                "repeticao": 1
              }
            ]
        """.trimIndent(),
        pesDireitos: String = "[3.00]",
        esquadrias: String = """
            [
              {
                "tipo": "JANELA",
                "geometria": {
                  "base": 1.50,
                  "altura": 1.20,
                  "repeticao": 1
                },
                "material": "ALUMINIO",
                "alturaPeitoril": 0.90,
                "informacaoAdicional": "Janela"
              }
            ]
        """.trimIndent()
    ): String {
        val nomeUnico = "SalaInt-${UUID.randomUUID().toString().take(8)}"
        return """
            {
              "tipo": "SALA_AULA",
              "nome": "$nomeUnico",
              "localizacao": {
                "bloco": "BLOCO_10",
                "unidade": "CIDADE_ALTA",
                "andar": 1
              },
              "capacidade": 30,
              "geometrias": $geometrias,
              "pesDireitos": $pesDireitos,
              "esquadrias": $esquadrias,
              "informacaoAdicional": "Teste de validacao"
            }
        """.trimIndent()
    }
}
