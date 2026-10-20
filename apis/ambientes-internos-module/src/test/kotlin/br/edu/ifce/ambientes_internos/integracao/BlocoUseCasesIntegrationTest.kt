package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.application.interfaces.IBlocoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.BlocoUseCases
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
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@DataJpaTest
@Import(BlocoUseCases::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para os Casos de Uso de Bloco")
class BlocoUseCasesIntegrationTest {

    @Autowired
    lateinit var blocoUseCases: IBlocoUseCases

    @Autowired
    lateinit var repoBloco: BlocoRepository

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
    fun `Deve criar bloco quando nao existe`() {
        val bloco = blocoUseCases.resolverOuCriar(sede, " ginásio ")

        assertNotNull(bloco.id)
        assertEquals("GINASIO", bloco.nome)
        assertEquals(sede, bloco.unidade)

        val persistido = repoBloco.findById(bloco.id!!).orElseThrow()
        assertEquals("GINASIO", persistido.nome)
    }

    @Test
    fun `Deve reusar bloco existente com variacao do nome`() {
        val primeiro = blocoUseCases.resolverOuCriar(sede, "Ginásio")
        val segundo = blocoUseCases.resolverOuCriar(sede, "ginasio")

        assertEquals(primeiro.id, segundo.id)
        assertEquals(1L, repoBloco.count())
    }

    @Test
    fun `Deve separar blocos por unidade com o mesmo nome`() {
        val naSede = blocoUseCases.resolverOuCriar(sede, "BLOCO 1")
        val naCidadeAlta = blocoUseCases.resolverOuCriar(cidadeAlta, "bloco 1")

        assertNotNull(naSede.id)
        assertNotNull(naCidadeAlta.id)
        assertNotEquals(naSede.id, naCidadeAlta.id)
        assertEquals(2L, repoBloco.count())
    }

    @Test
    fun `Deve ser idempotente em chamadas repetidas`() {
        val primeiro = blocoUseCases.resolverOuCriar(sede, "BLOCO 1")
        val segundo = blocoUseCases.resolverOuCriar(sede, "BLOCO 1")
        val terceiro = blocoUseCases.resolverOuCriar(sede, " BLOCO  01 ")

        assertEquals(primeiro.id, segundo.id)
        assertEquals(primeiro.id, terceiro.id)
        assertEquals(1L, repoBloco.count())
    }

    @Test
    fun `Deve lancar excecao para nome em branco`() {
        assertThrows<IllegalArgumentException> {
            blocoUseCases.resolverOuCriar(sede, "   ")
        }

        assertEquals(0L, repoBloco.count())
    }

    @Test
    fun `Deve listar blocos da unidade em ordem natural`() {
        listOf("CASA", "BLOCO 10", "BLOCO 2A", "BLOCO 2").forEach {
            blocoUseCases.resolverOuCriar(sede, it)
        }

        val nomes = blocoUseCases.listarPorUnidade(sede.id!!).map { it.nome }

        assertEquals(listOf("BLOCO 2", "BLOCO 2A", "BLOCO 10", "CASA"), nomes)
    }

    @Test
    fun `Deve lançar excecao ao listar blocos de unidade inexistente`() {
        assertThrows<IllegalArgumentException> {
            blocoUseCases.listarPorUnidade(Long.MAX_VALUE)
        }
    }

    @Test
    fun `Deve obter bloco validando a unidade`() {
        val bloco = blocoUseCases.resolverOuCriar(sede, "BLOCO 1")

        val encontrado = blocoUseCases.obterPorIdEValidarUnidade(bloco.id!!, sede.id!!)

        assertEquals(bloco.id, encontrado.id)
    }

    @Test
    fun `Deve lancar excecao ao validar bloco de outra unidade`() {
        val bloco = blocoUseCases.resolverOuCriar(sede, "BLOCO 1")

        val erro = assertThrows<IllegalArgumentException> {
            blocoUseCases.obterPorIdEValidarUnidade(bloco.id!!, cidadeAlta.id!!)
        }

        assertEquals("O bloco não pertence à unidade informada.", erro.message)
    }

}
