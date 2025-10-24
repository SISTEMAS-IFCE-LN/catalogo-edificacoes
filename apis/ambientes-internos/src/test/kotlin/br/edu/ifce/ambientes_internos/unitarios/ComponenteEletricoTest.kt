package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.componentes.Equipamento
import br.edu.ifce.ambientes_internos.model.domain.componentes.Lampada
import br.edu.ifce.ambientes_internos.model.domain.componentes.Luminaria
import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.FixacaoLuminaria
import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.FormatoLampada
import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.MaterialLuminaria
import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.TipoEquipamento
import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.TipoLampada
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@DisplayName("Testes para classe ComponentesEletricos e suas filhas")
class ComponenteEletricoTest {

    @Test
    fun `Equipamento deve calcular a potencia total dos equipamentos`() {
        // Dados
        val equipamento = Equipamento(
            quantidade = 3,
            tipo = TipoEquipamento.COMPUTADOR,
            potenciaWatts = BigDecimal("150.00")
        )

        // Resultado esperado: 3 * 150.00 = 450.00
        val potenciaEsperada = BigDecimal("450.00")

        // Quando
        val potenciaCalculada = equipamento.calcularPotenciaWatts()

        // Então
        assertEquals(potenciaEsperada, potenciaCalculada)
    }

    @Test
    fun `Luminaria deve calcular a potencia de uma luminaria corretamente`() {
        // Dados
        val lampada = Lampada(
            tipo = TipoLampada.LED,
            formato = FormatoLampada.TUBULAR_120CM,
            potenciaWatts = BigDecimal("9.50")
        )
        val luminaria = Luminaria(
            material = MaterialLuminaria.METALICO,
            fixacao = FixacaoLuminaria.EMBUTIR,
            lampada = lampada,
            quantidadeLampadas = 2
        )
        // Resultado esperado: 9.50 * 2 = 19.00
        val potenciaEsperada = BigDecimal("19.00")

        // Quando
        val potenciaCalculada = luminaria.calcularPotenciaLuminariaWatts()

        // Então
        assertEquals(potenciaEsperada, potenciaCalculada)
    }

    @Test
    fun `Luminaria deve calcular a potencia total de varias luminarias`() {
        // Dado
        val lampada = Lampada(
            tipo = TipoLampada.LED,
            formato = FormatoLampada.TUBULAR_60CM,
            potenciaWatts = BigDecimal("10.00")
        )
        val luminaria = Luminaria(
            material = MaterialLuminaria.PLASTICO,
            fixacao = FixacaoLuminaria.EMBUTIR,
            lampada = lampada,
            quantidadeLampadas = 4,
            quantidade = 5
        )
        // Resultado esperado:
        // Potência de 1 luminária = 10.00 * 4 = 40.00
        // Potência total = 40.00 * 5 = 200.00
        val potenciaEsperada = BigDecimal("200.00")

        // Quando
        val potenciaCalculada = luminaria.calcularPotenciaWatts()

        // Então
        assertEquals(potenciaEsperada, potenciaCalculada)
    }
}