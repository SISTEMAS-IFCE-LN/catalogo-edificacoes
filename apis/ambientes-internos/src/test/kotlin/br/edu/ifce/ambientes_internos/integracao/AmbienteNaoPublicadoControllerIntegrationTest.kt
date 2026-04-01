package br.edu.ifce.ambientes_internos.integracao

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de integração do controller de ambientes não publicados")
class AmbienteNaoPublicadoControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

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
}
