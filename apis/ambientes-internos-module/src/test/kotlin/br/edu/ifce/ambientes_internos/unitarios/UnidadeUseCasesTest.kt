package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.application.usecases.UnidadeUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
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

@DisplayName("Testes unitários do fallback de corrida dos Casos de Uso de Unidade")
class UnidadeUseCasesTest {

    private val repoUnidade: UnidadeRepository = mock(UnidadeRepository::class.java)
    private val repoCampus: CampusRepository = mock(CampusRepository::class.java)
    private val unidadeUseCases = UnidadeUseCases(repoUnidade, repoCampus)

    @Test
    fun `Deve reler a unidade existente apos violacao de unicidade no save`() {
        val campus = Campus("LIMOEIRO DO NORTE").apply { id = 1L }
        val existente = Unidade(campus, "SEDE").apply { id = 7L }

        // primeira leitura não encontra (corrida); após a violação, a releitura encontra
        Mockito.`when`(repoUnidade.findByCampusAndNome(campus, "SEDE"))
            .thenReturn(null)
            .thenReturn(existente)
        Mockito.`when`(repoUnidade.saveAndFlush(any(Unidade::class.java)))
            .thenThrow(DataIntegrityViolationException("uk_unidade_campus_nome"))

        val resultado = unidadeUseCases.resolverOuCriar(campus, " sede ")

        assertEquals(7L, resultado.id)
        verify(repoUnidade, times(2)).findByCampusAndNome(campus, "SEDE")
    }

    @ParameterizedTest(name = "Nome ''{0}'' normaliza para vazio e deve ser rejeitado")
    @ValueSource(strings = ["***", "---", "   "])
    fun `Deve rejeitar nome que normaliza para vazio`(nomeDigitado: String) {
        val campus = Campus("LIMOEIRO DO NORTE").apply { id = 1L }

        assertFailsWith<IllegalArgumentException> {
            unidadeUseCases.resolverOuCriar(campus, nomeDigitado)
        }
        // A validação ocorre antes de qualquer acesso ao repositório
        verifyNoInteractions(repoUnidade)
    }
}
