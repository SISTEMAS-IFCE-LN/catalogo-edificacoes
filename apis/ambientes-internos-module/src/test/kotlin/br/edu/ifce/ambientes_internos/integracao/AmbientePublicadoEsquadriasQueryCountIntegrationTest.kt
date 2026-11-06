package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.TestApplication
import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbientePublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteValidacaoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.enums.TipoGeometria
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaReq
import jakarta.persistence.EntityManagerFactory
import org.hibernate.SessionFactory
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Caminho do UC20: [IAmbientePublicadoUseCases.listarEsquadriasAmbientes] usa
 * `findAllByIdInAndStatus` paginado, cujo `@EntityGraph` cobre
 * `localizacao -> bloco -> unidade -> campus`.
 *
 * O teste mede os statements com 4 e depois com 12 ambientes da mesma página e prova a
 * **invariância**: o número de statements não cresce com N. As coleções lazy de
 * `Ambiente` (`esquadrias`, `geometrias`, `pesDireitos`) usam `@BatchSize`, então a
 * contagem absoluta depende desses lotes; o que importa aqui é não acompanhar N.
 */
@SpringBootTest(classes = [TestApplication::class])
@ActiveProfiles("test")
@DisplayName("Testes de contagem de consultas da listagem de esquadrias dos ambientes publicados")
class AmbientePublicadoEsquadriasQueryCountIntegrationTest {

    @Autowired
    lateinit var naoPublicadoUseCases: IAmbienteNaoPublicadoUseCases

    @Autowired
    lateinit var validacaoUseCases: IAmbienteValidacaoUseCases

    @Autowired
    lateinit var publicadoUseCases: IAmbientePublicadoUseCases

    @Autowired
    lateinit var emf: EntityManagerFactory

    private fun requisicao(nome: String): AmbienteReq =
        AmbienteReq(
            tipo = TipoAmbiente.SALA_AULA,
            nome = nome,
            localizacao = localizacaoReqPorNome(
                campus = "LIMOEIRO DO NORTE",
                unidade = "CIDADE ALTA",
                bloco = "BLOCO 10",
                andar = 0
            ),
            capacidade = 30,
            geometrias = setOf(
                GeometriaAmbienteReq(
                    tipo = TipoGeometria.RETANGULAR,
                    base = BigDecimal("6.0"),
                    altura = BigDecimal("3.0")
                )
            ),
            pesDireitos = setOf(BigDecimal("3.0")),
            esquadrias = setOf(
                EsquadriaReq(
                    tipo = TipoEsquadria.PORTA,
                    geometria = GeometriaEsquadriaReq(base = BigDecimal("0.9"), altura = BigDecimal("2.1")),
                    material = MaterialEsquadria.MADEIRA_FICHA
                )
            ),
            informacaoAdicional = ""
        )

    private fun criarAmbientePublicado(nome: String): Long {
        val ambiente = naoPublicadoUseCases.cadastrarAmbiente(requisicao(nome))
        naoPublicadoUseCases.enviarValidacaoAmbientes(setOf(ambiente.id))
        validacaoUseCases.publicarAmbiente(ambiente.id)
        return ambiente.id
    }

    private data class Medicao(
        val statements: Long,
        val ambientes: Int,
        val totalElements: Long,
        val esquadriasPorAmbiente: List<Int>
    )

    private fun medirStatements(ids: Set<Long>): Medicao {
        val stats = emf.unwrap(SessionFactory::class.java).statistics
        stats.isStatisticsEnabled = true
        stats.clear()

        val resultado = publicadoUseCases.listarEsquadriasAmbientes(ids, PageRequest.of(0, 20))

        return Medicao(
            statements = stats.prepareStatementCount,
            ambientes = resultado.ambientes.size,
            totalElements = resultado.dadosPaginacao.totalElements,
            esquadriasPorAmbiente = resultado.ambientes.map { it.detalhesEsquadrias.esquadrias.size }
        )
    }

    @Test
    fun `listagem de esquadrias dos ambientes publicados nao faz N+1`() {
        // given: 4 ambientes publicados
        val idsPequeno = (1..4).map { indice ->
            criarAmbientePublicado("Sala Esquadria N+1 $indice")
        }.toSet()

        // when: mede a mesma página com 4 ambientes
        val medicaoPequena = medirStatements(idsPequeno)

        // and: acrescenta 8 ambientes publicados (total 12, ainda na mesma página)
        val idsGrande = idsPequeno + (5..12).map { indice ->
            criarAmbientePublicado("Sala Esquadria N+1 $indice")
        }.toSet()
        val medicaoGrande = medirStatements(idsGrande)

        // then: a medição realmente percorreu os ambientes e carregou as esquadrias
        // (sem isso a contagem seria vazia e não provaria nada)
        assertEquals(4, medicaoPequena.ambientes)
        assertEquals(4L, medicaoPequena.totalElements)
        assertEquals(listOf(1, 1, 1, 1), medicaoPequena.esquadriasPorAmbiente)
        assertEquals(12, medicaoGrande.ambientes)
        assertEquals(12L, medicaoGrande.totalElements)
        assertEquals(List(12) { 1 }, medicaoGrande.esquadriasPorAmbiente)

        // then: o número de statements não cresce com a quantidade de ambientes.
        // Com N+1 real, 12 ambientes produziriam bem mais statements que 4.
        assertTrue(
            medicaoGrande.statements <= medicaoPequena.statements,
            "Esperado invariância (12 ambientes <= 4 ambientes), mas foram " +
                "${medicaoGrande.statements} e ${medicaoPequena.statements} statements"
        )
    }
}
