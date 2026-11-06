package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Auditorio
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaAdministrativa
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.data.domain.PageRequest
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

    @Autowired
    lateinit var repoBloco: BlocoRepository

    @Autowired
    lateinit var repoCampus: CampusRepository

    @Autowired
    lateinit var repoUnidade: UnidadeRepository

    private lateinit var limoeiro: Campus
    private lateinit var outroCampus: Campus
    private lateinit var sede: Unidade
    private lateinit var cidadeAlta: Unidade
    private lateinit var uepe: Unidade

    @BeforeEach
    fun setUp() {
        limoeiro = criarCampus(repoCampus, "LIMOEIRO DO NORTE")
        outroCampus = criarCampus(repoCampus, "FORTALEZA")
        sede = criarUnidade(repoCampus, repoUnidade, "LIMOEIRO DO NORTE", "SEDE")
        cidadeAlta = criarUnidade(repoCampus, repoUnidade, "LIMOEIRO DO NORTE", "CIDADE ALTA")
        uepe = criarUnidade(repoCampus, repoUnidade, "FORTALEZA", "UEPE")
    }

    private fun bloco(unidade: Unidade, nome: String): Bloco = criarBloco(repoBloco, unidade, nome)

    private fun filtro(
        campusId: Long? = null,
        unidadeId: Long? = null,
        blocoId: Long? = null,
        andar: Int? = null,
        pageable: PageRequest
    ) = repository.findByLocalizacaoAndStatus(
        campusId = campusId,
        unidadeId = unidadeId,
        blocoId = blocoId,
        filtrarCampus = campusId != null,
        filtrarUnidade = unidadeId != null,
        filtrarBloco = blocoId != null,
        andar = andar,
        status = StatusAmbiente.NAO_PUBLICADO,
        pageable = pageable
    )

    @Test
    fun `Deve salvar tres ambientes distintos e retornar id e tipo corretos`() {
        // Dados
        val salaAula = SalaAula(
            nome = "Sala A 101",
            localizacao = Localizacao(bloco(cidadeAlta, "BLOCO PRINCIPAL"), 0),
            capacidade = 30
        )

        // adicionar geometrias, esquadrias e pesDireitos na salaAula
        val geom1 = Retangular(base = BigDecimal("5.00"), altura = BigDecimal("4.00"), repeticao = 1)
        val jan = Janela(geometria = geom1, material = MaterialEsquadria.ALUMINIO, alturaPeitoril = BigDecimal("0.90"))
        val porta = Porta(
            geometria = Retangular(base = BigDecimal("0.90"), altura = BigDecimal("2.00"), repeticao = 1),
            material = MaterialEsquadria.MADEIRA_MACICA
        )

        salaAula.adicionarGeometria(geom1)
        salaAula.adicionarEsquadria(jan)
        salaAula.adicionarEsquadria(porta)
        salaAula.pesDireitos.add(BigDecimal("3.00"))

        val salaAdm = SalaAdministrativa(
            nome = "Sala Adm 1",
            localizacao = Localizacao(bloco(sede, "BLOCO 4"), 0),
            capacidade = 4
        )

        val auditorio = Auditorio(
            nome = "Auditório Central",
            localizacao = Localizacao(bloco(sede, "BLOCO 5"), 0),
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

        // Então - relacionamento bidirecional deve estar consistente
        assertEquals(savedSalaAula.id, savedSalaAula.geometrias.first().ambiente.id)
        assertEquals(true, savedSalaAula.esquadrias.all { it.ambiente.id == savedSalaAula.id })
    }

    @Test
    fun `Deve recuperar itens do banco de dados com id, tipo e relacionamentos corretos`() {
        // Dados
        val salaAula = SalaAula(
            nome = "Sala A 102",
            localizacao = Localizacao(bloco(cidadeAlta, "BLOCO PRINCIPAL"), 0),
            capacidade = 25
        )

        val geom = Retangular(base = BigDecimal("6.10"), altura = BigDecimal("5.30"), repeticao = 1)
        val jan = Janela(geometria = geom, material = MaterialEsquadria.ALUMINIO, alturaPeitoril = BigDecimal("0.90"))
        salaAula.adicionarGeometria(geom)
        salaAula.adicionarEsquadria(jan)
        salaAula.pesDireitos.add(BigDecimal("3.50"))

        val salaAdm = SalaAdministrativa(
            nome = "Sala Adm 2",
            localizacao = Localizacao(bloco(sede, "BLOCO 4"), 0),
            capacidade = 3
        )

        val auditorio = Auditorio(
            nome = "Auditório Secundário",
            localizacao = Localizacao(bloco(sede, "BLOCO 5"), 0),
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

        // Então - relacionamento bidirecional deve estar consistente
        assertEquals(found1.id, found1.geometrias.first().ambiente.id)
        assertEquals(true, found1.esquadrias.all { it.ambiente.id == found1.id })

        // Então - igualdade entre entidade recuperada e o objeto original
        assertEquals(salaAula, found1)
        assertEquals(salaAdm, found2)
        assertEquals(auditorio, found3)
    }

    @Test
    fun `Deve filtrar ambientes por campus unidade bloco e andar na consulta por localizacao`() {
        // Dados - hierarquia completa: campus -> unidade -> bloco
        val blocoPrincipal = bloco(cidadeAlta, "BLOCO PRINCIPAL")
        val bloco4 = bloco(sede, "BLOCO 4")
        val blocoUepe = bloco(uepe, "BLOCO 1")

        repository.save(
            SalaAula(
                nome = "Sala Cidade Alta",
                localizacao = Localizacao(blocoPrincipal, 1),
                capacidade = 30
            )
        )
        repository.save(
            SalaAula(
                nome = "Sala Sede",
                localizacao = Localizacao(bloco4, 2),
                capacidade = 20
            )
        )
        repository.save(
            SalaAula(
                nome = "Sala UEPE",
                localizacao = Localizacao(blocoUepe, 1),
                capacidade = 15
            )
        )

        val pageable = PageRequest.of(0, 10)

        // Filtro por campus (dois campi no total)
        val porCampus = filtro(
            campusId = limoeiro.id,
            unidadeId = null,
            blocoId = null,
            andar = null,
            pageable = pageable
        )
        assertEquals(2, porCampus.totalElements)
        assertEquals(setOf("Sala Cidade Alta", "Sala Sede"), porCampus.content.map { it.nome }.toSet())

        // Filtro por unidade - derivada de localizacao.bloco.unidade
        val porUnidade = filtro(
            campusId = null,
            unidadeId = sede.id,
            blocoId = null,
            andar = null,
            pageable = pageable
        )
        assertEquals(1, porUnidade.totalElements)
        assertEquals("Sala Sede", porUnidade.content.first().nome)
        assertEquals("LIMOEIRO DO NORTE", porUnidade.content.first().localizacao.bloco.unidade.campus.nome)

        // Filtro por bloco
        val porBloco = filtro(
            campusId = null,
            unidadeId = null,
            blocoId = blocoPrincipal.id,
            andar = null,
            pageable = pageable
        )
        assertEquals(1, porBloco.totalElements)
        assertEquals("Sala Cidade Alta", porBloco.content.first().nome)
        assertEquals("CIDADE ALTA", porBloco.content.first().localizacao.bloco.unidade.nome)

        // Filtro por andar
        val porAndar = filtro(
            campusId = null,
            unidadeId = null,
            blocoId = null,
            andar = 2,
            pageable = pageable
        )
        assertEquals(1, porAndar.totalElements)
        assertEquals("Sala Sede", porAndar.content.first().nome)

        // Combinação inconsistente (campus x unidade) -> página vazia
        val divergente = filtro(
            campusId = outroCampus.id,
            unidadeId = sede.id,
            blocoId = null,
            andar = null,
            pageable = pageable
        )
        assertEquals(0, divergente.totalElements)
    }

    @Test
    fun `Deve retornar pagina vazia quando os ids de localizacao nao existem`() {
        repository.save(
            SalaAula(
                nome = "Sala Existente",
                localizacao = Localizacao(bloco(sede, "BLOCO 1"), 1),
                capacidade = 20
            )
        )

        val pageable = PageRequest.of(0, 10)
        listOf(
            filtro(
                campusId = Long.MAX_VALUE,
                unidadeId = null,
                blocoId = null,
                andar = null,
                    pageable = pageable
            ),
            filtro(
                campusId = null,
                unidadeId = Long.MAX_VALUE,
                blocoId = null,
                andar = null,
                    pageable = pageable
            ),
            filtro(
                campusId = null,
                unidadeId = null,
                blocoId = Long.MAX_VALUE,
                andar = null,
                    pageable = pageable
            )
        ).forEach { page ->
            assertEquals(0, page.totalElements)
            assertTrue(page.content.isEmpty())
        }
    }

}
