package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.TestApplication
import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.enums.TipoGeometria
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
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
import kotlin.test.assertTrue

@SpringBootTest(classes = [TestApplication::class])
@ActiveProfiles("test")
@DisplayName("Testes de contagem de consultas da listagem por localização")
class AmbienteLocalizacaoQueryCountIntegrationTest {

    @Autowired
    lateinit var useCases: IAmbienteNaoPublicadoUseCases

    @Autowired
    lateinit var emf: EntityManagerFactory

    private fun requisicao(nome: String, campus: String, unidade: String, bloco: String): AmbienteReq =
        AmbienteReq(
            tipo = TipoAmbiente.SALA_AULA,
            nome = nome,
            localizacao = localizacaoReqPorNome(campus, unidade, bloco, andar = 0),
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

    @Test
    fun `listagem por localizacao nao faz N+1`() {
        // given: 12 ambientes distribuídos em 3 campi/unidades/blocos distintos
        val grupos = listOf(
            Triple("LIMOEIRO DO NORTE", "CIDADE ALTA", "BLOCO 10"),
            Triple("FORTALEZA", "UEPE", "BLOCO 20"),
            Triple("QUIXADA", "SEDE", "BLOCO 30")
        )
        grupos.forEachIndexed { grupoIndex, (campus, unidade, bloco) ->
            (1..4).forEach { indice ->
                useCases.cadastrarAmbiente(
                    requisicao(
                        nome = "Sala N+1 $grupoIndex-$indice",
                        campus = campus,
                        unidade = unidade,
                        bloco = bloco
                    )
                )
            }
        }

        // when: limpa as estatísticas e executa a listagem paginada
        val stats = emf.unwrap(SessionFactory::class.java).statistics
        stats.isStatisticsEnabled = true
        stats.clear()

        useCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(
                campusId = null,
                unidadeId = null,
                blocoId = null,
                andar = 0
            ),
            PageRequest.of(0, 20)
        )

        // then: pageable executa count + dados, mais o batch de geometrias.
        // O limite superior prova que o número não cresce com a quantidade de ambientes.
        val statements = stats.prepareStatementCount
        assertTrue(
            statements <= 5,
            "Esperado <= 5 statements (count + dados + batch), mas foram $statements"
        )
    }
}
