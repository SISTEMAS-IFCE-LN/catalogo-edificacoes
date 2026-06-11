package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class UsuarioServiceTest {

    @Mock
    lateinit var repository: UsuarioRepository

    @InjectMocks
    lateinit var service: UsuarioService

    @Test
    fun `deve atualizar perfis adicionando ROLE_COLABORADOR automaticamente`() {
        val usuario = Usuario(id = 1, email = "user@ifce.edu.br", nome = "User")
        `when`(repository.findById(1L)).thenReturn(Optional.of(usuario))
        `when`(repository.save(usuario)).thenReturn(usuario)

        service.atualizarPerfis(1L, setOf(Perfil.ROLE_VALIDADOR))

        assertEquals(2, usuario.perfis.size)
        assert(usuario.perfis.contains(Perfil.ROLE_VALIDADOR))
        assert(usuario.perfis.contains(Perfil.ROLE_COLABORADOR))
    }

    @Test
    fun `deve lancar 404 quando usuario nao existe ao atualizar perfis`() {
        `when`(repository.findById(99L)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.atualizarPerfis(99L, setOf(Perfil.ROLE_COLABORADOR))
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun `deve lancar 409 ao tentar remover ROLE_ADMINISTRADOR do ultimo admin`() {
        val admin = Usuario(id = 1, email = "admin@ifce.edu.br", nome = "Admin").apply {
            perfis = mutableSetOf(Perfil.ROLE_ADMINISTRADOR, Perfil.ROLE_COLABORADOR)
        }
        `when`(repository.findById(1L)).thenReturn(Optional.of(admin))
        `when`(repository.countByAtivoTrueAndPerfisContains(Perfil.ROLE_ADMINISTRADOR)).thenReturn(1)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.atualizarPerfis(1L, setOf(Perfil.ROLE_COLABORADOR))
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun `deve permitir remover ROLE_ADMINISTRADOR quando ha mais de um admin`() {
        val admin = Usuario(id = 1, email = "admin@ifce.edu.br", nome = "Admin").apply {
            perfis = mutableSetOf(Perfil.ROLE_ADMINISTRADOR, Perfil.ROLE_COLABORADOR)
        }
        `when`(repository.findById(1L)).thenReturn(Optional.of(admin))
        `when`(repository.countByAtivoTrueAndPerfisContains(Perfil.ROLE_ADMINISTRADOR)).thenReturn(3)
        `when`(repository.save(admin)).thenReturn(admin)

        service.atualizarPerfis(1L, setOf(Perfil.ROLE_COLABORADOR))

        assert(!admin.perfis.contains(Perfil.ROLE_ADMINISTRADOR))
    }

    @Test
    fun `deve desativar usuario com sucesso`() {
        val usuario = Usuario(id = 1, email = "user@ifce.edu.br", nome = "User", ativo = true)
        `when`(repository.findById(1L)).thenReturn(Optional.of(usuario))
        `when`(repository.save(usuario)).thenReturn(usuario)

        service.desativarUsuario(1L)

        assertEquals(false, usuario.ativo)
    }

    @Test
    fun `deve lancar 409 ao desativar o ultimo administrador`() {
        val admin = Usuario(id = 1, email = "admin@ifce.edu.br", nome = "Admin").apply {
            perfis = mutableSetOf(Perfil.ROLE_ADMINISTRADOR, Perfil.ROLE_COLABORADOR)
            ativo = true
        }
        `when`(repository.findById(1L)).thenReturn(Optional.of(admin))
        `when`(repository.countByAtivoTrueAndPerfisContains(Perfil.ROLE_ADMINISTRADOR)).thenReturn(1)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.desativarUsuario(1L)
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }
}
