package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.TestApplication
import br.edu.ifce.ambientes_internos.TestSecurityConfig
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
// sem @WithMockUser de propósito: o endpoint é público
@DisplayName("Testes de integração do endpoint público de unidades")
class UnidadeControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var repoCampus: CampusRepository

    @Autowired
    lateinit var repoUnidade: UnidadeRepository

    /**
     * O contexto H2 é compartilhado entre as classes de teste. Cada caso usa um campus
     * próprio para que listas e contagens não dependam de dados deixados por outros testes.
     */
    private fun campusUnico(prefixo: String = "CAMPUS"): Campus =
        repoCampus.saveAndFlush(
            Campus("$prefixo ${UUID.randomUUID().toString().take(8).uppercase()}")
        )

    @Test
    fun `deve listar unidades do campus em ordem natural`() {
        val campus = campusUnico()
        repoUnidade.saveAllAndFlush(
            listOf(
                Unidade(campus, "UEPE"),
                Unidade(campus, "Sede"),
                Unidade(campus, "Cidade Alta")
            )
        )

        mockMvc.perform(get("/api/unidades").param("campusId", campus.id!!.toString()))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].id").isNumber)
            .andExpect(jsonPath("$[0].nome").value("CIDADE ALTA"))
            .andExpect(jsonPath("$[0].campus").doesNotExist())
            .andExpect(jsonPath("$[1].nome").value("SEDE"))
            .andExpect(jsonPath("$[2].nome").value("UEPE"))
    }

    @Test
    fun `nao deve vazar unidades de outros campi`() {
        val alvo = campusUnico("ALVO")
        val outro = campusUnico("OUTRO")
        repoUnidade.saveAllAndFlush(
            listOf(
                Unidade(alvo, "SEDE"),
                Unidade(outro, "SEDE"),
                Unidade(outro, "UEPE")
            )
        )

        mockMvc.perform(get("/api/unidades").param("campusId", alvo.id!!.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nome").value("SEDE"))
    }

    @Test
    fun `deve retornar lista vazia quando o campus nao possui unidades`() {
        val campus = campusUnico()

        mockMvc.perform(get("/api/unidades").param("campusId", campus.id!!.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `deve retornar 400 quando o parametro campusId estiver ausente`() {
        mockMvc.perform(get("/api/unidades"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.mensagem").value(containsString("campusId")))
    }

    @Test
    fun `deve retornar 400 ao informar campus inexistente`() {
        mockMvc.perform(get("/api/unidades").param("campusId", Long.MAX_VALUE.toString()))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.mensagem").value(containsString("Campus inválido")))
    }

    @Test
    fun `deve retornar 400 ao informar campusId invalido`() {
        mockMvc.perform(get("/api/unidades").param("campusId", "0"))
            .andExpect(status().isBadRequest)
    }

}
