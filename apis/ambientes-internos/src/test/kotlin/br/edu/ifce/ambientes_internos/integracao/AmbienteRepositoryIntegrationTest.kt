package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Auditorio
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAdministrativa
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para AmbienteRepository")
class AmbienteRepositoryIntegrationTest {

    @Autowired
    lateinit var repository: AmbienteRepository

    @Test
    fun `Deve salvar tres ambientes distintos e retornar id e tipo corretos`() {
        // Dados
        val salaAula = SalaAula(
            nome = "Sala A 101",
            localizacao = Localizacao(Bloco.BLOCO_PRINCIPAL, Unidade.CIDADE_ALTA),
            capacidade = 30
        )

        // adicionar geometrias, esquadrias e pesDireitos na salaAula
        val geom1 = Retangular(base = BigDecimal("5.00"), altura = BigDecimal("4.00"), repeticao = 1)
        val jan = Janela(geometria = geom1, material = MaterialEsquadria.ALUMINIO, alturaPeitoril = BigDecimal("0.90"))
        val porta = Porta(
            geometria = Retangular(base = BigDecimal("0.90"), altura = BigDecimal("2.00"), repeticao = 1),
            material = MaterialEsquadria.MADEIRA_MACICA
        )

        salaAula.geometrias.add(geom1)
        salaAula.esquadrias.add(jan)
        salaAula.esquadrias.add(porta)
        salaAula.pesDireitos.add(BigDecimal("3.00"))

        val salaAdm = SalaAdministrativa(
            nome = "Sala Adm 1",
            localizacao = Localizacao(Bloco.BLOCO_4, Unidade.SEDE),
            capacidade = 4
        )

        val auditorio = Auditorio(
            nome = "Auditório Central",
            localizacao = Localizacao(Bloco.BLOCO_5, Unidade.SEDE),
            capacidade = 200
        )

        // Quando
        val savedSalaAula = repository.save(salaAula)
        val savedSalaAdm = repository.save(salaAdm)
        val savedAuditorio = repository.save(auditorio)

        // Então - ids gerados
        assertNotNull(savedSalaAula.id)
        assertTrue(savedSalaAula.id!! > 0)
        assertNotNull(savedSalaAdm.id)
        assertTrue(savedSalaAdm.id!! > 0)
        assertNotNull(savedAuditorio.id)
        assertTrue(savedAuditorio.id!! > 0)

        // Então - igualdade entre entidade salva e o objeto original
        assertEquals(salaAula, savedSalaAula)
        assertEquals(salaAdm, savedSalaAdm)
        assertEquals(auditorio, savedAuditorio)
    }

    @Test
    fun `Deve recuperar itens do banco de dados com id, tipo e relacionamentos corretos`() {
        // Dados
        val salaAula = SalaAula(
            nome = "Sala A 102",
            localizacao = Localizacao(Bloco.BLOCO_PRINCIPAL, Unidade.CIDADE_ALTA),
            capacidade = 25
        )

        val geom = Retangular(base = BigDecimal("6.10"), altura = BigDecimal("5.30"), repeticao = 1)
        val jan = Janela(geometria = geom, material = MaterialEsquadria.ALUMINIO, alturaPeitoril = BigDecimal("0.90"))
        salaAula.geometrias.add(geom)
        salaAula.esquadrias.add(jan)
        salaAula.pesDireitos.add(BigDecimal("3.50"))

        val salaAdm = SalaAdministrativa(
            nome = "Sala Adm 2",
            localizacao = Localizacao(Bloco.BLOCO_4, Unidade.SEDE),
            capacidade = 3
        )

        val auditorio = Auditorio(
            nome = "Auditório Secundário",
            localizacao = Localizacao(Bloco.BLOCO_5, Unidade.SEDE),
            capacidade = 120
        )

        // Quando - salvar no banco
        val saved1 = repository.save(salaAula)
        val saved2 = repository.save(salaAdm)
        val saved3 = repository.save(auditorio)

        // Quando - recuperar por id
        val found1 = repository.findById(saved1.id!!).get()
        val found2 = repository.findById(saved2.id!!).get()
        val found3 = repository.findById(saved3.id!!).get()

        // Então - igualdade entre entidade recuperada e o objeto original
        assertEquals(salaAula, found1)
        assertEquals(salaAdm, found2)
        assertEquals(auditorio, found3)
    }

}
