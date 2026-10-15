package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import org.junit.jupiter.api.DisplayName
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.DefaultAsserter.assertNotEquals
import kotlin.test.Test

@DisplayName("Testes para a classe Bloco")
class BlocoTest {

    private val inputCases: Map<String, String> = hashMapOf(
        " ginásio " to "GINASIO",
        "Bloco 01" to "BLOCO 1",
        "BLOCO 0010" to "BLOCO 10",
        "Bloco-2" to "BLOCO 2",
        "Biblioteca/ Acadêmica" to "BIBLIOTECA ACADEMICA",
        "Casa   de Mel" to "CASA DE MEL",
        "Pátio" to "PATIO",
        "Bloco 1A" to "BLOCO 1A",
        "   " to ""
    )

    @Test
    fun `Deve normalizar corretamente os nomes dos blocos`() {
        inputCases.forEach { (input, expected) ->
            val blocoInput1 = Bloco(Unidade.SEDE, input)
            val blocoInput2 = Bloco(Unidade.SEDE, expected)

            val blocoExpected1 = Bloco(Unidade.SEDE, expected)
            val blocoExpected2 = Bloco(Unidade.CIDADE_ALTA, expected)

            val blocoAny = Bloco(Unidade.SEDE, "Qualquer")
            blocoAny.nome = input

            assertEquals(
                "Falha ao normalizar o nome do bloco para o input: '$input'",
                expected,
                blocoInput1.nome
            )
            assertEquals(
                "Falha ao comparar blocos com nomes normalizados: '$input' e '$expected'",
                blocoExpected1,
                blocoInput1
            )
            assertNotEquals(
                "Falha ao comparar blocos de unidades diferentes com o mesmo nome: '$expected'",
                blocoExpected2,
                blocoInput2
            )
            assertEquals(
                "Falha ao comparar hashCode de blocos com nomes normalizados: '$input' e '$expected'",
                blocoExpected1.hashCode(),
                blocoInput1.hashCode()
            )
            assertEquals(
                "Falha ao normalizar o nome do bloco usando setter para o input: '$input'",
                expected,
                blocoAny.nome
            )
        }
        assertNotEquals(
            "Falha ao comparar blocos diferentes com nomes diferentes",
            Bloco(Unidade.SEDE, "BLOCO 1"),
            Bloco(Unidade.SEDE, "BLOCO 2")
        )
    }

}