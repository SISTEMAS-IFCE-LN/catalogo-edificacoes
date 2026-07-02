package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.TestApplication
import br.edu.ifce.ambientes_internos.TestSecurityConfig
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
@DisplayName("Testes de integração de segurança das rotas de ambientes")
class AmbienteSegurancaRotasIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `deve permitir acesso aos ambientes publicados com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/publicados")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve permitir acesso aos ambientes publicados por tipo com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/publicados/tipo")
                .param("tipo", "SALA_AULA")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve permitir acesso aos ambientes publicados por nome com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/publicados/nome")
                .param("nome", "Sala")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve permitir acesso aos ambientes publicados por localizacao com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/publicados/localizacao")
                .param("bloco", "BLOCO_10")
                .param("unidade", "CIDADE_ALTA")
                .param("andar", "1")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve negar acesso aos ambientes nao publicados com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/nao-publicados")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `deve negar acesso aos ambientes nao publicados por tipo com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/nao-publicados/tipo")
                .param("tipo", "SALA_AULA")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `deve negar acesso aos ambientes nao publicados por nome com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/nao-publicados/nome")
                .param("nome", "Sala")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `deve negar acesso aos ambientes nao publicados por localizacao com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/nao-publicados/localizacao")
                .param("bloco", "BLOCO_10")
                .param("unidade", "CIDADE_ALTA")
                .param("andar", "1")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `deve permitir acesso aos ambientes nao publicados com jwt de gestor`() {
        mockMvc.perform(
            get("/api/ambientes/nao-publicados")
                .with(jwtComRoles("ROLE_GESTOR_SISTEMA"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve permitir acesso aos ambientes nao publicados por tipo com jwt de gestor`() {
        mockMvc.perform(
            get("/api/ambientes/nao-publicados/tipo")
                .param("tipo", "SALA_AULA")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_GESTOR_SISTEMA"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve permitir acesso aos ambientes nao publicados por nome com jwt de gestor`() {
        mockMvc.perform(
            get("/api/ambientes/nao-publicados/nome")
                .param("nome", "Sala")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_GESTOR_SISTEMA"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve permitir acesso aos ambientes nao publicados por localizacao com jwt de gestor`() {
        mockMvc.perform(
            get("/api/ambientes/nao-publicados/localizacao")
                .param("bloco", "BLOCO_10")
                .param("unidade", "CIDADE_ALTA")
                .param("andar", "1")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_GESTOR_SISTEMA"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve negar acesso aos ambientes em validacao com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/validacao")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `deve negar acesso aos ambientes em validacao por tipo com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/validacao/tipo")
                .param("tipo", "SALA_AULA")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `deve negar acesso aos ambientes em validacao por nome com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/validacao/nome")
                .param("nome", "Sala")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `deve negar acesso aos ambientes em validacao por localizacao com jwt de colaborador`() {
        mockMvc.perform(
            get("/api/ambientes/validacao/localizacao")
                .param("bloco", "BLOCO_10")
                .param("unidade", "CIDADE_ALTA")
                .param("andar", "1")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_COLABORADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `deve permitir acesso aos ambientes em validacao com jwt de validador`() {
        mockMvc.perform(
            get("/api/ambientes/validacao")
                .with(jwtComRoles("ROLE_VALIDADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve permitir acesso aos ambientes em validacao por tipo com jwt de validador`() {
        mockMvc.perform(
            get("/api/ambientes/validacao/tipo")
                .param("tipo", "SALA_AULA")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_VALIDADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve permitir acesso aos ambientes em validacao por nome com jwt de validador`() {
        mockMvc.perform(
            get("/api/ambientes/validacao/nome")
                .param("nome", "Sala")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_VALIDADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    @Test
    fun `deve permitir acesso aos ambientes em validacao por localizacao com jwt de validador`() {
        mockMvc.perform(
            get("/api/ambientes/validacao/localizacao")
                .param("bloco", "BLOCO_10")
                .param("unidade", "CIDADE_ALTA")
                .param("andar", "1")
                .param("page", "0")
                .param("size", "20")
                .with(jwtComRoles("ROLE_VALIDADOR"))
                .header(HttpHeaders.ACCEPT, "application/json")
        ).andExpect(status().isOk)
    }

    private fun jwtComRoles(vararg roles: String) = jwt()
        .jwt { jwt ->
            jwt.claim("roles", roles.toList())
            jwt.subject("teste")
        }
        .authorities(*roles.map { SimpleGrantedAuthority(it) }.toTypedArray())
}

