package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para BlocoRepository")
class BlocoRepositoryIntegrationTest {

    @Autowired
    lateinit var repository: BlocoRepository

    @Test
    fun `Deve normalizar o nome ao persistir bloco`() {
        val salvo = repository.saveAndFlush(Bloco(Unidade.SEDE, "bloco 1"))

        assertEquals("BLOCO 1", salvo.nome)

        val persistido = repository.findById(salvo.id!!).orElseThrow()
        assertEquals("BLOCO 1", persistido.nome)
    }

    @Test
    fun `Deve buscar bloco por unidade e nome com escopo por unidade`() {
        repository.saveAndFlush(Bloco(Unidade.SEDE, "BLOCO 1"))

        val naSede = repository.findByUnidadeAndNome(Unidade.SEDE, "BLOCO 1")
        val naCidadeAlta = repository.findByUnidadeAndNome(Unidade.CIDADE_ALTA, "BLOCO 1")

        assertTrue(naSede.isPresent)
        assertEquals("BLOCO 1", naSede.get().nome)
        assertEquals(Unidade.SEDE, naSede.get().unidade)
        assertTrue(naCidadeAlta.isEmpty)
    }

    @Test
    fun `Deve impedir duplicidade de nome na mesma unidade`() {
        repository.saveAndFlush(Bloco(Unidade.SEDE, "BLOCO 1"))

        assertThrows<DataIntegrityViolationException> {
            repository.saveAndFlush(Bloco(Unidade.SEDE, "BLOCO 1"))
        }
    }

    @Test
    fun `Deve listar apenas os blocos da unidade`() {
        repository.saveAllAndFlush(
            listOf(
                Bloco(Unidade.SEDE, "BLOCO 1"),
                Bloco(Unidade.SEDE, "BLOCO 2"),
                Bloco(Unidade.CIDADE_ALTA, "BLOCO 10")
            )
        )

        val daSede = repository.findByUnidade(Unidade.SEDE).map { it.nome }

        assertEquals(2, daSede.size)
        assertEquals(setOf("BLOCO 1", "BLOCO 2"), daSede.toSet())
    }

    @Test
    fun `Deve retornar blocos com o mesmo nome de todas as unidades`() {
        repository.saveAllAndFlush(
            listOf(
                Bloco(Unidade.SEDE, "BLOCO 1"),
                Bloco(Unidade.CIDADE_ALTA, "BLOCO 1"),
                Bloco(Unidade.SEDE, "BLOCO 2")
            )
        )

        val encontrados = repository.findAllByNome("BLOCO 1")

        assertEquals(2, encontrados.size)
        assertEquals(setOf(Unidade.SEDE, Unidade.CIDADE_ALTA), encontrados.map { it.unidade }.toSet())
    }

}
