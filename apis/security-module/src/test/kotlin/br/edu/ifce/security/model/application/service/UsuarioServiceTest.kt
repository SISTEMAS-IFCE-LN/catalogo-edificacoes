package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*

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

    @Test
    fun `deve ativar usuario com sucesso`() {
        val usuario = Usuario(id = 1, email = "user@ifce.edu.br", nome = "User", ativo = false)
        `when`(repository.findById(1L)).thenReturn(Optional.of(usuario))
        `when`(repository.save(usuario)).thenReturn(usuario)

        service.ativarUsuario(1L)

        assertEquals(true, usuario.ativo)
    }

    @Test
    fun `deve lancar 404 quando usuario nao existe ao ativar`() {
        `when`(repository.findById(99L)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.ativarUsuario(99L)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun `deve listar usuarios com paginacao`() {
        val u1 = Usuario(id = 1, email = "user1@ifce.edu.br", nome = "User One").apply {
            perfis = mutableSetOf(Perfil.ROLE_COLABORADOR)
        }
        val u2 = Usuario(id = 2, email = "user2@ifce.edu.br", nome = "User Two").apply {
            perfis = mutableSetOf(Perfil.ROLE_VALIDADOR, Perfil.ROLE_COLABORADOR)
        }
        val pageable = PageRequest.of(0, 20)
        val page: Page<Usuario> = PageImpl(listOf(u1, u2), pageable, 2)
        `when`(repository.findAll(pageable)).thenReturn(page)

        val result = service.listarUsuarios(pageable)

        assertEquals(2, result.usuarios.size)
        assertEquals("user1@ifce.edu.br", result.usuarios[0].email)
        assertEquals("User Two", result.usuarios[1].nome)
        assertEquals(2L, result.dadosPaginacao.totalElements)
        assertEquals(1, result.dadosPaginacao.totalPages)
        assertEquals(0, result.dadosPaginacao.currentPage)
        assertEquals(20, result.dadosPaginacao.pageSize)
        assertFalse(result.dadosPaginacao.hasNext)
        assertFalse(result.dadosPaginacao.hasPrevious)
    }

    @Test
    fun `deve limitar page size em 100 ao listar usuarios`() {
        val pageable = PageRequest.of(0, 500)
        val page: Page<Usuario> = PageImpl(emptyList(), PageRequest.of(0, 100), 0)
        `when`(repository.findAll(any(Pageable::class.java))).thenReturn(page)

        service.listarUsuarios(pageable)

        val captor = ArgumentCaptor.forClass(Pageable::class.java)
        org.mockito.Mockito.verify(repository).findAll(captor.capture())
        assertEquals(100, captor.value.pageSize)
        assertEquals(0, captor.value.pageNumber)
    }

    @Test
    fun `deve obter usuario por email com sucesso`() {
        val usuario = Usuario(id = 1, email = "user@ifce.edu.br", nome = "User", ativo = true).apply {
            perfis = mutableSetOf(Perfil.ROLE_COLABORADOR, Perfil.ROLE_VALIDADOR)
        }
        `when`(repository.findByEmail("user@ifce.edu.br")).thenReturn(usuario)

        val result = service.obterUsuarioPorEmail("user@ifce.edu.br")

        assertEquals(1L, result.id)
        assertEquals("user@ifce.edu.br", result.email)
        assertEquals("User", result.nome)
        assertTrue(result.ativo)
        assertNotNull(result.criadoEm)
        assertEquals(2, result.perfis.size)
        assertTrue(result.perfis.contains(Perfil.ROLE_COLABORADOR))
        assertTrue(result.perfis.contains(Perfil.ROLE_VALIDADOR))
    }

    @Test
    fun `deve lancar 404 quando email nao existe`() {
        `when`(repository.findByEmail("inexistente@ifce.edu.br")).thenReturn(null)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.obterUsuarioPorEmail("inexistente@ifce.edu.br")
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun `deve listar usuarios por nome retornando resultados`() {
        val u1 = Usuario(id = 1, email = "joao@ifce.edu.br", nome = "João Silva").apply {
            perfis = mutableSetOf(Perfil.ROLE_COLABORADOR)
        }
        val pageable = PageRequest.of(0, 20)
        val page: Page<Usuario> = PageImpl(listOf(u1), pageable, 1)
        `when`(repository.findByNomeContainingIgnoreCase("joao", pageable)).thenReturn(page)

        val result = service.listarUsuariosPorNome("joao", pageable)

        assertEquals(1, result.usuarios.size)
        assertEquals("João Silva", result.usuarios[0].nome)
        assertEquals(1L, result.dadosPaginacao.totalElements)
        assertTrue(result.dadosPaginacao.hasNext == false)
    }

    @Test
    fun `deve retornar lista vazia quando nenhum usuario corresponde ao nome`() {
        val pageable = PageRequest.of(0, 20)
        val page: Page<Usuario> = PageImpl(emptyList(), pageable, 0)
        `when`(repository.findByNomeContainingIgnoreCase("xyz", pageable)).thenReturn(page)

        val result = service.listarUsuariosPorNome("xyz", pageable)

        assertTrue(result.usuarios.isEmpty())
        assertEquals(0L, result.dadosPaginacao.totalElements)
        assertEquals(0, result.dadosPaginacao.totalPages)
    }
}
