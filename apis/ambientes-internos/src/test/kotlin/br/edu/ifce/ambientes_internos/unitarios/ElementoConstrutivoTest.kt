package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.ParedeExterna
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.ParedeInterna
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Piso
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Teto
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.Revestimento
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoParede
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoPiso
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoTeto
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Triangular
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@DisplayName("Testes para a classe ElementoConstrutivo e suas filhas")
class ElementoConstrutivoTest {

    @Test
    fun `Piso deve calcular area total considerando geometrias e quantidade`() {
        // Dados
        val ret = Retangular(BigDecimal("2.50"), BigDecimal("3.00")) // Área: 7.50
        val tri = Triangular(BigDecimal("2.00"), BigDecimal("1.00")) // Área: 1.00
        val piso = Piso(TipoPiso.CERAMICA, mutableListOf(ret, tri), quantidade = 2)

        // Área esperada: (7.50 + 1.00) * 2 = 17.00
        val areaEsperada = BigDecimal("17.00")

        // Quando
        val areaTotal = piso.calcularAreaTotalM2()

        // Então
        assertEquals(areaEsperada, areaTotal)
    }

    @Test
    fun `Teto deve calcular area total considerando geometrias e quantidade`() {
        // Dados
        val ret = Retangular(BigDecimal("4.00"), BigDecimal("2.50")) // Área: 10.00
        val teto = Teto(TipoTeto.FORRO_GESSO, Revestimento.PINTURA, mutableListOf(ret), quantidade = 1)

        // Área esperada: 10.00 * 1 = 10.00
        val areaEsperada = BigDecimal("10.00")

        // Quando
        val areaTotal = teto.calcularAreaTotalM2()

        // Então
        assertEquals(areaEsperada, areaTotal)
    }

    @Test
    fun `ParedeExterna deve calcular area total descontando esquadrias`() {
        // Dados
        val geometriaParede = Retangular(BigDecimal("3.00"), BigDecimal("2.80")) // Área parede: 8.40
        val portaGeom = Retangular(BigDecimal("0.90"), BigDecimal("2.10")) // Área porta: 1.89

        val porta = Porta(portaGeom)

        val parede = ParedeExterna(
            tipo = TipoParede.ALVENARIA,
            revestimento = Revestimento.REBOCO,
            geometrias = mutableListOf(geometriaParede),
            esquadrias = mutableListOf(porta),
            quantidade = 1
        )

        // Área esperada: 8.40 - 1.89 = 6.51
        val valorEsperado = BigDecimal("6.51")

        // Quando
        val areaTotal = parede.calcularAreaTotalM2()

        // Então
        assertEquals(valorEsperado, areaTotal)
    }

    @Test
    fun `ParedeInterna deve calcular area total descontando esquadrias`() {
        // Dados
        val geometriaParede = Retangular(BigDecimal("2.50"), BigDecimal("2.50")) // Área parede: 6.25
        val portaGeom = Retangular(BigDecimal("0.60"), BigDecimal("1.80")) // Área porta: 1.08

        val porta = Porta(portaGeom)

        val parede = ParedeInterna(
            tipo = TipoParede.DRYWALL,
            revestimento = Revestimento.PINTURA,
            geometrias = mutableListOf(geometriaParede),
            esquadrias = mutableListOf(porta),
            quantidade = 4
        )

        // Área esperada: (6.25 - 1.08) * 4 = 20.68
        val valorEsperado = BigDecimal("20.68")

        // Quando
        val areaTotal = parede.calcularAreaTotalM2()

        // Então
        assertEquals(valorEsperado, areaTotal)
    }

}