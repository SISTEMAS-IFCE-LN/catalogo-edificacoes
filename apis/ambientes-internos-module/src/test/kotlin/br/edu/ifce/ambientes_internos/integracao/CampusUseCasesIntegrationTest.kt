package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.application.interfaces.ICampusUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.CampusUseCases
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DataJpaTest
@Import(CampusUseCases::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para os Casos de Uso de Campus")
class CampusUseCasesIntegrationTest {

    @Autowired
    lateinit var campusUseCases: ICampusUseCases

    @Autowired
    lateinit var repoCampus: CampusRepository

    @Test
    fun `Deve criar campus normalizado quando nao existe`() {
        val campus = campusUseCases.resolverOuCriar(" limoeiro do norte ")

        assertNotNull(campus.id)
        assertEquals("LIMOEIRO DO NORTE", campus.nome)

        val persistido = repoCampus.findById(campus.id!!).orElseThrow()
        assertEquals("LIMOEIRO DO NORTE", persistido.nome)
    }

    @Test
    fun `Deve reusar campus existente com variacao do nome`() {
        val primeiro = campusUseCases.resolverOuCriar("Limoeiro do Norte")
        val segundo = campusUseCases.resolverOuCriar("LIMOEIRO DO NORTE")

        assertEquals(primeiro.id, segundo.id)
        assertEquals(1L, repoCampus.count())
    }

    @Test
    fun `Deve lancar excecao para nome em branco`() {
        val erro = assertThrows<IllegalArgumentException> {
            campusUseCases.resolverOuCriar("   ")
        }

        assertEquals("O nome do campus é inválido.", erro.message)
        assertEquals(0L, repoCampus.count())
    }

    @Test
    fun `Deve obter campus existente por id`() {
        val campus = campusUseCases.resolverOuCriar("Limoeiro do Norte")

        val encontrado = campusUseCases.obterPorId(campus.id!!)

        assertEquals(campus.id, encontrado.id)
        assertEquals("LIMOEIRO DO NORTE", encontrado.nome)
    }

    @Test
    fun `Deve lancar excecao ao obter campus inexistente por id`() {
        assertThrows<IllegalArgumentException> {
            campusUseCases.obterPorId(999L)
        }
    }

    @Test
    fun `Deve listar campi em ordem natural`() {
        listOf("UEPE", "Sede", "Cidade Alta").forEach(campusUseCases::resolverOuCriar)

        val nomes = campusUseCases.listar().map { it.nome }

        assertEquals(listOf("CIDADE ALTA", "SEDE", "UEPE"), nomes)
    }

}
