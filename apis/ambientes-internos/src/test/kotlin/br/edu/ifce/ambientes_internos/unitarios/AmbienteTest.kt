package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import java.math.BigDecimal

@DisplayName("Testes para a classe Ambiente")
class AmbienteTest {

    private lateinit var ambiente: Ambiente
    private lateinit var porta: Porta
    private lateinit var janela: Janela

    @BeforeEach
    fun setUp() {
        // Geometrias usadas no ambiente
        val pisoGeometria1 = Retangular(base = BigDecimal("5.55"), altura = BigDecimal("4.25")) // 23.59 m2
        val pisoGeometria2 = Retangular(base = BigDecimal("6.10"), altura = BigDecimal("5.30")) // 32.33 m2

        // Geometrias para esquadrias
        val portaGeometria = Retangular(base = BigDecimal("0.85"), altura = BigDecimal("2.10")) // 1.79 m2
        val janelaGeometria = Retangular(base = BigDecimal("1.50"), altura = BigDecimal("1.10")) // 1.65 m2

        // Esquadrias
        porta = Porta(
            geometria = portaGeometria,
            materialEsquadria = MaterialEsquadria.VAO_ABERTO
        )

        janela = Janela(
            geometria = janelaGeometria,
            materialEsquadria = MaterialEsquadria.ALUMINIO,
            alturaPeitoril = BigDecimal("0.90")
        )

        // Ambiente anônimo para teste usando listas mutáveis
        ambiente = object : Ambiente(
            id = null,
            nome = "Ambiente de Teste",
            localizacao = "Bloco de Teste",
            capacidade = 50,
            geometrias = mutableSetOf(pisoGeometria1, pisoGeometria2),
            pesDireitos = mutableSetOf(BigDecimal("3.00")),
            esquadrias = mutableSetOf(porta, janela),
            informacaoAdicional = "",
            publicado = true
        ) {}
    }

    @Test
    fun `Deve calcular a area total do ambiente`() {
        // Dados
        val areaEsperada = BigDecimal("55.92") // 23.59 + 32.33

        // Quando
        val areaCalculada = ambiente.calcularAreaAmbienteM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a area total das portas`() {
        // Dados
        val areaEsperada = BigDecimal("1.79")

        // Quando
        val areaCalculada = ambiente.calcularAreaEsquadriasM2(Porta::class)

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a area total das janelas`() {
        // Dados
        val areaEsperada = BigDecimal("1.65")

        // Quando
        val areaCalculada = ambiente.calcularAreaEsquadriasM2(Janela::class)

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve retornar mapa de area por esquadria para um tipo`() {
        // Quando
        val mapa = ambiente.calcularAreaEsquadriasPorTipoM2(Janela::class)

        // Então
        assertEquals(1, mapa.size)
        assertEquals(janela, mapa.keys.first())
        assertEquals(BigDecimal("1.65"), mapa[janela])
    }
}
