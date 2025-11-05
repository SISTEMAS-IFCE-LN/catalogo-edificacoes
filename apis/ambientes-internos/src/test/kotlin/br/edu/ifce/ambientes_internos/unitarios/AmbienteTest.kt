package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
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
    private lateinit var janela1: Janela
    private lateinit var janela2: Janela

    @BeforeEach
    fun setUp() {
        // Geometrias usadas no ambiente
        val ambienteGeometria1 = Retangular(base = BigDecimal("5.55"), altura = BigDecimal("4.25")) // 23.59 m2
        val ambienteGeometria2 = Retangular(base = BigDecimal("6.10"), altura = BigDecimal("5.30")) // 32.33 m2

        // Geometrias para esquadrias
        val portaGeometria = Retangular(base = BigDecimal("0.85"), altura = BigDecimal("2.10")) // 1.79 m2
        val janelaGeometria1 = Retangular(base = BigDecimal("1.50"), altura = BigDecimal("1.10")) // 1.65 m2
        val janelaGeometria2 = Retangular(base = BigDecimal("1.20"), altura = BigDecimal("1.00")) // 1.20 m2

        // Esquadrias
        porta = Porta(
            geometria = portaGeometria,
            tipo = TipoEsquadria.VAO_ABERTO
        )

        janela1 = Janela(
            geometria = janelaGeometria1,
            tipo = TipoEsquadria.ALUMINIO,
            alturaPeitoril = BigDecimal("0.90")
        )

        janela2 = Janela(
            geometria = janelaGeometria2,
            tipo = TipoEsquadria.MADEIRA_MACICA,
            alturaPeitoril = BigDecimal("0.80")
        )

        // Ambiente anônimo para teste usando listas mutáveis
        ambiente = object : Ambiente(
            id = null,
            nome = "Ambiente de Teste",
            localizacao = "Bloco de Teste",
            tipo = TipoAmbiente.SALA_AULA,
            capacidade = 50,
            geometrias = mutableSetOf(ambienteGeometria1, ambienteGeometria2),
            pesDireitos = mutableSetOf(BigDecimal("3.00")),
            esquadrias = mutableSetOf(porta, janela1, janela2),
            informacaoAdicional = "",
            status = StatusAmbiente.PUBLICADO
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
        val areaEsperada = BigDecimal("1.65").add(BigDecimal("1.20")) // 1.65 + 1.20 = 2.85

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
        assertEquals(2, mapa.size)
        assertEquals(BigDecimal("1.65"), mapa[janela1])
        assertEquals(BigDecimal("1.20"), mapa[janela2])
    }

    @Test
    fun `Deve retornar mapa de area por geometria`() {
        // Dados
        val geometria1 = Retangular(base = BigDecimal("5.55"), altura = BigDecimal("4.25")) // 23.59 m2
        val geometria2 = Retangular(base = BigDecimal("6.10"), altura = BigDecimal("5.30")) // 32.33 m2

        // Quando
        val mapa = ambiente.calcularAreaAmbientePorGeometriaM2()

        // Então
        assertEquals(2, mapa.size)
        assertEquals(BigDecimal("23.59"), mapa[geometria1])
        assertEquals(BigDecimal("32.33"), mapa[geometria2])
    }

}
