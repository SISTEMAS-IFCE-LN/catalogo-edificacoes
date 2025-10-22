package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Bandeirola
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Folha
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Guarnicao
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.Abertura
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals

class EsquadriaTest {

    val geometriaFolha1 = Retangular(BigDecimal("0.8"), BigDecimal("2.1"))
    val geometriaFolha2 = Retangular(BigDecimal("0.3"), BigDecimal("2.1"), 2)

    val folha1 = Folha(geometriaFolha1, MaterialEsquadria.MADEIRA_VIDRO, Abertura.ABRIR)
    val folha2 = Folha(geometriaFolha2, MaterialEsquadria.MADEIRA_MACICA, Abertura.FIXA)

    val geometriaBandeirola = Retangular(BigDecimal("1.4"), BigDecimal("0.3"))

    val bandeirola = Bandeirola(geometriaBandeirola, MaterialEsquadria.MADEIRA_VIDRO, Abertura.FIXA)

    val geometriaGuarnicao1 = Retangular(BigDecimal("0.15"), BigDecimal("2.42"), 2)
    val geometriaGuarnicao2 = Retangular(BigDecimal("0.15"), BigDecimal("1.44"))
    val geometriaGuarnicao3 = Retangular(BigDecimal("0.15"), BigDecimal("1.40"))

    val guarnicao1 = Guarnicao(geometriaGuarnicao1, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))
    val guarnicao2 = Guarnicao(geometriaGuarnicao2, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))
    val guarnicao3 = Guarnicao(geometriaGuarnicao3, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))

    val geometriaPorta = Retangular(BigDecimal("1.44"), BigDecimal("2.44"))

    val componentes = mutableListOf(folha1, folha2, bandeirola, guarnicao1, guarnicao2, guarnicao3)

    val porta = Porta(geometriaPorta, componentes)

    private fun <T, R> filtrarClasse(classe: Class<T>, lista: MutableList<R>): List<T> {
        return lista.filter { classe.isInstance(it) }.map { classe.cast(it) }
    }

    private fun <T, R> aplicarCalculo(
        classe: Class<T>,
        lista: MutableList<R>,
        calcular: (T) -> BigDecimal
    ): BigDecimal {
        return filtrarClasse(classe, lista)
            .fold(BigDecimal.ZERO) { somatoria, item -> somatoria.add(calcular(item)) }
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun calcularAreaRetangular(geometria: Retangular): BigDecimal {
        return geometria.base.multiply(geometria.altura)
            .multiply(BigDecimal(geometria.repeticao))
    }

    @Test
    fun `Deve calcular a area das folhas de uma esquadria`() {
        // Dados
        val areaEsperadaFolhasPorta = aplicarCalculo(
            Folha::class.java,
            porta.componentes
        ) { calcularAreaRetangular(it.geometria as Retangular) }


        // Quando
        val areaFolhasPorta = porta.calcularAreaFolhasM2()

        // Então
        assertEquals(areaEsperadaFolhasPorta, areaFolhasPorta)
    }

    @Test
    fun `Deve calcular a area das bandeirolas de uma esquadria`() {
        // Dados
        val areaEsperadaBandeirolasPorta = aplicarCalculo(
            Bandeirola::class.java,
            porta.componentes
        ) { calcularAreaRetangular(it.geometria as Retangular) }

        // Quando
        val areaBandeirolasPorta = porta.calcularAreaBandeirolasM2()

        // Então
        assertEquals(areaEsperadaBandeirolasPorta, areaBandeirolasPorta)
    }

    @Test
    fun `Deve calcular o comprimento das guarnicoes de uma esquadria`() {
        // Dados
        val comprimentoEsperadoGuarnicoesPorta = aplicarCalculo(
            Guarnicao::class.java,
            porta.componentes
        ) { it.geometria.altura.multiply(BigDecimal(it.geometria.repeticao)) }

        // Quando
        val comprimentoGuarnicoesPorta = porta.calcularComprimentoGuarnicoesM()

        // Então
        assertEquals(comprimentoEsperadoGuarnicoesPorta, comprimentoGuarnicoesPorta)
    }

    @Test
    fun `Deve calcular a area das guarnicoes de uma esquadria`() {
        // Dados
        val areaEsperadaGuarnicoesPorta = aplicarCalculo(
            Guarnicao::class.java,
            porta.componentes
        ) { calcularAreaRetangular(it.geometria as Retangular) }

        // Quando
        val areaGuarnicoesPorta = porta.calcularAreaGuarnicoesM2()

        // Então
        assertEquals(areaEsperadaGuarnicoesPorta, areaGuarnicoesPorta)
    }

}