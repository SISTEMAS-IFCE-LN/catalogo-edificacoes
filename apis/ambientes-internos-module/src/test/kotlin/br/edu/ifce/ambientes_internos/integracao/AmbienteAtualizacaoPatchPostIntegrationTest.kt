package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.TestApplication
import br.edu.ifce.ambientes_internos.TestSecurityConfig
import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.enums.TipoGeometria
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaReq
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import jakarta.servlet.ServletException
import jakarta.validation.ConstraintViolationException
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertTrue

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
@WithMockUser(authorities = ["ROLE_GESTOR_SISTEMA"])
@DisplayName("Testes de integração dos endpoints de validação de parâmetros nos PATCH e POST")
class AmbienteAtualizacaoPatchPostIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var ambientesNPUseCases: IAmbienteNaoPublicadoUseCases

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
                "campus": {"nome": "LIMOEIRO DO NORTE"},
                "unidade": {"nome": "CIDADE ALTA"},
                "bloco": {"nome": "BLOCO 10"},
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
                "campus": {"nome": "LIMOEIRO DO NORTE"},
                "unidade": {"nome": "CIDADE ALTA"},
                "bloco": {"nome": "BLOCO 10"},
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
                "campus": {"nome": "LIMOEIRO DO NORTE"},
                "unidade": {"nome": "CIDADE ALTA"},
                "bloco": {"nome": "BLOCO 10"},
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
                "campus": {"nome": "LIMOEIRO DO NORTE"},
                "unidade": {"nome": "CIDADE ALTA"},
                "bloco": {"nome": "BLOCO 10"},
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
                "campus": {"nome": "LIMOEIRO DO NORTE"},
                "unidade": {"nome": "CIDADE ALTA"},
                "bloco": {"nome": "BLOCO 10"},
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
                "campus": {"nome": "LIMOEIRO DO NORTE"},
                "unidade": {"nome": "CIDADE ALTA"},
                "bloco": {"nome": "BLOCO 10"},
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
                "campus": {"nome": "LIMOEIRO DO NORTE"},
                "unidade": {"nome": "CIDADE ALTA"},
                "bloco": {"nome": "BLOCO 10"},
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
                "campus": {"nome": "LIMOEIRO DO NORTE"},
                "unidade": {"nome": "CIDADE ALTA"},
                "bloco": {"nome": "$blocoLongo"},
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
                "campus": {"nome": "LIMOEIRO DO NORTE"},
                "unidade": {"nome": "$unidadeLonga"},
                "bloco": {"nome": "BLOCO 10"},
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
                "campus": {"nome": "LIMOEIRO DO NORTE"},
                "unidade": {"nome": "CIDADE ALTA"},
                "bloco": {"nome": "BLOCO 10"},
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
    fun `deve retornar 404 ao incluir pes direito em ambiente inexistente`() {
        val payloadPatch = "[3.555]"

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/pes-direitos/incluir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadPatch)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `deve retornar 404 ao incluir pes direito zero em ambiente inexistente`() {
        val payloadPatch = "[0.00]"

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/pes-direitos/incluir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadPatch)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `deve retornar 404 ao incluir pes direito negativo em ambiente inexistente`() {
        val payloadPatch = "[-3.50]"

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/999/pes-direitos/incluir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadPatch)
        ).andExpect(status().isNotFound)
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

    // ========== Caso do UC16 (POST /{id}): alterar tipo e dados com troca de bloco ==========

    // Transacional de propósito: o contexto Spring/H2 é compartilhado entre as classes de
    // teste e os dados criados aqui não podem vazar (o BlocoControllerIntegrationTest, por
    // exemplo, limpa todos os blocos com repoBloco.deleteAll()).
    @Transactional
    @Test
    fun `deve alterar tipo e dados de ambiente nao publicado com troca de bloco`() {
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(
            AmbienteReq(
                tipo = TipoAmbiente.SALA_AULA,
                nome = "Sala UC16 ${UUID.randomUUID().toString().take(8)}",
                localizacao = localizacaoReqPorNome(bloco = "BLOCO 10", andar = 1),
                capacidade = 30,
                geometrias = setOf(
                    GeometriaAmbienteReq(
                        tipo = TipoGeometria.RETANGULAR,
                        base = BigDecimal("6.0"),
                        altura = BigDecimal("3.0")
                    )
                ),
                pesDireitos = setOf(BigDecimal("3.0")),
                esquadrias = setOf(
                    EsquadriaReq(
                        tipo = TipoEsquadria.PORTA,
                        geometria = GeometriaEsquadriaReq(base = BigDecimal("0.9"), altura = BigDecimal("2.1")),
                        material = MaterialEsquadria.MADEIRA_FICHA,
                        informacaoAdicional = "Porta de abrir"
                    )
                ),
                informacaoAdicional = "Ambiente para alteração de tipo"
            )
        )

        val localizacaoAtual = ambienteSalvo.localizacao

        val payloadPost = """
            {
              "tipo": "LABORATORIO_INFORMATICA",
              "nome": "Sala UC16 Alterada ${UUID.randomUUID().toString().take(8)}",
              "localizacao": {
                "campus": {"id": ${localizacaoAtual.campus.id}},
                "unidade": {"id": ${localizacaoAtual.unidade.id}},
                "bloco": {"nome": "BLOCO 11"},
                "andar": 2
              },
              "capacidade": 25,
              "geometrias": [
                {
                  "tipo": "RETANGULAR",
                  "base": 8.00,
                  "altura": 4.00,
                  "repeticao": 1
                }
              ],
              "pesDireitos": [3.50],
              "esquadrias": [
                {
                  "tipo": "PORTA",
                  "geometria": {
                    "base": 0.80,
                    "altura": 2.10,
                    "repeticao": 1
                  },
                  "material": "ALUMINIO",
                  "informacaoAdicional": "Porta de abrir"
                }
              ],
              "informacaoAdicional": "Ambiente alterado para laboratório"
            }
        """.trimIndent()

        // Quando o tipo e dados do ambiente forem alterados com troca de bloco/unidade
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/ambientes/nao-publicados/${ambienteSalvo.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadPost)
        ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.localizacao.campus.nome").value("LIMOEIRO DO NORTE"))
            .andExpect(jsonPath("$.localizacao.unidade.nome").value("CIDADE ALTA"))
            .andExpect(jsonPath("$.localizacao.bloco.nome").value("BLOCO 11"))
            .andExpect(jsonPath("$.localizacao.andar").value(2))
    }
}

