package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@DisplayName("Testes para a classe Unidade")
class UnidadeTest {

    private val campus = Campus("Limoeiro do Norte")

    @Test
    fun `Deve normalizar o nome no construtor e no setter`() {
        val unidade = Unidade(campus, "  cidade   alta ")

        assertEquals("CIDADE ALTA", unidade.nome)

        unidade.nome = "Sede"

        assertEquals("SEDE", unidade.nome)
    }

    @Test
    fun `Deve comparar unidades por campus e nome normalizado ignorando o id`() {
        val semId = Unidade(campus, "Cidade Alta")
        val comId = Unidade(Campus("LIMOEIRO DO NORTE"), "CIDADE ALTA").apply { id = 42L }

        assertEquals(
            semId,
            comId,
            "Falha ao comparar unidades com mesmo campus e nome e ids diferentes"
        )
        assertEquals(
            comId,
            semId,
            "Falha ao comparar unidades com mesmo campus e nome e ids diferentes (ordem inversa)"
        )
        assertEquals(
            semId.hashCode(),
            comId.hashCode(),
            "Falha ao comparar hashCode de unidades com mesmo campus e nome e ids diferentes"
        )
    }

    @Test
    fun `Nao deve considerar iguais unidades com o mesmo nome em campi diferentes`() {
        assertNotEquals(
            Unidade(campus, "Sede"),
            Unidade(Campus("Outro Campus"), "Sede")
        )
    }

    @Test
    fun `Nao deve considerar iguais unidades com nomes diferentes no mesmo campus`() {
        assertNotEquals(
            Unidade(campus, "Sede"),
            Unidade(campus, "Cidade Alta")
        )
    }
}
