package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.application.usecases.BlocoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import org.junit.jupiter.api.DisplayName
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@DisplayName("Testes unitários do fallback de corrida dos Casos de Uso de Bloco")
class BlocoUseCasesTest {

    private val repoBloco: BlocoRepository = mock(BlocoRepository::class.java)
    private val repoUnidade: UnidadeRepository = mock(UnidadeRepository::class.java)
    private val blocoUseCases = BlocoUseCases(repoBloco, repoUnidade)

    @Test
    fun `Deve reler o bloco existente apos violacao de unicidade no save`() {
        val campus = Campus("LIMOEIRO DO NORTE").apply { id = 1L }
        val unidade = Unidade(campus, "SEDE").apply { id = 2L }
        val existente = Bloco(unidade, "BLOCO 1").apply { id = 9L }

        // primeira leitura não encontra (corrida); após a violação, a releitura encontra
        Mockito.`when`(repoBloco.findByUnidadeAndNome(unidade, "BLOCO 1"))
            .thenReturn(null)
            .thenReturn(existente)
        Mockito.`when`(repoBloco.saveAndFlush(any(Bloco::class.java)))
            .thenThrow(DataIntegrityViolationException("uk_bloco_unidade_nome"))

        val resultado = blocoUseCases.resolverOuCriar(unidade, " bloco 01 ")

        assertEquals(9L, resultado.id)
        verify(repoBloco, times(2)).findByUnidadeAndNome(unidade, "BLOCO 1")
    }

    @ParameterizedTest(name = "Nome ''{0}'' normaliza para vazio e deve ser rejeitado")
    @ValueSource(strings = ["***", "---", "   "])
    fun `Deve rejeitar nome que normaliza para vazio`(nomeDigitado: String) {
        val campus = Campus("LIMOEIRO DO NORTE").apply { id = 1L }
        val unidade = Unidade(campus, "SEDE").apply { id = 2L }

        assertFailsWith<IllegalArgumentException> {
            blocoUseCases.resolverOuCriar(unidade, nomeDigitado)
        }
        // A validação ocorre antes de qualquer acesso ao repositório
        verifyNoInteractions(repoBloco)
    }
}
