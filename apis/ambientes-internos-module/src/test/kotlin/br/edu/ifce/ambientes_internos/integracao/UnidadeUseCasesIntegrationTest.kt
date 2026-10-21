package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.application.interfaces.IUnidadeUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.UnidadeUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@DataJpaTest
@Import(UnidadeUseCases::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para os Casos de Uso de Unidade")
class UnidadeUseCasesIntegrationTest {

    @Autowired
    lateinit var unidadeUseCases: IUnidadeUseCases

    @Autowired
    lateinit var repoUnidade: UnidadeRepository

    @Autowired
    lateinit var repoCampus: CampusRepository

    private fun criarCampus(nome: String = "Limoeiro do Norte"): Campus =
        repoCampus.saveAndFlush(Campus(nome))

    @Test
    fun `Deve criar unidade sob o campus quando nao existe`() {
        val campus = criarCampus()

        val unidade = unidadeUseCases.resolverOuCriar(campus, " cidade   alta ")

        assertNotNull(unidade.id)
        assertEquals("CIDADE ALTA", unidade.nome)

        val persistida = repoUnidade.findById(unidade.id!!).orElseThrow()
        assertEquals("CIDADE ALTA", persistida.nome)
        assertEquals(campus.id, persistida.campus.id)
    }

    @Test
    fun `Deve reusar unidade existente com variacao de caixa e acento`() {
        val campus = criarCampus()

        val primeiro = unidadeUseCases.resolverOuCriar(campus, "Cidade Alta")
        val segundo = unidadeUseCases.resolverOuCriar(campus, "cídade álta")

        assertEquals(primeiro.id, segundo.id)
        assertEquals(1L, repoUnidade.count())
    }

    @Test
    fun `Deve criar unidades distintas com o mesmo nome em campi diferentes`() {
        val limoeiro = criarCampus()
        val outro = criarCampus("Outro Campus")

        val primeira = unidadeUseCases.resolverOuCriar(limoeiro, "SEDE")
        val segunda = unidadeUseCases.resolverOuCriar(outro, "sede")

        assertNotEquals(primeira.id, segunda.id)
        assertEquals(2L, repoUnidade.count())
    }

    @Test
    fun `Deve lancar excecao para nome em branco`() {
        val campus = criarCampus()

        val erro = assertThrows<IllegalArgumentException> {
            unidadeUseCases.resolverOuCriar(campus, "   ")
        }

        assertEquals("O nome da unidade é inválido.", erro.message)
        assertEquals(0L, repoUnidade.count())
    }

    @Test
    fun `Deve obter unidade validando o campus`() {
        val campus = criarCampus()
        val unidade = unidadeUseCases.resolverOuCriar(campus, "SEDE")

        val encontrada = unidadeUseCases.obterPorIdEValidarCampus(unidade.id!!, campus.id!!)

        assertEquals(unidade.id, encontrada.id)
    }

    @Test
    fun `Deve lancar excecao ao validar unidade de outro campus`() {
        val limoeiro = criarCampus()
        val outro = criarCampus("Outro Campus")
        val unidade = unidadeUseCases.resolverOuCriar(limoeiro, "SEDE")

        val erro = assertThrows<IllegalArgumentException> {
            unidadeUseCases.obterPorIdEValidarCampus(unidade.id!!, outro.id!!)
        }

        assertEquals("A unidade não pertence ao campus informado.", erro.message)
    }

    @Test
    fun `Deve lancar excecao ao validar unidade inexistente`() {
        val campus = criarCampus()

        val erro = assertThrows<IllegalArgumentException> {
            unidadeUseCases.obterPorIdEValidarCampus(999L, campus.id!!)
        }

        assertEquals("Unidade inválida: 999.", erro.message)
    }

    @Test
    fun `Deve lancar excecao ao validar campus inexistente`() {
        val campus = criarCampus()
        val unidade = unidadeUseCases.resolverOuCriar(campus, "SEDE")

        val erro = assertThrows<IllegalArgumentException> {
            unidadeUseCases.obterPorIdEValidarCampus(unidade.id!!, 999L)
        }

        assertEquals("Campus inválido: 999.", erro.message)
    }

    @Test
    fun `Deve lancar excecao ao listar unidades de campus inexistente`() {
        assertThrows<IllegalArgumentException> {
            unidadeUseCases.listarPorCampus(999L)
        }
    }

    @Test
    fun `Deve listar unidades do campus em ordem natural`() {
        val campus = criarCampus()
        listOf("UEPE", "Sede", "Cidade Alta").forEach { unidadeUseCases.resolverOuCriar(campus, it) }

        val nomes = unidadeUseCases.listarPorCampus(campus.id!!).map { it.nome }

        assertEquals(listOf("CIDADE ALTA", "SEDE", "UEPE"), nomes)
    }

}
