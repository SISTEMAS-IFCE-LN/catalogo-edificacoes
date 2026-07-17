package br.edu.ifce.security.integracao

import br.edu.ifce.security.TestApplication
import br.edu.ifce.security.TestSecurityConfig
import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.RefreshToken
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.RefreshTokenRepository
import br.edu.ifce.security.model.repository.UsuarioRepository
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
@DisplayName("Testes de integração do AuthController")
class AuthControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    lateinit var refreshTokenRepository: RefreshTokenRepository

    private lateinit var usuario: Usuario
    private lateinit var refreshTokenValido: String

    @BeforeEach
    fun setup() {
        refreshTokenRepository.deleteAll()
        usuarioRepository.deleteAll()
        usuario = usuarioRepository.save(
            Usuario(
                email = "teste@ifce.edu.br",
                nome = "Teste Usuário",
                perfis = mutableSetOf(Perfil.ROLE_COLABORADOR)
            )
        )
        refreshTokenValido = criarRefreshToken(usuario)
    }

    private fun criarRefreshToken(
        usuario: Usuario,
        expiraEm: LocalDateTime = LocalDateTime.now().plusHours(12)
    ): String {
        refreshTokenRepository.findByUsuarioAndRevogadoFalse(usuario)?.let {
            it.revogado = true
            refreshTokenRepository.save(it)
        }
        val token = RefreshToken(
            token = java.util.UUID.randomUUID().toString(),
            usuario = usuario,
            expiraEm = expiraEm
        )
        return refreshTokenRepository.save(token).token
    }

    @Nested
    @DisplayName("GET /auth/csrf-token")
    inner class CsrfTokenTests {

        @Test
        fun `deve retornar 200 com token CSRF e cookie XSRF-TOKEN`() {
            mockMvc.perform(get("/auth/csrf-token"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").isNotEmpty)
                .andExpect(cookie().exists("XSRF-TOKEN"))
        }

        @Test
        fun `deve retornar token CSRF diferente a cada requisicao`() {
            val result1 = mockMvc.perform(get("/auth/csrf-token"))
                .andExpect(status().isOk)
                .andReturn()
            val token1 = result1.response.getCookie("XSRF-TOKEN")?.value

            val result2 = mockMvc.perform(get("/auth/csrf-token"))
                .andExpect(status().isOk)
                .andReturn()
            val token2 = result2.response.getCookie("XSRF-TOKEN")?.value

            assert(token1 != token2) { "Tokens CSRF devem ser diferentes" }
        }
    }

    @Nested
    @DisplayName("POST /auth/refresh")
    inner class RefreshTests {

        @Test
        fun `deve retornar 200 com novo access token quando cookie e CSRF validos`() {
            mockMvc.perform(
                post("/auth/refresh")
                    .cookie(Cookie("refreshToken", refreshTokenValido))
                    .with(csrf())
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").isNotEmpty)
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
        }

        @Test
        fun `deve retornar 401 quando cookie refreshToken ausente`() {
            mockMvc.perform(
                post("/auth/refresh")
                    .with(csrf())
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `deve retornar 401 quando cookie refreshToken invalido`() {
            mockMvc.perform(
                post("/auth/refresh")
                    .cookie(Cookie("refreshToken", "token-inexistente"))
                    .with(csrf())
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `deve retornar 401 quando cookie refreshToken expirado`() {
            val tokenExpirado = criarRefreshToken(usuario, LocalDateTime.now().minusHours(1))

            mockMvc.perform(
                post("/auth/refresh")
                    .cookie(Cookie("refreshToken", tokenExpirado))
                    .with(csrf())
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `deve retornar 401 quando cookie refreshToken revogado`() {
            refreshTokenRepository.findByToken(refreshTokenValido)?.let {
                it.revogado = true
                refreshTokenRepository.save(it)
            }

            mockMvc.perform(
                post("/auth/refresh")
                    .cookie(Cookie("refreshToken", refreshTokenValido))
                    .with(csrf())
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `deve retornar 403 quando CSRF token ausente`() {
            mockMvc.perform(
                post("/auth/refresh")
                    .cookie(Cookie("refreshToken", refreshTokenValido))
            )
                .andExpect(status().isForbidden)
        }

        @Test
        fun `deve retornar 403 quando CSRF token invalido`() {
            mockMvc.perform(
                post("/auth/refresh")
                    .cookie(Cookie("refreshToken", refreshTokenValido))
                    .header("X-XSRF-TOKEN", "csrf-invalido")
            )
                .andExpect(status().isForbidden)
        }

        @Test
        fun `deve rotacionar refresh token quando tempo restante menor que access token expiration`() {
            val tokenPertoDeExpirar = criarRefreshToken(
                usuario,
                LocalDateTime.now().plusSeconds(600)
            )

            val result = mockMvc.perform(
                post("/auth/refresh")
                    .cookie(Cookie("refreshToken", tokenPertoDeExpirar))
                    .with(csrf())
            )
                .andExpect(status().isOk)
                .andExpect(cookie().exists("refreshToken"))
                .andReturn()

            val novoCookie = result.response.getCookie("refreshToken")
            assert(novoCookie?.value != tokenPertoDeExpirar) {
                "Refresh token deve ser rotacionado"
            }
        }

        @Test
        fun `nao deve rotacionar refresh token quando tempo restante maior que access token expiration`() {
            val result = mockMvc.perform(
                post("/auth/refresh")
                    .cookie(Cookie("refreshToken", refreshTokenValido))
                    .with(csrf())
            )
                .andExpect(status().isOk)
                .andReturn()

            val cookieRefresh = result.response.getCookie("refreshToken")
            assert(cookieRefresh == null) {
                "Refresh token não deve ser rotacionado quando tempo restante é suficiente"
            }
        }
    }

    @Nested
    @DisplayName("POST /auth/logout")
    inner class LogoutTests {

        @Test
        fun `deve revogar refresh token e retornar 204`() {
            mockMvc.perform(
                post("/auth/logout")
                    .cookie(Cookie("refreshToken", refreshTokenValido))
                    .with(csrf())
            )
                .andExpect(status().isNoContent)
                .andExpect(cookie().maxAge("refreshToken", 0))

            val tokenNoBanco = refreshTokenRepository.findByToken(refreshTokenValido)
            assert(tokenNoBanco?.revogado == true) {
                "Refresh token deve estar revogado após logout"
            }
        }

        @Test
        fun `deve retornar 204 quando cookie ausente (idempotente)`() {
            mockMvc.perform(
                post("/auth/logout")
                    .with(csrf())
            )
                .andExpect(status().isNoContent)
                .andExpect(cookie().maxAge("refreshToken", 0))
        }

        @Test
        fun `deve retornar 403 quando CSRF token ausente`() {
            mockMvc.perform(
                post("/auth/logout")
                    .cookie(Cookie("refreshToken", refreshTokenValido))
            )
                .andExpect(status().isForbidden)
        }

        @Test
        fun `deve retornar 403 quando CSRF token invalido`() {
            mockMvc.perform(
                post("/auth/logout")
                    .cookie(Cookie("refreshToken", refreshTokenValido))
                    .header("X-XSRF-TOKEN", "csrf-invalido")
            )
                .andExpect(status().isForbidden)
        }
    }
}
