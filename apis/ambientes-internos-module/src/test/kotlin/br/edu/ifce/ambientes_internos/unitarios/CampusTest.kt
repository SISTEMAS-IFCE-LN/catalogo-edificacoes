package br.edu.ifce.ambientes_internos.unitarios

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@DisplayName("Testes para a classe Campus")
class CampusTest {

    @Test
    fun `Deve normalizar o nome no construtor e no setter`() {
        val campus = Campus("  limoeiro do norte ")

        assertEquals("LIMOEIRO DO NORTE", campus.nome)

        campus.nome = "Cidade Alta"

        assertEquals("CIDADE ALTA", campus.nome)
    }

    @Test
    fun `Deve comparar campi pelo nome normalizado ignorando o id`() {
        val semId = Campus("Limoeiro do Norte")
        val comId = Campus("LIMOEIRO DO NORTE").apply { id = 42L }

        assertEquals(
            semId,
            comId,
            "Falha ao comparar campi com mesmo nome e ids diferentes"
        )
        assertEquals(
            comId,
            semId,
            "Falha ao comparar campi com mesmo nome e ids diferentes (ordem inversa)"
        )
        assertEquals(
            semId.hashCode(),
            comId.hashCode(),
            "Falha ao comparar hashCode de campi com mesmo nome e ids diferentes"
        )
    }

    @Test
    fun `Nao deve considerar iguais campi com nomes diferentes`() {
        assertNotEquals(
            Campus("Limoeiro do Norte"),
            Campus("Cidade Alta")
        )
    }
}
