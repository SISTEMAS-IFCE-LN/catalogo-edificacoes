package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@DisplayName("Testes para a classe Ambiente")
class AmbienteTest {

    private lateinit var ambiente: Ambiente

    @BeforeEach
    fun setUp() {
        // Geometrias usadas no ambiente
        val ambienteGeometria1 = Retangular(base = BigDecimal("5.55"), altura = BigDecimal("4.25")) // 23.59 m2
        val ambienteGeometria2 = Retangular(base = BigDecimal("6.10"), altura = BigDecimal("5.30")) // 32.33 m2

        // Ambiente anônimo para teste usando listas mutáveis
        ambiente = object : Ambiente(
            id = null,
            nome = "Ambiente de Teste",
            localizacao = Localizacao(Bloco.BLOCO_PRINCIPAL, Unidade.CIDADE_ALTA),
            capacidade = 50,
            tipo = TipoAmbiente.SALA_AULA,
            geometrias = mutableSetOf(ambienteGeometria1, ambienteGeometria2),
            pesDireitos = mutableSetOf(),
            esquadrias = mutableSetOf(),
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
    fun `Deve calcular area das esquadrias agrupadas por tipo e material`() {
        // Dados
        val janela1 = Janela(
            geometria = Retangular(base = BigDecimal("1.00"), altura = BigDecimal("1.50")), // 1.50
            material = MaterialEsquadria.ALUMINIO,
            alturaPeitoril = BigDecimal("0.90")
        )

        val janela2 = Janela(
            geometria = Retangular(base = BigDecimal("0.80"), altura = BigDecimal("1.40")), // 1.12
            material = MaterialEsquadria.ALUMINIO,
            alturaPeitoril = BigDecimal("0.90")
        )

        val porta1 = Porta(
            geometria = Retangular(base = BigDecimal("0.90"), altura = BigDecimal("2.00")), // 1.80
            material = MaterialEsquadria.MADEIRA_MACICA
        )

        ambiente.esquadrias.add(janela1)
        ambiente.esquadrias.add(janela2)
        ambiente.esquadrias.add(porta1)

        val esperado = mapOf(
            Pair(TipoEsquadria.JANELA, MaterialEsquadria.ALUMINIO) to BigDecimal("2.62"), // 1.50 + 1.12
            Pair(TipoEsquadria.PORTA, MaterialEsquadria.MADEIRA_MACICA) to BigDecimal("1.80")
        )

        // Quando
        val resultado = ambiente.calcularAreaEsquadriasPorTipoMaterial()

        // Entao
        assertEquals(esperado, resultado)
    }

    @Test
    fun `Deve calcular area das esquadrias para tipo e material especificados`() {
        // Dados
        val janela1 = Janela(
            geometria = Retangular(base = BigDecimal("1.00"), altura = BigDecimal("1.50")), // 1.50
            material = MaterialEsquadria.ALUMINIO,
            alturaPeitoril = BigDecimal("0.90")
        )

        val janela2 = Janela(
            geometria = Retangular(base = BigDecimal("0.80"), altura = BigDecimal("1.40")), // 1.12
            material = MaterialEsquadria.ALUMINIO,
            alturaPeitoril = BigDecimal("0.90")
        )

        val porta1 = Porta(
            geometria = Retangular(base = BigDecimal("0.90"), altura = BigDecimal("2.00")), // 1.80
            material = MaterialEsquadria.MADEIRA_MACICA
        )

        ambiente.esquadrias.add(janela1)
        ambiente.esquadrias.add(janela2)
        ambiente.esquadrias.add(porta1)

        // Quando
        val resultado = ambiente.calcularAreaEsquadriasPorTipoMaterial(TipoEsquadria.JANELA, MaterialEsquadria.ALUMINIO)

        // Então
        assertEquals(BigDecimal("2.62"), resultado) // 1.50 + 1.12
    }

}
