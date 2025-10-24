package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Bandeirola
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Folha
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Guarnicao
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.Abertura
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@DisplayName("Testes para a classe Esquadria e suas filhas")
class EsquadriaTest {

    @Test
    fun `Deve calcular a area das folhas de uma esquadria`() {
        // Dados

        // Area: 0.8 * 2.1 = 1.68
        val geometriaFolha1 = Retangular(BigDecimal("0.8"), BigDecimal("2.1"))
        val folha1 = Folha(geometriaFolha1, MaterialEsquadria.MADEIRA_VIDRO, Abertura.ABRIR)

        // Area: 0.3 * 2.1 = 0.63 * 2 = 1.26
        val geometriaFolha2 = Retangular(BigDecimal("0.3"), BigDecimal("2.1"), 2)
        val folha2 = Folha(geometriaFolha2, MaterialEsquadria.MADEIRA_MACICA, Abertura.FIXA)

        val porta = Porta(
            geometria = Retangular(BigDecimal.ONE, BigDecimal.ONE),
            componentes = mutableListOf(folha1, folha2)
        )

        // Área total esperada: 1.68 + 1.26 = 2.94
        val areaEsperada = BigDecimal("2.94")

        // Quando
        val areaCalculada = porta.calcularAreaFolhasM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a area das bandeirolas de uma esquadria`() {
        // Dados

        // Área: 1.4 * 0.3 = 0.42
        val geometriaBandeirola = Retangular(BigDecimal("1.4"), BigDecimal("0.3"))
        val bandeirola = Bandeirola(geometriaBandeirola, MaterialEsquadria.MADEIRA_VIDRO, Abertura.FIXA)

        val porta = Porta(
            geometria = Retangular(BigDecimal.ONE, BigDecimal.ONE),
            componentes = mutableListOf(bandeirola)
        )

        val areaEsperada = BigDecimal("0.42")

        // Quando
        val areaCalculada = porta.calcularAreaBandeirolasM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular o comprimento das guarnicoes de uma esquadria`() {
        // Dados

        // Comprimento: 2.42 * 2 = 4.84
        val geometriaGuarnicao1 = Retangular(BigDecimal("0.15"), BigDecimal("2.42"), 2)
        val guarnicao1 = Guarnicao(geometriaGuarnicao1, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))

        // Comprimento: 1.44
        val geometriaGuarnicao2 = Retangular(BigDecimal("0.15"), BigDecimal("1.44"))
        val guarnicao2 = Guarnicao(geometriaGuarnicao2, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))

        val porta = Porta(
            geometria = Retangular(BigDecimal.ONE, BigDecimal.ONE),
            componentes = mutableListOf(guarnicao1, guarnicao2)
        )

        // Comprimento total esperado: 4.84 + 1.44 = 6.28
        val comprimentoEsperado = BigDecimal("6.28")

        // Quando
        val comprimentoCalculado = porta.calcularComprimentoGuarnicoesM()

        // Então
        assertEquals(comprimentoEsperado, comprimentoCalculado)
    }

    @Test
    fun `Deve calcular a area das guarnicoes de uma esquadria`() {
        // Dados

        // Área: 0.15 * 2.42 = 0.363 * 2 = 0.726 arredondado para 0.73
        val geometriaGuarnicao1 = Retangular(BigDecimal("0.15"), BigDecimal("2.42"), 2)
        val guarnicao1 = Guarnicao(geometriaGuarnicao1, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))

        // Área: 0.15 * 1.44 = 0.216 arredondado para 0.22
        val geometriaGuarnicao2 = Retangular(BigDecimal("0.15"), BigDecimal("1.44"))
        val guarnicao2 = Guarnicao(geometriaGuarnicao2, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))

        val porta = Porta(
            geometria = Retangular(BigDecimal.ONE, BigDecimal.ONE),
            componentes = mutableListOf(guarnicao1, guarnicao2)
        )

        // Área total esperada: 0.73 + 0.22 = 0.95
        val areaEsperada = BigDecimal("0.95")

        // Quando
        val areaCalculada = porta.calcularAreaGuarnicoesM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

}