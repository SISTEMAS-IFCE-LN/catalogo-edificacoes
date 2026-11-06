package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.TestApplication
import br.edu.ifce.ambientes_internos.TestSecurityConfig
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * O contexto H2 dos demais testes é compartilhado entre as classes. Para provar a lista
 * vazia sem depender da ordem de execução, esta classe usa um banco dedicado e ordena
 * os métodos: primeiro os cenários sem dados, depois o cenário que cria campi.
 */
@SpringBootTest(
    classes = [TestApplication::class],
    properties = [
        "spring.datasource.url=jdbc:h2:mem:ambientes_campus_controller;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
    ]
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
// sem @WithMockUser de propósito: o endpoint é público
@DisplayName("Testes de integração do endpoint público de campus")
class CampusControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var repoCampus: CampusRepository

    @Test
    @Order(1)
    fun `deve responder sem Authorization`() {
        mockMvc.perform(get("/api/campus"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    }

    @Test
    @Order(2)
    fun `deve retornar lista vazia quando nao houver campi`() {
        mockMvc.perform(get("/api/campus"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @Order(3)
    fun `deve listar campi em ordem natural sem campos extras`() {
        repoCampus.saveAllAndFlush(listOf(Campus("UEPE"), Campus("Cidade Alta")))

        mockMvc.perform(get("/api/campus"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").isNumber)
            .andExpect(jsonPath("$[0].nome").value("CIDADE ALTA"))
            .andExpect(jsonPath("$[0].campus").doesNotExist())
            .andExpect(jsonPath("$[1].id").isNumber)
            .andExpect(jsonPath("$[1].nome").value("UEPE"))
            .andExpect(jsonPath("$[1].campus").doesNotExist())
    }

}
