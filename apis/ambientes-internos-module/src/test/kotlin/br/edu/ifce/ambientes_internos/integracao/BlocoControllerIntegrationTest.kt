package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.TestApplication
import br.edu.ifce.ambientes_internos.TestSecurityConfig
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
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
@DisplayName("Testes de integração do endpoint público de blocos")
class BlocoControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var repoBloco: BlocoRepository

    @Autowired
    lateinit var repoCampus: CampusRepository

    @Autowired
    lateinit var repoUnidade: UnidadeRepository

    /**
     * O contexto H2 é compartilhado entre as classes de teste. Cada caso usa uma unidade
     * própria para que listas e contagens não dependam de dados deixados por outros testes.
     */
    private fun unidadeUnica(prefixo: String = "SEDE"): Unidade =
        criarUnidade(
            repoCampus,
            repoUnidade,
            unidadeNome = "$prefixo-${UUID.randomUUID().toString().take(8).uppercase()}"
        )

    @Test
    fun `deve listar blocos da unidade em ordem natural de nome`() {
        val sede = unidadeUnica()
        repoBloco.saveAllAndFlush(
            listOf(
                Bloco(sede, "BLOCO 10"),
                Bloco(sede, "BLOCO 2A"),
                Bloco(sede, "BLOCO 2")
            )
        )

        mockMvc.perform(get("/api/blocos").param("unidadeId", sede.id.toString()))
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
        val uepe = unidadeUnica("UEPE")

        mockMvc.perform(get("/api/blocos").param("unidadeId", uepe.id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `deve retornar 400 ao informar unidade inexistente`() {
        mockMvc.perform(get("/api/blocos").param("unidadeId", Long.MAX_VALUE.toString()))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.mensagem").value(containsString("Unidade inválida")))
    }

    @Test
    fun `deve retornar 400 ao informar unidadeId invalido`() {
        mockMvc.perform(get("/api/blocos").param("unidadeId", "0"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `deve retornar 400 quando o parametro unidadeId estiver ausente`() {
        mockMvc.perform(get("/api/blocos"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.mensagem").value(containsString("unidadeId")))
    }

    @Test
    fun `nao deve vazar blocos de outras unidades`() {
        val sede = unidadeUnica()
        val cidadeAlta = unidadeUnica("CIDADE ALTA")
        val uepe = unidadeUnica("UEPE")
        repoBloco.saveAllAndFlush(
            listOf(
                Bloco(sede, "BLOCO 1"),
                Bloco(cidadeAlta, "BLOCO 10"),
                Bloco(uepe, "ANEXO A")
            )
        )

        mockMvc.perform(get("/api/blocos").param("unidadeId", sede.id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nome").value("BLOCO 1"))
    }

}
