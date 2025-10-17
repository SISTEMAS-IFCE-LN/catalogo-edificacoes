package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Bandeirola
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Folha
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Guarnicao
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.Abertura
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class EsquadriasTest {

    val geometriaFolha1 = Retangular(BigDecimal("0.8"), BigDecimal("2.1"))
    val geometriaFolha2 = Retangular(BigDecimal("0.3"), BigDecimal("2.1"))

    val folha1 = Folha(geometriaFolha1, MaterialEsquadria.MADEIRA_VIDRO, Abertura.ABRIR)
    val folha2 = Folha(geometriaFolha2, MaterialEsquadria.MADEIRA_MACICA, Abertura.FIXA)

    val geometriaBandeirola = Retangular(BigDecimal("1.1"), BigDecimal("0.3"))

    val bandeirola = Bandeirola(geometriaBandeirola, MaterialEsquadria.MADEIRA_VIDRO, Abertura.FIXA)

    val geometriaGuarnicao1 = Retangular(BigDecimal("0.15"), BigDecimal("2.42"), 2)
    val geometriaGuarnicao2 = Retangular(BigDecimal("0.15"), BigDecimal("1.14"))
    val geometriaGuarnicao3 = Retangular(BigDecimal("0.15"), BigDecimal("1.12"))

    val guarnicao1 = Guarnicao(geometriaGuarnicao1, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))
    val guarnicao2 = Guarnicao(geometriaGuarnicao2, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))
    val guarnicao3 = Guarnicao(geometriaGuarnicao3, MaterialEsquadria.MADEIRA_MACICA, BigDecimal("0.02"))

    val geometriaPorta = Retangular(BigDecimal("1.14"), BigDecimal("2.44"))

    val componentes = mutableListOf(folha1, folha2, bandeirola, guarnicao1, guarnicao2, guarnicao3)

    val porta1 = Porta(geometriaPorta, componentes = componentes)
    val porta2 = Porta(geometriaPorta, 7, componentes)

    @Test
    fun `Deve obter a quantidade de folhas de uma ou mais esquadrias`() {
        // Dados
        val quantidadeEsperadaFolhasPorta1 = 1
        val quantidadeEsperadaFolhasPorta2 = 7

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
        val areaEsperadaFolhasPorta1 = BigDecimal("1.16")
        val areaEsperadaFolhasPorta2 = BigDecimal("8.09")

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
        val quantidadeEsperadaBandeirolasPorta1 = 1
        val quantidadeEsperadaBandeirolasPorta2 = 7

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
        val areaEsperadaBandeirolasPorta1 = BigDecimal("0.33")
        val areaEsperadaBandeirolasPorta2 = BigDecimal("2.31")

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
        val comprimentoEsperadoGuarnicoesPorta1 = BigDecimal("7.10")
        val comprimentoEsperadoGuarnicoesPorta2 = BigDecimal("49.70")

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
        val areaEsperadaGuarnicoesPorta1 = BigDecimal("1.07")
        val areaEsperadaGuarnicoesPorta2 = BigDecimal("7.49")

        // Quando
        val areaGuarnicoesPorta1 = porta1.calcularAreaTotalGuarnicoesM2()
        val areaGuarnicoesPorta2 = porta2.calcularAreaTotalGuarnicoesM2()

        // Então
        assertEquals(areaEsperadaGuarnicoesPorta1, areaGuarnicoesPorta1)
        assertEquals(areaEsperadaGuarnicoesPorta2, areaGuarnicoesPorta2)
    }

}