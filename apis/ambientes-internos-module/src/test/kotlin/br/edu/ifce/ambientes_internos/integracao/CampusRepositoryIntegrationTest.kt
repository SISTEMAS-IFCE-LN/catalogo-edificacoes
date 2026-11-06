package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import jakarta.persistence.EntityManager
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
@DisplayName("Testes de integração para CampusRepository")
class CampusRepositoryIntegrationTest {

    @Autowired
    lateinit var repository: CampusRepository

    @Autowired
    lateinit var repoUnidade: UnidadeRepository

    @Autowired
    lateinit var entityManager: EntityManager

    @Test
    fun `Deve buscar campus pelo nome normalizado`() {
        repository.saveAndFlush(Campus("  limoeiro do norte "))

        val encontrado = assertNotNull(repository.findByNome("LIMOEIRO DO NORTE"))

        assertEquals("LIMOEIRO DO NORTE", encontrado.nome)
    }

    @Test
    fun `Deve retornar nulo quando o campus nao existe`() {
        assertNull(repository.findByNome("CAMPUS INEXISTENTE"))
    }

    @Test
    fun `Deve impedir duplicidade de nome de campus`() {
        repository.saveAndFlush(Campus("Limoeiro do Norte"))

        assertThrows<DataIntegrityViolationException> {
            repository.saveAndFlush(Campus("LIMOEIRO DO NORTE"))
        }
    }

    @Test
    fun `Deve remover campus sem unidades`() {
        val campus = repository.saveAndFlush(Campus("Campus Temporario"))

        repository.deleteIfOrphan(campus.id!!)
        entityManager.flush()
        entityManager.clear()

        assertNull(repository.findById(campus.id!!).orElse(null))
    }

    @Test
    fun `Nao deve remover campus com unidade`() {
        val campus = repository.saveAndFlush(Campus("Campus Temporario"))
        repoUnidade.saveAndFlush(Unidade(campus, "SEDE"))

        repository.deleteIfOrphan(campus.id!!)

        assertNotNull(repository.findById(campus.id!!).orElse(null))
    }

}
