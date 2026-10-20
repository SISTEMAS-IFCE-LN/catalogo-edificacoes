package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para UnidadeRepository")
class UnidadeRepositoryIntegrationTest {

    @Autowired
    lateinit var repository: UnidadeRepository

    @Autowired
    lateinit var repoCampus: CampusRepository

    private fun criarCampus(nome: String): Campus = repoCampus.saveAndFlush(Campus(nome))

    @Test
    fun `Deve buscar unidade por campus e nome normalizado`() {
        val campus = criarCampus("Limoeiro do Norte")
        repository.saveAndFlush(Unidade(campus, " cidade   alta "))

        val encontrada = assertNotNull(repository.findByCampusAndNome(campus, "CIDADE ALTA"))

        assertEquals("CIDADE ALTA", encontrada.nome)
        assertEquals(campus, encontrada.campus)
    }

    @Test
    fun `Nao deve encontrar unidade em outro campus`() {
        val limoeiro = criarCampus("Limoeiro do Norte")
        val outro = criarCampus("Outro Campus")
        repository.saveAndFlush(Unidade(limoeiro, "SEDE"))

        assertNull(repository.findByCampusAndNome(outro, "SEDE"))
    }

    @Test
    fun `Deve permitir o mesmo nome de unidade em campi diferentes`() {
        val limoeiro = criarCampus("Limoeiro do Norte")
        val outro = criarCampus("Outro Campus")

        val primeira = repository.saveAndFlush(Unidade(limoeiro, "SEDE"))
        val segunda = repository.saveAndFlush(Unidade(outro, "SEDE"))

        assertNotEquals(primeira.id, segunda.id)
        assertEquals(2L, repository.count())
    }

    @Test
    fun `Deve impedir duplicidade de nome de unidade no mesmo campus`() {
        val campus = criarCampus("Limoeiro do Norte")
        repository.saveAndFlush(Unidade(campus, "SEDE"))

        assertThrows<DataIntegrityViolationException> {
            repository.saveAndFlush(Unidade(campus, "SEDE"))
        }
    }

    @Test
    fun `Deve listar apenas as unidades do campus`() {
        val limoeiro = criarCampus("Limoeiro do Norte")
        val outro = criarCampus("Outro Campus")
        repository.saveAllAndFlush(
            listOf(
                Unidade(limoeiro, "SEDE"),
                Unidade(limoeiro, "CIDADE ALTA"),
                Unidade(outro, "UEPE")
            )
        )

        val doLimoeiro = repository.findByCampus(limoeiro).map { it.nome }

        assertEquals(2, doLimoeiro.size)
        assertEquals(setOf("SEDE", "CIDADE ALTA"), doLimoeiro.toSet())
    }

    @Test
    fun `Nao deve encontrar unidade de outro campus ao validar pelo id`() {
        val limoeiro = criarCampus("Limoeiro do Norte")
        val outro = criarCampus("Outro Campus")
        val unidade = repository.saveAndFlush(Unidade(limoeiro, "SEDE"))

        assertNull(repository.findByIdAndCampusId(unidade.id!!, outro.id!!))
        assertNotNull(repository.findByIdAndCampusId(unidade.id!!, limoeiro.id!!))
    }

}
