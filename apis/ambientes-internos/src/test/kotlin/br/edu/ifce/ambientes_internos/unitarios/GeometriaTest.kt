package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Triangular
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class GeometriaTest {

    val base1 = BigDecimal("2.5")
    val altura1 = BigDecimal("3.65")
    val base2 = BigDecimal("4.58")
    val altura2 = BigDecimal("1.05")
    val repeticao = 6

    @Test
    fun `Deve calcular a area total de uma ou mais geometrias retangulares`() {
        // Dados
        val retangulo1 = Retangular(base1, altura1)
        val retangulo2 = Retangular(base2, altura2, repeticao)

        val areaEsperada1 = BigDecimal("9.13")
        val areaEsperada2 = BigDecimal("28.85")

        // Quando
        val areaTotal1 = retangulo1.calcularAreaTotalM2()
        val areaTotal2 = retangulo2.calcularAreaTotalM2()

        // Então
        assertEquals(areaEsperada1, areaTotal1)
        assertEquals(areaEsperada2, areaTotal2)
    }

    @Test
    fun `Deve calcular a area total de uma ou mais geometrias triangulares`() {
        // Dados
        val triangulo1 = Triangular(base1, altura1)
        val triangulo2 = Triangular(base2, altura2, repeticao)

        val areaEsperada1 = BigDecimal("4.56")
        val areaEsperada2 = BigDecimal("14.43")

        // Quando
        val areaTotal1 = triangulo1.calcularAreaTotalM2()
        val areaTotal2 = triangulo2.calcularAreaTotalM2()

        // Então
        assertEquals(areaEsperada1, areaTotal1)
        assertEquals(areaEsperada2, areaTotal2)
    }
}