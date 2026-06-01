package br.edu.ifce.ambientes_internos.integracao

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import jakarta.servlet.ServletException
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de integração para validações de esquadrias em ambientes")
class AmbienteEsquadriaValidacaoIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    // ========== Validações de esquadrias no cadastro ==========

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com esquadria com altura negativa`() {
        val payload = payloadCadastroAmbienteComEsquadria(alturaEsquadria = "-1.20")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com esquadria com base negativa`() {
        val payload = payloadCadastroAmbienteComEsquadria(baseEsquadria = "-1.50")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com esquadria com base com mais de 2 casas decimais`() {
        val payload = payloadCadastroAmbienteComEsquadria(baseEsquadria = "1.555")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com esquadria com altura com mais de 2 casas decimais`() {
        val payload = payloadCadastroAmbienteComEsquadria(alturaEsquadria = "1.234")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com esquadria com repeticao igual a zero`() {
        val payload = payloadCadastroAmbienteComEsquadria(repeticaoEsquadria = "0")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com esquadria com repeticao negativa`() {
        val payload = payloadCadastroAmbienteComEsquadria(repeticaoEsquadria = "-2")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com esquadria com informacao adicional maior que 255 caracteres`() {
        val infoLonga = "X".repeat(256)
        val payload = payloadCadastroAmbiente().replace(
            "\"informacaoAdicional\": \"Janela\"",
            "\"informacaoAdicional\": \"$infoLonga\""
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com altura peitoril com mais de 2 casas decimais`() {
        val payload = payloadCadastroAmbiente(alturaPeitoril = "0.999")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 ao cadastrar ambiente com altura peitoril negativa`() {
        val payload = payloadCadastroAmbiente(alturaPeitoril = "-0.50")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/ambientes/nao-publicados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isBadRequest)
    }

    // ========== Validações ao incluir esquadrias ==========

    @Test
    fun `deve retornar 400 ao incluir esquadria com base zero`() {
        val payloadPatch = """
            [
              {
                "tipo": "JANELA",
                "geometria": {
                  "base": 0.00,
                  "altura": 1.20,
                  "repeticao": 1
                },
                "material": "ALUMINIO",
                "alturaPeitoril": 0.90
              }
            ]
        """.trimIndent()

        assertThrows<ServletException> {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/esquadrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andReturn()
        }
    }

    @Test
    fun `deve retornar 400 ao incluir esquadria com altura zero`() {
        val payloadPatch = """
            [
              {
                "tipo": "JANELA",
                "geometria": {
                  "base": 1.50,
                  "altura": 0.00,
                  "repeticao": 1
                },
                "material": "ALUMINIO",
                "alturaPeitoril": 0.90
              }
            ]
        """.trimIndent()

        assertThrows<ServletException> {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/esquadrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andReturn()
        }
    }

    @Test
    fun `deve retornar 400 ao incluir esquadria com altura peitoril maior que 2 casas decimais`() {
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
                "alturaPeitoril": 0.999
              }
            ]
        """.trimIndent()

        assertThrows<ServletException> {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/esquadrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andReturn()
        }
    }

    @Test
    fun `deve retornar 400 ao incluir esquadria com altura peitoril negativa`() {
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
                "alturaPeitoril": -0.50
              }
            ]
        """.trimIndent()

        assertThrows<ServletException> {
            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/ambientes/nao-publicados/1/esquadrias/incluir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadPatch)
            ).andReturn()
        }
    }

    private fun payloadCadastroAmbiente(
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

    private fun payloadCadastroAmbienteComEsquadria(
        baseEsquadria: String = "1.50",
        alturaEsquadria: String = "1.20",
        repeticaoEsquadria: String = "1"
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
                    "base": $baseEsquadria,
                    "altura": $alturaEsquadria,
                    "repeticao": $repeticaoEsquadria
                  },
                  "material": "ALUMINIO",
                  "alturaPeitoril": 0.90,
                  "informacaoAdicional": "Janela"
                }
              ],
              "informacaoAdicional": "Teste de validacao"
            }
        """.trimIndent()
    }
}


