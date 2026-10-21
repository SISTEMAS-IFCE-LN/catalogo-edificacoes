package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.application.usecases.CampusUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import org.junit.jupiter.api.DisplayName
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.Test
import kotlin.test.assertEquals

@DisplayName("Testes unitários do fallback de corrida dos Casos de Uso de Campus")
class CampusUseCasesTest {

    private val repoCampus: CampusRepository = mock(CampusRepository::class.java)
    private val campusUseCases = CampusUseCases(repoCampus)

    @Test
    fun `Deve reler o campus existente apos violacao de unicidade no save`() {
        val existente = Campus("LIMOEIRO DO NORTE").apply { id = 42L }

        // primeira leitura não encontra (corrida); após a violação, a releitura encontra
        Mockito.`when`(repoCampus.findByNome("LIMOEIRO DO NORTE"))
            .thenReturn(null)
            .thenReturn(existente)
        Mockito.`when`(repoCampus.saveAndFlush(any(Campus::class.java)))
            .thenThrow(DataIntegrityViolationException("uk_campus_nome"))

        val resultado = campusUseCases.resolverOuCriar(" limoeiro do norte ")

        assertEquals(42L, resultado.id)
        verify(repoCampus, times(2)).findByNome("LIMOEIRO DO NORTE")
    }
}
