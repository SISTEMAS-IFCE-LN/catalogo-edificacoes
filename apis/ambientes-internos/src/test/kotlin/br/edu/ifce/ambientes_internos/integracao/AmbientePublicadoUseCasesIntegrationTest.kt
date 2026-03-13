package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbientePublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.AmbientePublicadoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.LaboratorioInformatica
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DataJpaTest
@Import(AmbientePublicadoUseCases::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para os Casos de Uso dos Ambientes Publicados")
class AmbientePublicadoUseCasesIntegrationTest {

    @Autowired
    lateinit var ambientesPUseCases: IAmbientePublicadoUseCases

    @Autowired
    lateinit var repoAmb: AmbienteRepository

    // -------------------------------------------------------------------------
    // Helpers de criação de entidades
    // -------------------------------------------------------------------------

    private fun criarSalaAula(
        nome: String,
        bloco: Bloco,
        unidade: Unidade,
        andar: Int,
        capacidade: Int,
        base: String,
        altura: String,
        esquadrias: MutableSet<Esquadria> = mutableSetOf(),
        informacaoAdicional: String = ""
    ): SalaAula {
        val sala = SalaAula(
            nome = nome,
            localizacao = Localizacao(bloco = bloco, unidade = unidade, andar = andar),
            capacidade = capacidade,
            geometrias = mutableSetOf(Retangular(base = BigDecimal(base), altura = BigDecimal(altura)) as Geometria),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            esquadrias = esquadrias,
            informacaoAdicional = informacaoAdicional
        )
        sala.status = StatusAmbiente.PUBLICADO
        return repoAmb.save(sala)
    }

    private fun criarLaboratorioInformatica(
        nome: String,
        bloco: Bloco,
        unidade: Unidade,
        andar: Int,
        capacidade: Int,
        base: String,
        altura: String,
        informacaoAdicional: String = ""
    ): LaboratorioInformatica {
        val lab = LaboratorioInformatica(
            nome = nome,
            localizacao = Localizacao(bloco = bloco, unidade = unidade, andar = andar),
            capacidade = capacidade,
            geometrias = mutableSetOf(Retangular(base = BigDecimal(base), altura = BigDecimal(altura)) as Geometria),
            esquadrias = mutableSetOf(),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = informacaoAdicional
        )
        lab.status = StatusAmbiente.PUBLICADO
        return repoAmb.save(lab)
    }

    // -------------------------------------------------------------------------
    // Testes
    // -------------------------------------------------------------------------

    @Test
    fun `Deve obter ambiente publicado por id`() {
        val ambienteSalvo = criarSalaAula(
            nome = "Sala de Aula Publicada",
            bloco = Bloco.BLOCO_10, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "6.0", altura = "3.0",
            esquadrias = mutableSetOf(
                Janela(
                    geometria = Retangular(base = BigDecimal("1.5"), altura = BigDecimal("1.2")),
                    material = MaterialEsquadria.ALUMINIO,
                    alturaPeitoril = BigDecimal("0.90"),
                    informacaoAdicional = "Janela de correr"
                )
            ),
            informacaoAdicional = "Sala equipada com projetor e quadro branco."
        )

        // Quando o ambiente publicado for recuperado pelo seu ID
        val ambienteRecuperado = ambientesPUseCases.obterAmbientePorId(ambienteSalvo.id!!)

        // Então o ambiente recuperado deve ter status PUBLICADO e dados corretos
        assertEquals(ambienteSalvo.nome, ambienteRecuperado.nome)
        assertEquals(StatusAmbiente.PUBLICADO, ambienteRecuperado.status)
        assertEquals(ambienteSalvo.capacidade, ambienteRecuperado.capacidade)
    }

    @Test
    fun `Deve lancar excecao ao obter ambiente nao publicado por id`() {
        val ambienteNaoPublicado = SalaAula(
            nome = "Sala Não Publicada",
            localizacao = Localizacao(bloco = Bloco.BLOCO_10, unidade = Unidade.CIDADE_ALTA, andar = 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            esquadrias = mutableSetOf(),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Sala não publicada"
        )
        val ambienteSalvo = repoAmb.save(ambienteNaoPublicado)

        // Quando tentar obter um ambiente com status NAO_PUBLICADO
        assertThrows<NoSuchElementException> {
            ambientesPUseCases.obterAmbientePorId(ambienteSalvo.id!!)
        }
    }

    @Test
    fun `Deve listar ambientes publicados com paginacao`() {
        criarSalaAula(
            nome = "Sala 1", bloco = Bloco.BLOCO_10, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "6.0", altura = "3.0"
        )
        criarSalaAula(
            nome = "Sala 2", bloco = Bloco.BLOCO_11, unidade = Unidade.CIDADE_ALTA, andar = 2,
            capacidade = 35, base = "7.0", altura = "4.0"
        )

        // Quando os ambientes publicados forem listados com paginação
        val resultado = ambientesPUseCases.listarAmbientes(PageRequest.of(0, 10))

        // Então a lista deve conter todos os ambientes publicados
        assertTrue(resultado.ambientes.isNotEmpty())
        assertEquals(2, resultado.dadosPaginacao.totalElements)
        assertEquals(1, resultado.dadosPaginacao.totalPages)
        assertEquals(0, resultado.dadosPaginacao.currentPage)
        assertEquals(false, resultado.dadosPaginacao.hasNext)
        assertEquals(false, resultado.dadosPaginacao.hasPrevious)
    }

    @Test
    fun `Deve retornar lista vazia ao listar ambientes publicados quando nao ha registros`() {
        // Quando os ambientes publicados forem listados (nenhum salvo)
        val resultado = ambientesPUseCases.listarAmbientes(PageRequest.of(0, 10))

        // Então a lista deve estar vazia
        assertTrue(resultado.ambientes.isEmpty())
        assertEquals(0, resultado.dadosPaginacao.totalElements)
        assertEquals(0, resultado.dadosPaginacao.totalPages)
    }

    @Test
    fun `Deve listar ambientes publicados por tipo com paginacao`() {
        criarSalaAula(
            nome = "Sala de Aula 1", bloco = Bloco.BLOCO_10, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "6.0", altura = "3.0", informacaoAdicional = "Sala de aula"
        )
        criarLaboratorioInformatica(
            nome = "Laboratório 1", bloco = Bloco.BLOCO_11, unidade = Unidade.CIDADE_ALTA, andar = 2,
            capacidade = 40, base = "8.0", altura = "4.0", informacaoAdicional = "Laboratório"
        )

        // Quando listar por tipo
        val resultado = ambientesPUseCases.listarAmbientesPorTipo(TipoAmbiente.SALA_AULA.name, PageRequest.of(0, 10))

        // Então devem estar apenas ambientes do tipo solicitado
        assertEquals(1, resultado.dadosPaginacao.totalElements)
        assertEquals("Sala de Aula 1", resultado.ambientes.first().nome)
    }

    @Test
    fun `Deve retornar lista vazia ao listar ambientes publicados por tipo sem correspondencia`() {
        criarSalaAula(
            nome = "Sala Publicada", bloco = Bloco.BLOCO_12, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "6.0", altura = "3.0"
        )

        // Quando filtrar por um tipo sem correspondência
        val resultado = ambientesPUseCases.listarAmbientesPorTipo(
            TipoAmbiente.LABORATORIO_INFORMATICA.name,
            PageRequest.of(0, 10)
        )

        // Então a lista deve estar vazia e sem área acumulada
        assertTrue(resultado.ambientes.isEmpty())
        assertEquals(0, resultado.dadosPaginacao.totalElements)
        assertEquals(0, resultado.areaTotal.compareTo(BigDecimal.ZERO))
    }

    @Test
    fun `Deve calcular area total ao filtrar ambientes publicados por tipo`() {
        criarSalaAula(
            nome = "Sala 1", bloco = Bloco.BLOCO_10, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "5.0", altura = "4.0", informacaoAdicional = "Sala 1"
        )
        criarSalaAula(
            nome = "Sala 2", bloco = Bloco.BLOCO_11, unidade = Unidade.CIDADE_ALTA, andar = 2,
            capacidade = 35, base = "3.0", altura = "3.0", informacaoAdicional = "Sala 2"
        )
        criarLaboratorioInformatica(
            nome = "Laboratório", bloco = Bloco.BLOCO_3, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 20, base = "10.0", altura = "2.0", informacaoAdicional = "Laboratório"
        )

        // Quando listar apenas salas de aula
        val resultado = ambientesPUseCases.listarAmbientesPorTipo(TipoAmbiente.SALA_AULA.name, PageRequest.of(0, 10))

        // Então a área total considera somente o tipo filtrado (5*4 + 3*3 = 29)
        assertEquals(BigDecimal("29.00"), resultado.areaTotal)
        assertEquals(2, resultado.dadosPaginacao.totalElements)
    }

    @ParameterizedTest(name = "Filtro por nome ''{0}'' deve retornar {1} item(ns)")
    @MethodSource("filtrosNomePublicados")
    fun `Deve filtrar ambientes publicados por nome em cenarios relevantes`(
        filtroNome: String,
        totalEsperado: Int,
        nomeEsperado: String
    ) {
        criarSalaAula(
            nome = "Sala de Aula Publicada", bloco = Bloco.BLOCO_10, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "6.0", altura = "3.0"
        )
        criarSalaAula(
            nome = "Sala de Laboratório", bloco = Bloco.BLOCO_11, unidade = Unidade.CIDADE_ALTA, andar = 2,
            capacidade = 25, base = "5.0", altura = "3.0", informacaoAdicional = "Sala de laboratório"
        )

        val resultado = ambientesPUseCases.listarAmbientesPorNome(filtroNome, PageRequest.of(0, 10))

        assertEquals(totalEsperado.toLong(), resultado.dadosPaginacao.totalElements)
        if (totalEsperado == 0) {
            assertTrue(resultado.ambientes.isEmpty())
        } else {
            assertEquals(nomeEsperado, resultado.ambientes.first().nome)
        }
    }

    @ParameterizedTest(name = "Filtro por localizacao ''{0}'' deve retornar {1} item(ns)")
    @MethodSource("filtrosLocalizacaoPublicados")
    fun `Deve filtrar ambientes publicados por localizacao em cenarios relevantes`(
        filtroLocalizacao: LocalizacaoPesquisaReq,
        totalEsperado: Int,
        nomeEsperado: String
    ) {
        criarSalaAula(
            nome = "Sala de Aula Publicada", bloco = Bloco.BLOCO_10, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "6.0", altura = "3.0"
        )
        criarSalaAula(
            nome = "Sala de Aula em Outro Bloco", bloco = Bloco.BLOCO_11, unidade = Unidade.CIDADE_ALTA, andar = 2,
            capacidade = 35, base = "7.0", altura = "4.0", informacaoAdicional = "Sala em outro bloco"
        )

        val resultado = ambientesPUseCases.listarAmbientesPorLocalizacao(filtroLocalizacao, PageRequest.of(0, 10))

        assertEquals(totalEsperado.toLong(), resultado.dadosPaginacao.totalElements)
        if (totalEsperado == 0) {
            assertTrue(resultado.ambientes.isEmpty())
        } else {
            assertEquals(nomeEsperado, resultado.ambientes.first().nome)
        }
    }

    companion object {
        @JvmStatic
        fun filtrosNomePublicados(): Stream<Arguments> = Stream.of(
            Arguments.of("Sala de Aula", 1, "Sala de Aula Publicada"),
            Arguments.of("sala de aula", 1, "Sala de Aula Publicada"),
            Arguments.of("Nome Inexistente", 0, "")
        )

        @JvmStatic
        fun filtrosLocalizacaoPublicados(): Stream<Arguments> = Stream.of(
            Arguments.of(
                LocalizacaoPesquisaReq(bloco = "BLOCO_10", unidade = null, andar = null),
                1,
                "Sala de Aula Publicada"
            ),
            Arguments.of(
                LocalizacaoPesquisaReq(bloco = "bloco 10", unidade = null, andar = null),
                1,
                "Sala de Aula Publicada"
            ),
            Arguments.of(LocalizacaoPesquisaReq(bloco = "LOCALIZACAO_INEXISTENTE", unidade = null, andar = null), 0, "")
        )
    }

    @Test
    fun `Deve incluir area total nos resultados de listagem`() {
        criarSalaAula(
            nome = "Sala 1", bloco = Bloco.BLOCO_10, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "6.0", altura = "3.0"
        )
        criarSalaAula(
            nome = "Sala 2", bloco = Bloco.BLOCO_11, unidade = Unidade.CIDADE_ALTA, andar = 2,
            capacidade = 35, base = "4.0", altura = "3.0", informacaoAdicional = "Segunda sala"
        )

        // Quando listar ambientes
        val resultado = ambientesPUseCases.listarAmbientes(PageRequest.of(0, 10))

        // Então a área total deve ser calculada corretamente (6*3 + 4*3 = 18 + 12 = 30)
        assertEquals(BigDecimal("30.00"), resultado.areaTotal)
    }

    @Test
    fun `Deve incluir area total em filtro por tipo`() {
        criarSalaAula(
            nome = "Sala 1", bloco = Bloco.BLOCO_10, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "5.0", altura = "4.0", informacaoAdicional = "Sala 1"
        )
        criarLaboratorioInformatica(
            nome = "Laboratório 2", bloco = Bloco.BLOCO_11, unidade = Unidade.CIDADE_ALTA, andar = 2,
            capacidade = 35, base = "3.0", altura = "3.0", informacaoAdicional = "Lab"
        )

        // Quando listar apenas laboratórios
        val resultado = ambientesPUseCases.listarAmbientesPorTipo(
            TipoAmbiente.LABORATORIO_INFORMATICA.name,
            PageRequest.of(0, 10)
        )

        // Então a área total deve refletir apenas o tipo filtrado (3*3 = 9)
        assertEquals(BigDecimal("9.00"), resultado.areaTotal)
        assertEquals(1, resultado.dadosPaginacao.totalElements)
    }

    @Test
    fun `Deve validar paginacao em listagem por nome`() {
        for (i in 1..15) {
            criarSalaAula(
                nome = "Sala de Aula $i", bloco = Bloco.BLOCO_1, unidade = Unidade.SEDE, andar = i,
                capacidade = 30, base = "6.0", altura = "3.0", informacaoAdicional = "Sala $i"
            )
        }

        // Quando listar primeira página
        val resultado1 = ambientesPUseCases.listarAmbientesPorNome("Sala de Aula", PageRequest.of(0, 5))

        assertEquals(5, resultado1.ambientes.size)
        assertEquals(15, resultado1.dadosPaginacao.totalElements)
        assertEquals(3, resultado1.dadosPaginacao.totalPages)
        assertEquals(true, resultado1.dadosPaginacao.hasNext)
        assertEquals(false, resultado1.dadosPaginacao.hasPrevious)

        // Quando listar segunda página
        val resultado2 = ambientesPUseCases.listarAmbientesPorNome("Sala de Aula", PageRequest.of(1, 5))

        assertEquals(5, resultado2.ambientes.size)
        assertEquals(1, resultado2.dadosPaginacao.currentPage)
        assertEquals(true, resultado2.dadosPaginacao.hasNext)
        assertEquals(true, resultado2.dadosPaginacao.hasPrevious)
    }

    @Test
    fun `Deve nao listar ambientes nao publicados em nenhum filtro`() {
        criarSalaAula(
            nome = "Sala de Aula Publicada", bloco = Bloco.BLOCO_10, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "6.0", altura = "3.0"
        )
        repoAmb.save(
            SalaAula(
                nome = "Sala Não Publicada",
                localizacao = Localizacao(bloco = Bloco.BLOCO_2, unidade = Unidade.SEDE, andar = 1),
                capacidade = 30,
                geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
                esquadrias = mutableSetOf(),
                pesDireitos = mutableSetOf(BigDecimal("3.0")),
                informacaoAdicional = "Sala não publicada"
            )
        )

        val pageable = PageRequest.of(0, 10)

        // Então não deve incluir ambiente não publicado em nenhum filtro
        assertEquals(1, ambientesPUseCases.listarAmbientes(pageable).dadosPaginacao.totalElements)
        assertEquals(1, ambientesPUseCases.listarAmbientesPorNome("Sala", pageable).dadosPaginacao.totalElements)
        assertEquals(
            1,
            ambientesPUseCases.listarAmbientesPorLocalizacao(
                LocalizacaoPesquisaReq(bloco = "BLOCO_10", unidade = null, andar = null),
                pageable
            ).dadosPaginacao.totalElements
        )
    }

    @Test
    fun `Deve listar esquadrias dos ambientes publicados com paginacao`() {
        val ambiente1 = criarSalaAula(
            nome = "Ambiente Publicado 1", bloco = Bloco.BLOCO_1, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 20, base = "4.0", altura = "3.0",
            esquadrias = mutableSetOf(
                Porta(
                    geometria = Retangular(base = BigDecimal("0.9"), altura = BigDecimal("2.1")),
                    material = MaterialEsquadria.MADEIRA_FICHA
                ),
                Janela(
                    geometria = Retangular(base = BigDecimal("1.0"), altura = BigDecimal("1.0")),
                    material = MaterialEsquadria.ALUMINIO,
                    alturaPeitoril = BigDecimal("0.90")
                )
            )
        )
        val ambiente2 = criarSalaAula(
            nome = "Ambiente Publicado 2", bloco = Bloco.BLOCO_2, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 25, base = "5.0", altura = "3.0",
            esquadrias = mutableSetOf(
                Porta(
                    geometria = Retangular(base = BigDecimal("0.9"), altura = BigDecimal("2.1")),
                    material = MaterialEsquadria.MADEIRA_FICHA
                ),
                Janela(
                    geometria = Retangular(base = BigDecimal("1.5"), altura = BigDecimal("1.2")),
                    material = MaterialEsquadria.ALUMINIO,
                    alturaPeitoril = BigDecimal("0.90")
                )
            )
        )
        val ambiente3 = criarSalaAula(
            nome = "Ambiente Publicado 3", bloco = Bloco.BLOCO_3, unidade = Unidade.CIDADE_ALTA, andar = 1,
            capacidade = 30, base = "6.0", altura = "3.0",
            esquadrias = mutableSetOf(
                Porta(
                    geometria = Retangular(base = BigDecimal("0.9"), altura = BigDecimal("2.1")),
                    material = MaterialEsquadria.MADEIRA_FICHA
                ),
                Janela(
                    geometria = Retangular(base = BigDecimal("2.0"), altura = BigDecimal("1.5")),
                    material = MaterialEsquadria.ALUMINIO,
                    alturaPeitoril = BigDecimal("0.90")
                )
            )
        )

        val resultado = ambientesPUseCases.listarEsquadriasAmbientes(
            ids = setOf(ambiente1.id!!, ambiente2.id!!, ambiente3.id!!),
            pageable = PageRequest.of(0, 2)
        )

        // Validar paginação
        assertEquals(3, resultado.dadosPaginacao.totalElements)
        assertEquals(2, resultado.dadosPaginacao.totalPages)
        assertEquals(0, resultado.dadosPaginacao.currentPage)
        assertEquals(2, resultado.dadosPaginacao.pageSize)
        assertEquals(true, resultado.dadosPaginacao.hasNext)
        assertEquals(false, resultado.dadosPaginacao.hasPrevious)

        // Validar conteúdo retornado (2 ambientes na primeira página)
        assertEquals(2, resultado.ambientes.size)

        val ambientesRetornados = resultado.ambientes.keys.toList()
        assertTrue(ambientesRetornados.any { it.nome == "Ambiente Publicado 1" })
        assertTrue(ambientesRetornados.any { it.nome == "Ambiente Publicado 2" })

        assertTrue(resultado.totalTipoMaterial.isNotEmpty())
        assertTrue(resultado.totalTipoMaterial.any { it.tipo == TipoEsquadria.PORTA && it.material == MaterialEsquadria.MADEIRA_FICHA })

        // Validar área total das janelas de alumínio dos 2 primeiros ambientes na paginação
        // Ambiente 1: 1.0 * 1.0 = 1.0 m²  |  Ambiente 2: 1.5 * 1.2 = 1.8 m²  |  Total: 2.8 m²
        val areaJanelasAluminio =
            resultado.totalTipoMaterial.find { it.tipo == TipoEsquadria.JANELA && it.material == MaterialEsquadria.ALUMINIO }?.area
        val areaEsperada = BigDecimal("1.0").multiply(BigDecimal("1.0"))
            .add(BigDecimal("1.5").multiply(BigDecimal("1.2")))
        assertEquals(areaEsperada.setScale(2), areaJanelasAluminio?.setScale(2))
    }

    @Test
    fun `Deve limitar pagina para no maximo 100 ambientes publicados`() {
        (1..120).forEach { indice ->
            criarSalaAula(
                nome = "Sala Publicada $indice",
                bloco = Bloco.BLOCO_10,
                unidade = Unidade.CIDADE_ALTA,
                andar = indice,
                capacidade = 30,
                base = "6.0",
                altura = "3.0"
            )
        }

        val resultado = ambientesPUseCases.listarAmbientes(PageRequest.of(0, 1000))

        assertEquals(120, resultado.dadosPaginacao.totalElements)
        assertEquals(100, resultado.dadosPaginacao.pageSize)
        assertEquals(100, resultado.ambientes.size)
    }

    @Test
    fun `Deve limitar pagina para no maximo 100 ambientes ao listar esquadrias`() {
        val ids = (1..120).map { indice ->
            criarSalaAula(
                nome = "Sala com esquadria $indice",
                bloco = Bloco.BLOCO_11,
                unidade = Unidade.CIDADE_ALTA,
                andar = indice,
                capacidade = 30,
                base = "6.0",
                altura = "3.0",
                esquadrias = mutableSetOf(
                    Porta(
                        geometria = Retangular(base = BigDecimal("0.9"), altura = BigDecimal("2.1")),
                        material = MaterialEsquadria.MADEIRA_FICHA,
                        informacaoAdicional = "Porta padrão"
                    )
                )
            ).id!!
        }.toSet()

        val resultado = ambientesPUseCases.listarEsquadriasAmbientes(ids, PageRequest.of(0, 1000))

        assertEquals(120, resultado.dadosPaginacao.totalElements)
        assertEquals(100, resultado.dadosPaginacao.pageSize)
        assertEquals(100, resultado.ambientes.size)
    }
}
