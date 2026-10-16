package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.TestApplication
import br.edu.ifce.ambientes_internos.TestSecurityConfig
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
// sem @WithMockUser de propósito: o endpoint é público
@DisplayName("Testes de integração do endpoint público de blocos")
class BlocoControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var repoBloco: BlocoRepository

    // o contexto é compartilhado entre as classes de teste; sem limpeza, os casos de
    // ordem natural e de lista vazia dependeriam da ordem de execução dos métodos
    @BeforeEach
    fun limparBlocos() {
        repoBloco.deleteAll()
    }

    @Test
    fun `deve listar blocos da unidade em ordem natural de nome`() {
        repoBloco.saveAllAndFlush(
            listOf(
                Bloco(Unidade.SEDE, "BLOCO 10"),
                Bloco(Unidade.SEDE, "BLOCO 2A"),
                Bloco(Unidade.SEDE, "BLOCO 2")
            )
        )

        mockMvc.perform(get("/api/blocos").param("unidade", "SEDE"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].id").isNumber)
            .andExpect(jsonPath("$[0].nome").value("BLOCO 2"))
            .andExpect(jsonPath("$[0].unidade").doesNotExist())
            .andExpect(jsonPath("$[1].nome").value("BLOCO 2A"))
            .andExpect(jsonPath("$[2].nome").value("BLOCO 10"))
    }

    @Test
    fun `deve retornar lista vazia quando a unidade nao possui blocos`() {
        mockMvc.perform(get("/api/blocos").param("unidade", "UEPE"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `deve aceitar nome do enum e rotulo da unidade sem diferenciar caixa`() {
        repoBloco.saveAndFlush(Bloco(Unidade.SEDE, "BLOCO 1"))

        listOf("SEDE", "Sede", "sede").forEach { valor ->
            mockMvc.perform(get("/api/blocos").param("unidade", valor))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("BLOCO 1"))
        }
    }

    @Test
    fun `deve retornar 400 ao informar unidade invalida`() {
        mockMvc.perform(get("/api/blocos").param("unidade", "XYZ"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.mensagem").value("Unidade inválida."))
    }

    @Test
    fun `deve retornar 400 quando o parametro unidade estiver ausente`() {
        mockMvc.perform(get("/api/blocos"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.mensagem").value(containsString("unidade")))
    }

    @Test
    fun `nao deve vazar blocos de outras unidades`() {
        repoBloco.saveAllAndFlush(
            listOf(
                Bloco(Unidade.SEDE, "BLOCO 1"),
                Bloco(Unidade.CIDADE_ALTA, "BLOCO 10"),
                Bloco(Unidade.UEPE, "ANEXO A")
            )
        )

        mockMvc.perform(get("/api/blocos").param("unidade", "SEDE"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nome").value("BLOCO 1"))
    }

}
