package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.NormalizadorNome
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals

@DisplayName("Testes para o normalizador de nomes de localização")
class NormalizadorNomeTest {

    private val casos: Map<String, String> = linkedMapOf(
        "  bloco  10 " to "BLOCO 10",
        "Ginásio" to "GINASIO",
        "Cidade Alta" to "CIDADE ALTA",
        "bloco-01" to "BLOCO 1",
        "***" to "",
        "BLOCO 0002" to "BLOCO 2"
    )

    @Test
    fun `Deve normalizar os nomes conforme a tabela de casos`() {
        casos.forEach { (input, expected) ->
            assertEquals(
                expected,
                NormalizadorNome.normalizar(input),
                "Falha ao normalizar o nome para o input: '$input'"
            )
        }
    }
}
