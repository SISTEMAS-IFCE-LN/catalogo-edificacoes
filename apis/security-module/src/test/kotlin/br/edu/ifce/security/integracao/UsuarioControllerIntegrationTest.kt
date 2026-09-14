package br.edu.ifce.security.integracao

import br.edu.ifce.security.TestApplication
import br.edu.ifce.security.TestSecurityConfig
import br.edu.ifce.security.controller.UsuarioController
import br.edu.ifce.security.model.application.service.JwtService
import br.edu.ifce.security.model.application.service.UsuarioService
import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class, UsuarioController::class, UsuarioService::class)
@DisplayName("Testes de integração do UsuarioController (/api/usuarios/me)")
class UsuarioControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    lateinit var jwtService: JwtService

    private lateinit var usuarioColaborador: Usuario
    private lateinit var usuarioAdministrador: Usuario

    @BeforeEach
    fun setup() {
        usuarioRepository.deleteAll()
        usuarioColaborador = usuarioRepository.save(
            Usuario(
                email = "colaborador@ifce.edu.br",
                nome = "Colaborador",
                perfis = mutableSetOf(Perfil.ROLE_COLABORADOR)
            )
        )
        usuarioAdministrador = usuarioRepository.save(
            Usuario(
                email = "admin@ifce.edu.br",
                nome = "Administrador",
                perfis = mutableSetOf(Perfil.ROLE_ADMINISTRADOR, Perfil.ROLE_COLABORADOR)
            )
        )
    }

    private fun tokenPara(usuarioId: Long, perfis: Set<Perfil>): String =
        jwtService.gerarAccessToken(usuarioId, perfis.map { it.name })

    @Nested
    @DisplayName("GET /api/usuarios/me")
    inner class MeTests {

        @Test
        fun `deve retornar 200 com usuario autenticado quando JWT valido de colaborador`() {
            val token = tokenPara(usuarioColaborador.id!!, usuarioColaborador.perfis)

            mockMvc.perform(
                get("/api/usuarios/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(usuarioColaborador.id))
                .andExpect(jsonPath("$.email").value("colaborador@ifce.edu.br"))
                .andExpect(jsonPath("$.nome").value("Colaborador"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.criadoEm").exists())
                .andExpect(jsonPath("$.perfis[?(@=='ROLE_COLABORADOR')]").exists())
        }

        @Test
        fun `deve retornar 200 para administrador no endpoint me`() {
            val token = tokenPara(usuarioAdministrador.id!!, usuarioAdministrador.perfis)

            mockMvc.perform(
                get("/api/usuarios/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.email").value("admin@ifce.edu.br"))
        }

        @Test
        fun `deve retornar 401 quando Authorization header ausente`() {
            mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `deve retornar 404 quando subject do JWT nao corresponde a usuario existente`() {
            val tokenInexistente = jwtService.gerarAccessToken(9999999L, listOf(Perfil.ROLE_COLABORADOR.name))

            mockMvc.perform(
                get("/api/usuarios/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $tokenInexistente")
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("GET /api/usuarios (matcher ROLE_ADMINISTRADOR)")
    inner class ListarUsuariosTests {

        @Test
        fun `deve retornar 403 quando JWT com role nao-admin acessa listar usuarios`() {
            val token = tokenPara(usuarioColaborador.id!!, usuarioColaborador.perfis)

            mockMvc.perform(
                get("/api/usuarios")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            )
                .andExpect(status().isForbidden)
        }

        @Test
        fun `deve retornar 200 quando JWT com role ADMINISTRADOR acessa listar usuarios`() {
            val token = tokenPara(usuarioAdministrador.id!!, usuarioAdministrador.perfis)

            mockMvc.perform(
                get("/api/usuarios")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.usuarios").isArray)
                .andExpect(jsonPath("$.dadosPaginacao").exists())
        }
    }
}