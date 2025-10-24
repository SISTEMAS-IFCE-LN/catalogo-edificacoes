package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.componentes.ComponenteEletrico
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.ParedeExterna
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.ParedeInterna
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Piso
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Teto
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.Revestimento
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoParede
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoPiso
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoTeto
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import java.math.BigDecimal

@DisplayName("Testes para a classe Ambiente e suas filhas")
class AmbienteTest {

    private lateinit var ambiente: Ambiente

    @BeforeEach
    fun setUp() {
        // Geometrias
        val pisoGeometria1 = Retangular(base = BigDecimal("5.55"), altura = BigDecimal("4.25")) // 23.59 m2
        val pisoGeometria2 = Retangular(base = BigDecimal("6.10"), altura = BigDecimal("5.30")) // 32.33 m2
        val paredeGeometriaExterna1 =
            Retangular(base = BigDecimal("5.55"), altura = BigDecimal("2.80")) // 15.54 m2
        val paredeGeometriaExterna2 =
            Retangular(base = BigDecimal("4.25"), altura = BigDecimal("2.80")) // 11.90 m2
        val paredeGeometriaInterna = Retangular(base = BigDecimal("3.80"), altura = BigDecimal("2.60")) // 9.88 m2
        val tetoGeometria = Retangular(base = BigDecimal("11.65"), altura = BigDecimal("4.80")) // 55.92 m2
        val portaGeometria = Retangular(base = BigDecimal("0.85"), altura = BigDecimal("2.10")) // 1.79 m2
        val janelaGeometria = Retangular(base = BigDecimal("1.50"), altura = BigDecimal("1.10")) // 1.65 m2

        // Pisos
        val pisos = mutableListOf(
            Piso(tipo = TipoPiso.CERAMICA, geometrias = mutableListOf(pisoGeometria1)),
            Piso(tipo = TipoPiso.PORCELANATO, geometrias = mutableListOf(pisoGeometria2))
        )

        // Esquadrias
        val porta = Porta(geometria = portaGeometria)
        val janela = Janela(geometria = janelaGeometria)

        // Paredes
        val paredes = mutableListOf(
            // Externa 1 e 2: 15.54 (área bruta) - 1.79 (porta) = 13.75 * 2 = 27.50 m2
            ParedeExterna(
                tipo = TipoParede.ALVENARIA, revestimento = Revestimento.PINTURA,
                geometrias = mutableListOf(paredeGeometriaExterna1), esquadrias = mutableListOf(porta), quantidade = 2
            ),
            // Externa 2 e 4: 11.90 (área bruta) - 1.65 (janela) = 10.25 * 2  = 20.50 m2
            ParedeExterna(
                tipo = TipoParede.ALVENARIA, revestimento = Revestimento.PINTURA,
                geometrias = mutableListOf(paredeGeometriaExterna2), esquadrias = mutableListOf(janela), quantidade = 2
            ),
            // Interna 1, 2 e 3: Area líquida: 9.88 * 3 = 29.64 m2
            ParedeInterna(
                tipo = TipoParede.DRYWALL, revestimento = Revestimento.TEXTURA,
                geometrias = mutableListOf(paredeGeometriaInterna), quantidade = 3
            )
        )

        // Tetos
        val tetos = mutableListOf(
            Teto(
                tipo = TipoTeto.FORRO_GESSO, revestimento = Revestimento.PINTURA,
                geometrias = mutableListOf(tetoGeometria)
            )
        )

        // Componentes
        val componentes = mutableListOf(
            object : ComponenteEletrico(id = null, quantidade = 1, informacaoAdicional = "Iluminação") {
                override fun calcularPotenciaWatts(): BigDecimal = BigDecimal("100.00")
            },
            object : ComponenteEletrico(id = null, quantidade = 1, informacaoAdicional = "Ar Condicionado") {
                override fun calcularPotenciaWatts(): BigDecimal = BigDecimal("1500.00")
            }
        )

        // Ambiente anônimo para teste
        ambiente = object : Ambiente(
            id = null,
            nome = "Ambiente de Teste",
            localizacao = "Bloco de Teste",
            capacidade = 50,
            informacaoAdicional = "",
            publicado = true,
            pisos = pisos,
            paredes = paredes,
            tetos = tetos,
            componentes = componentes.toMutableList()
        ) {}
    }

    @Test
    fun `Deve calcular a area total dos pisos`() {
        // Dados
        val areaEsperada = BigDecimal("55.92") // 23.59 + 32.33

        // Quando
        val areaCalculada = ambiente.calcularAreaPisosM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a area total das paredes externas`() {
        // Dados
        val areaEsperada = BigDecimal("48.00") // 27.50 + 20.50

        // Quando
        val areaCalculada = ambiente.calcularAreaParedesExternasM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a area total das paredes internas`() {
        // Dados
        val areaEsperada = BigDecimal("29.64")

        // Quando
        val areaCalculada = ambiente.calcularAreaParedesInternasM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a area total das portas`() {
        // Dados
        val areaEsperada = BigDecimal("1.79")

        // Quando
        val areaCalculada = ambiente.calcularAreaPortasM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a area total das janelas`() {
        // Dados
        val areaEsperada = BigDecimal("1.65")

        // Quando
        val areaCalculada = ambiente.calcularAreaJanelasM2()

        // Então
        assertEquals(areaEsperada, areaCalculada)
    }

    @Test
    fun `Deve calcular a carga total do ambiente em watts`() {
        // Dados
        val cargaEsperada = BigDecimal("1600.00") // 100.00 + 1500.00

        // Quando
        val cargaCalculada = ambiente.calcularCargaAmbienteWatts()

        // Então
        assertEquals(cargaEsperada, cargaCalculada)
    }

    @Test
    fun `Deve calcular a area dos pisos agrupada por tipo`() {
        // Dados
        val areasEsperadas = mapOf(
            TipoPiso.CERAMICA.nome to BigDecimal("23.59"),
            TipoPiso.PORCELANATO.nome to BigDecimal("32.33")
        )

        // Quando
        val areasCalculadas = ambiente.calcularAreaPisosPorTipoRevestimentoM2()

        // Então
        assertEquals(areasEsperadas, areasCalculadas)
    }

    @Test
    fun `Deve calcular a area dos tetos agrupada por tipo de revestimento`() {
        val areasEsperadas = mapOf(
            "${TipoTeto.FORRO_GESSO.nome} - ${Revestimento.PINTURA.nome}" to BigDecimal("55.92")
        )
        val areasCalculadas = ambiente.calcularAreaTetosPorTipoRevestimentoM2()
        assertEquals(areasEsperadas, areasCalculadas)
    }

    @Test
    fun `Deve calcular a area das paredes agrupada por tipo de revestimento`() {
        // Dados
        val areasEsperadas = mapOf(
            "${TipoParede.ALVENARIA.nome} - ${Revestimento.PINTURA.nome}" to BigDecimal("48.00"), // 27.50 + 20.50
            "${TipoParede.DRYWALL.nome} - ${Revestimento.TEXTURA.nome}" to BigDecimal("29.64")
        )
        // Quando
        val areasCalculadas = ambiente.calcularAreaParedesPorTipoRevestimentoM2()

        // Então
        assertEquals(areasEsperadas, areasCalculadas)
    }
}

