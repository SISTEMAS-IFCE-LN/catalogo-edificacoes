package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Triangular
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@DisplayName("Testes para a classe Geometria e suas filhas")
class GeometriaTest {

    @Test
    fun `Deve calcular a area total de uma geometria retangular`() {
        // Dados
        val retangulo = Retangular(
            base = BigDecimal("2.5"),
            altura = BigDecimal("3.65")
        )
        // Área esperada: 2.5 * 3.65 = 9.125 -> 9.13
        val areaEsperada = BigDecimal("9.13")

        // Quando
        val areaCalculada = retangulo.calcularAreaTotalM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a area total de multiplas geometrias retangulares`() {
        // Dados
        val retangulo = Retangular(
            base = BigDecimal("4.58"),
            altura = BigDecimal("1.05"),
            repeticao = 6
        )
        // Área esperada: (4.58 * 1.05) * 6 = 4.809 * 6 = 28.854 -> 28.85
        val areaEsperada = BigDecimal("28.85")

        // Quando
        val areaCalculada = retangulo.calcularAreaTotalM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a area total de uma geometria triangular`() {
        // Dados
        val triangulo = Triangular(
            base = BigDecimal("2.5"),
            altura = BigDecimal("3.65")
        )
        // Área esperada: (2.5 * 3.65) / 2 = 4.5625 -> 4.56
        val areaEsperada = BigDecimal("4.56")

        // Quando
        val areaCalculada = triangulo.calcularAreaTotalM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a area total de multiplas geometrias triangulares`() {
        // Dados
        val triangulo = Triangular(
            base = BigDecimal("4.58"),
            altura = BigDecimal("1.05"),
            repeticao = 6
        )
        // Área esperada: ((4.58 * 1.05) / 2) * 6 = 2.4045 * 6 = 14.427 -> 14.43
        val areaEsperada = BigDecimal("14.43")

        // Quando
        val areaCalculada = triangulo.calcularAreaTotalM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }
}