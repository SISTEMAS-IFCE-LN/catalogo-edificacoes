package br.edu.ifce.security.unitarios

import br.edu.ifce.security.model.application.service.UsuarioOAuth2Service
import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.oauth2.core.OAuth2AuthenticationException

@ExtendWith(MockitoExtension::class)
class UsuarioOAuth2ServiceTest {

    @Mock
    lateinit var repository: UsuarioRepository

    @InjectMocks
    lateinit var service: UsuarioOAuth2Service

    private fun usuario(
        email: String = "user@ifce.edu.br",
        nome: String = "Usuario",
        ativo: Boolean = true,
        perfis: MutableSet<Perfil> = mutableSetOf(Perfil.ROLE_COLABORADOR)
    ) = Usuario(email = email, nome = nome, ativo = ativo, perfis = perfis)

    @Test
    fun `deve provisionar usuario institucional novo com ROLE_COLABORADOR`() {
        `when`(repository.findByEmail("novo@ifce.edu.br")).thenReturn(null)

        val autoridades = service.sincronizar("novo@ifce.edu.br", "Novo Usuario")

        val captor = ArgumentCaptor.forClass(Usuario::class.java)
        verify(repository).save(captor.capture())
        val salvo = captor.value
        assertEquals("novo@ifce.edu.br", salvo.email)
        assertEquals("Novo Usuario", salvo.nome)
        assertTrue(salvo.ativo)
        assertEquals(setOf(Perfil.ROLE_COLABORADOR), salvo.perfis)
        assertEquals(listOf("ROLE_COLABORADOR"), autoridades.map { it.authority })
    }

    @Test
    fun `deve negar e nao salvar usuario externo nao cadastrado`() {
        `when`(repository.findByEmail("externo@gmail.com")).thenReturn(null)

        val ex = assertThrows(OAuth2AuthenticationException::class.java) {
            service.sincronizar("externo@gmail.com", "Externo")
        }

        assertEquals("unauthorized_domain", ex.error.errorCode)
        verify(repository, never()).save(any(Usuario::class.java))
    }

    @Test
    fun `deve atualizar nome quando usuario existente tem nome diferente`() {
        val existente = usuario(nome = "Nome Antigo")
        `when`(repository.findByEmail(existente.email)).thenReturn(existente)

        val autoridades = service.sincronizar(existente.email, "Nome Novo")

        assertEquals("Nome Novo", existente.nome)
        verify(repository).save(existente)
        assertEquals(listOf("ROLE_COLABORADOR"), autoridades.map { it.authority })
    }

    @Test
    fun `nao deve salvar quando usuario existente tem o mesmo nome`() {
        val existente = usuario(nome = "Mesmo Nome")
        `when`(repository.findByEmail(existente.email)).thenReturn(existente)

        service.sincronizar(existente.email, "Mesmo Nome")

        verify(repository, never()).save(any(Usuario::class.java))
    }

    @Test
    fun `deve negar usuario inativo`() {
        val inativo = usuario(ativo = false)
        `when`(repository.findByEmail(inativo.email)).thenReturn(inativo)

        val ex = assertThrows(OAuth2AuthenticationException::class.java) {
            service.sincronizar(inativo.email, inativo.nome)
        }

        assertEquals("user_inactive", ex.error.errorCode)
    }

    @Test
    fun `deve permitir usuario externo ja cadastrado`() {
        val externo = usuario(email = "externo@gmail.com", nome = "Externo")
        `when`(repository.findByEmail(externo.email)).thenReturn(externo)

        val autoridades = service.sincronizar(externo.email, externo.nome)

        assertEquals(listOf("ROLE_COLABORADOR"), autoridades.map { it.authority })
        verify(repository, never()).save(any(Usuario::class.java))
    }

    @Test
    fun `deve retornar todas as authorities do usuario`() {
        val comPerfis = usuario(
            perfis = mutableSetOf(Perfil.ROLE_COLABORADOR, Perfil.ROLE_VALIDADOR, Perfil.ROLE_ADMINISTRADOR)
        )
        `when`(repository.findByEmail(comPerfis.email)).thenReturn(comPerfis)

        val autoridades = service.sincronizar(comPerfis.email, comPerfis.nome)

        assertEquals(
            setOf("ROLE_COLABORADOR", "ROLE_VALIDADOR", "ROLE_ADMINISTRADOR"),
            autoridades.map { it.authority }.toSet()
        )
    }
}
