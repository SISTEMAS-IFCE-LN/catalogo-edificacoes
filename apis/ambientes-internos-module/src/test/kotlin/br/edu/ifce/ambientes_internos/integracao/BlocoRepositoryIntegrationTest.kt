package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para BlocoRepository")
class BlocoRepositoryIntegrationTest {

    @Autowired
    lateinit var repository: BlocoRepository

    @Autowired
    lateinit var repoCampus: CampusRepository

    @Autowired
    lateinit var repoUnidade: UnidadeRepository

    private lateinit var sede: Unidade
    private lateinit var cidadeAlta: Unidade

    @BeforeEach
    fun setUp() {
        sede = criarUnidade(repoCampus, repoUnidade, unidadeNome = "SEDE")
        cidadeAlta = criarUnidade(repoCampus, repoUnidade, unidadeNome = "CIDADE ALTA")
    }

    @Test
    fun `Deve normalizar o nome ao persistir bloco`() {
        val salvo = repository.saveAndFlush(Bloco(sede, "bloco 1"))

        assertEquals("BLOCO 1", salvo.nome)

        val persistido = repository.findById(salvo.id!!).orElseThrow()
        assertEquals("BLOCO 1", persistido.nome)
    }

    @Test
    fun `Deve buscar bloco por unidade e nome com escopo por unidade`() {
        repository.saveAndFlush(Bloco(sede, "BLOCO 1"))

        val naSede = assertNotNull(repository.findByUnidadeAndNome(sede, "BLOCO 1"))
        val naCidadeAlta = repository.findByUnidadeAndNome(cidadeAlta, "BLOCO 1")

        assertEquals("BLOCO 1", naSede.nome)
        assertEquals(sede, naSede.unidade)
        assertNull(naCidadeAlta)
    }

    @Test
    fun `Deve impedir duplicidade de nome na mesma unidade`() {
        repository.saveAndFlush(Bloco(sede, "BLOCO 1"))

        assertThrows<DataIntegrityViolationException> {
            repository.saveAndFlush(Bloco(sede, "BLOCO 1"))
        }
    }

    @Test
    fun `Deve permitir o mesmo nome de bloco em unidades diferentes`() {
        repository.saveAndFlush(Bloco(sede, "BLOCO 1"))
        repository.saveAndFlush(Bloco(cidadeAlta, "BLOCO 1"))

        assertEquals(2L, repository.count())
        assertNotNull(repository.findByUnidadeAndNome(sede, "BLOCO 1"))
        assertNotNull(repository.findByUnidadeAndNome(cidadeAlta, "BLOCO 1"))
    }

    @Test
    fun `Deve listar apenas os blocos da unidade`() {
        repository.saveAllAndFlush(
            listOf(
                Bloco(sede, "BLOCO 1"),
                Bloco(sede, "BLOCO 2"),
                Bloco(cidadeAlta, "BLOCO 10")
            )
        )

        val daSede = repository.findByUnidade(sede).map { it.nome }

        assertEquals(2, daSede.size)
        assertEquals(setOf("BLOCO 1", "BLOCO 2"), daSede.toSet())
    }

    @Test
    fun `Deve validar o vinculo do bloco com a unidade pelo id`() {
        val bloco = repository.saveAndFlush(Bloco(sede, "BLOCO 1"))

        assertNotNull(repository.findByIdAndUnidadeId(bloco.id!!, sede.id!!))
        assertNull(repository.findByIdAndUnidadeId(bloco.id!!, cidadeAlta.id!!))
    }

}
