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
    val geometriaFolha2 = Retangular(BigDecimal("0.3"), BigDecimal("2.1"))

    val folha1 = Folha(geometriaFolha1, MaterialEsquadria.MADEIRA_VIDRO, Abertura.ABRIR)
    val folha2 = Folha(geometriaFolha2, MaterialEsquadria.MADEIRA_MACICA, Abertura.FIXA, 2)

    val geometriaBandeirola = Retangular(BigDecimal("1.4"), BigDecimal("0.3"))

    val bandeirola = Bandeirola(geometriaBandeirola, MaterialEsquadria.MADEIRA_VIDRO, Abertura.FIXA)

    val geometriaGuarnicao1 = Retangular(BigDecimal("0.15"), BigDecimal("2.42"), 2)
    val geometriaGuarnicao2 = Retangular(BigDecimal("0.15"), BigDecimal("1.44"))
    val geometriaGuarnicao3 = Retangular(BigDecimal("0.15"), BigDecimal("1.42"))

    val guarnicao1 = Guarnicao(geometriaGuarnicao1, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))
    val guarnicao2 = Guarnicao(geometriaGuarnicao2, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))
    val guarnicao3 = Guarnicao(geometriaGuarnicao3, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))

    val geometriaPorta = Retangular(BigDecimal("1.44"), BigDecimal("2.44"))

    val componentes = mutableListOf(folha1, folha2, bandeirola, guarnicao1, guarnicao2, guarnicao3)

    val porta1 = Porta(geometriaPorta, componentes = componentes)
    val porta2 = Porta(geometriaPorta, 7, componentes)

    private fun <T, R> filtrarClasse(classe: Class<T>, lista: MutableList<R>): List<T> {
        return lista.filter { classe.isInstance(it) }.map { classe.cast(it) }
    }

    private fun <T, R> contarItens(
        classe: Class<T>,
        lista: MutableList<R>,
        repeticao: Int,
        contar: (T) -> Int
    ): Int {
        return filtrarClasse(classe, lista)
            .fold(0) { somatoria, item -> somatoria + contar(item) } * repeticao
    }

    private fun <T, R> aplicarCalculo(
        classe: Class<T>,
        lista: MutableList<R>,
        repeticao: Int,
        calcular: (T) -> BigDecimal
    ): BigDecimal {
        return filtrarClasse(classe, lista)
            .fold(BigDecimal.ZERO) { somatoria, item -> somatoria.add(calcular(item)) }
            .multiply(BigDecimal(repeticao))
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun calcularAreaRetangular(geometria: Retangular, quantidade: Int): BigDecimal {
        return geometria.base.multiply(geometria.altura)
            .multiply(BigDecimal(quantidade))
    }

    @Test
    fun `Deve obter a quantidade de folhas de uma ou mais esquadrias`() {
        // Dados
        val quantidadeEsperadaFolhasPorta1 = contarItens(
            Folha::class.java,
            porta1.componentes,
            porta1.quantidade
        ) { it.quantidade }

        val quantidadeEsperadaFolhasPorta2 = contarItens(
            Folha::class.java,
            porta2.componentes,
            porta2.quantidade
        ) { it.quantidade }

        // Quando
        val quantidadeFolhasPorta1 = porta1.obterQuantidadeTotalFolhas()
        val quantidadeFolhasPorta2 = porta2.obterQuantidadeTotalFolhas()

        // Então
        assertEquals(quantidadeEsperadaFolhasPorta1, quantidadeFolhasPorta1)
        assertEquals(quantidadeEsperadaFolhasPorta2, quantidadeFolhasPorta2)
    }

    @Test
    fun `Deve calcular a area de folhas de uma ou mais esquadrias`() {
        // Dados
        val areaEsperadaFolhasPorta1 = aplicarCalculo(
            Folha::class.java,
            porta1.componentes,
            porta1.quantidade
        ) { calcularAreaRetangular(it.geometria as Retangular, it.quantidade) }

        val areaEsperadaFolhasPorta2 = aplicarCalculo(
            Folha::class.java,
            porta2.componentes,
            porta2.quantidade
        ) { calcularAreaRetangular(it.geometria as Retangular, it.quantidade) }

        // Quando
        val areaFolhasPorta1 = porta1.calcularAreaTotalFolhasM2()
        val areaFolhasPorta2 = porta2.calcularAreaTotalFolhasM2()

        // Então
        assertEquals(areaEsperadaFolhasPorta1, areaFolhasPorta1)
        assertEquals(areaEsperadaFolhasPorta2, areaFolhasPorta2)
    }

    @Test
    fun `Deve obter a quantidade de bandeirolas de uma ou mais esquadrias`() {
        // Dados
        val quantidadeEsperadaBandeirolasPorta1 = contarItens(
            Bandeirola::class.java,
            porta1.componentes,
            porta1.quantidade
        ) { it.quantidade }

        val quantidadeEsperadaBandeirolasPorta2 = contarItens(
            Bandeirola::class.java,
            porta2.componentes,
            porta2.quantidade
        ) { it.quantidade }

        // Quando
        val quantidadeBandeirolasPorta1 = porta1.obterQuantidadeTotalBandeirolas()
        val quantidadeBandeirolasPorta2 = porta2.obterQuantidadeTotalBandeirolas()

        // Então
        assertEquals(quantidadeEsperadaBandeirolasPorta1, quantidadeBandeirolasPorta1)
        assertEquals(quantidadeEsperadaBandeirolasPorta2, quantidadeBandeirolasPorta2)
    }

    @Test
    fun `Deve calcular a area de bandeirolas de uma ou mais esquadrias`() {
        // Dados
        val areaEsperadaBandeirolasPorta1 = aplicarCalculo(
            Bandeirola::class.java,
            porta1.componentes,
            porta1.quantidade
        ) { calcularAreaRetangular(it.geometria as Retangular, it.quantidade) }

        val areaEsperadaBandeirolasPorta2 = aplicarCalculo(
            Bandeirola::class.java,
            porta2.componentes,
            porta2.quantidade
        ) { calcularAreaRetangular(it.geometria as Retangular, it.quantidade) }

        // Quando
        val areaBandeirolasPorta1 = porta1.calcularAreaTotalBandeirolasM2()
        val areaBandeirolasPorta2 = porta2.calcularAreaTotalBandeirolasM2()

        // Então
        assertEquals(areaEsperadaBandeirolasPorta1, areaBandeirolasPorta1)
        assertEquals(areaEsperadaBandeirolasPorta2, areaBandeirolasPorta2)
    }

    @Test
    fun `Deve calcular o comprimento de guarnicoes de uma ou mais esquadrias`() {
        // Dados
        val comprimentoEsperadoGuarnicoesPorta1 = aplicarCalculo(
            Guarnicao::class.java,
            porta1.componentes,
            porta1.quantidade
        ) { it.geometria.altura.multiply(BigDecimal(it.quantidade)) }

        val comprimentoEsperadoGuarnicoesPorta2 = aplicarCalculo(
            Guarnicao::class.java,
            porta2.componentes,
            porta2.quantidade
        ) { it.geometria.altura.multiply(BigDecimal(it.quantidade)) }

        // Quando
        val comprimentoGuarnicoesPorta1 = porta1.calcularComprimentoTotalGuarnicoesM()
        val comprimentoGuarnicoesPorta2 = porta2.calcularComprimentoTotalGuarnicoesM()

        // Então
        assertEquals(comprimentoEsperadoGuarnicoesPorta1, comprimentoGuarnicoesPorta1)
        assertEquals(comprimentoEsperadoGuarnicoesPorta2, comprimentoGuarnicoesPorta2)
    }

    @Test
    fun `Deve calcular a area de guarnicoes de uma ou mais esquadrias`() {
        // Dados
        val areaEsperadaGuarnicoesPorta1 = aplicarCalculo(
            Guarnicao::class.java,
            porta1.componentes,
            porta1.quantidade
        ) { calcularAreaRetangular(it.geometria as Retangular, it.quantidade) }

        val areaEsperadaGuarnicoesPorta2 = aplicarCalculo(
            Guarnicao::class.java,
            porta2.componentes,
            porta2.quantidade
        ) { calcularAreaRetangular(it.geometria as Retangular, it.quantidade) }

        // Quando
        val areaGuarnicoesPorta1 = porta1.calcularAreaTotalGuarnicoesM2()
        val areaGuarnicoesPorta2 = porta2.calcularAreaTotalGuarnicoesM2()

        // Então
        assertEquals(areaEsperadaGuarnicoesPorta1, areaGuarnicoesPorta1)
        assertEquals(areaEsperadaGuarnicoesPorta2, areaGuarnicoesPorta2)
    }

    @Test
    fun `Deve calcular o vao util de uma porta`() {
        // Dados
        val vaoUtilEsperadoPorta = porta1.componentes.filter {
            it.javaClass == Folha::class.java && (it as Folha).abertura == Abertura.ABRIR
        }.fold(BigDecimal.ZERO) { somatoria, folha ->
            somatoria.add(
                folha.geometria.base.multiply(folha.geometria.altura)
                    .multiply(BigDecimal(folha.quantidade))
            )
        }.setScale(2, RoundingMode.HALF_UP)

        // Quando
        val vaoUtilPorta = porta1.calcularVaoUtil()

        // Então
        assertEquals(vaoUtilEsperadoPorta, vaoUtilPorta)
    }

}