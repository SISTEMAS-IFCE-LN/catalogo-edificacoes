package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.application.interfaces.IBlocoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.BlocoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
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

    @Test
    fun `Deve criar bloco quando nao existe`() {
        val bloco = blocoUseCases.resolverOuCriar(Unidade.SEDE, " ginásio ")

        assertNotNull(bloco.id)
        assertEquals("GINASIO", bloco.nome)
        assertEquals(Unidade.SEDE, bloco.unidade)

        val persistido = repoBloco.findById(bloco.id!!).orElseThrow()
        assertEquals("GINASIO", persistido.nome)
    }

    @Test
    fun `Deve reusar bloco existente com variacao do nome`() {
        val primeiro = blocoUseCases.resolverOuCriar(Unidade.SEDE, "Ginásio")
        val segundo = blocoUseCases.resolverOuCriar(Unidade.SEDE, "ginasio")

        assertEquals(primeiro.id, segundo.id)
        assertEquals(1L, repoBloco.count())
    }

    @Test
    fun `Deve separar blocos por unidade com o mesmo nome`() {
        val naSede = blocoUseCases.resolverOuCriar(Unidade.SEDE, "BLOCO 1")
        val naCidadeAlta = blocoUseCases.resolverOuCriar(Unidade.CIDADE_ALTA, "bloco 1")

        assertNotNull(naSede.id)
        assertNotNull(naCidadeAlta.id)
        assertNotEquals(naSede.id, naCidadeAlta.id)
        assertEquals(2L, repoBloco.count())
    }

    @Test
    fun `Deve ser idempotente em chamadas repetidas`() {
        val primeiro = blocoUseCases.resolverOuCriar(Unidade.SEDE, "BLOCO 1")
        val segundo = blocoUseCases.resolverOuCriar(Unidade.SEDE, "BLOCO 1")
        val terceiro = blocoUseCases.resolverOuCriar(Unidade.SEDE, " BLOCO  01 ")

        assertEquals(primeiro.id, segundo.id)
        assertEquals(primeiro.id, terceiro.id)
        assertEquals(1L, repoBloco.count())
    }

    @Test
    fun `Deve lancar excecao para nome em branco`() {
        assertThrows<IllegalArgumentException> {
            blocoUseCases.resolverOuCriar(Unidade.SEDE, "   ")
        }

        assertEquals(0L, repoBloco.count())
    }

}
