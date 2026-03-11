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
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.enums.TipoGeometria
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoReq
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaTipoMaterialRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaRes
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
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

    lateinit var ambienteReq: AmbienteReq
    lateinit var salaAula: SalaAula
    lateinit var ambienteRes: AmbienteRes

    @BeforeEach
    fun setup() {
        // Dados - Modelo de requisição
        ambienteReq = AmbienteReq(
            tipo = TipoAmbiente.SALA_AULA,
            nome = "Sala de Aula Publicada",
            localizacao = LocalizacaoReq(
                unidade = Unidade.CIDADE_ALTA, bloco = Bloco.BLOCO_10, andar = 1
            ), capacidade = 30, geometrias = setOf(
                GeometriaAmbienteReq(
                    tipo = TipoGeometria.RETANGULAR, base = BigDecimal("6.0"), altura = BigDecimal("3.0")
                )
            ), pesDireitos = setOf(BigDecimal("3.0")), esquadrias = setOf(
                EsquadriaReq(
                    tipo = TipoEsquadria.JANELA,
                    geometria = GeometriaEsquadriaReq(
                        base = BigDecimal("1.5"), altura = BigDecimal("1.2")
                    ),
                    material = MaterialEsquadria.ALUMINIO,
                    alturaPeitoril = BigDecimal("0.90"),
                    informacaoAdicional = "Janela de correr"
                ), EsquadriaReq(
                    tipo = TipoEsquadria.PORTA, geometria = GeometriaEsquadriaReq(
                        base = BigDecimal("0.9"), altura = BigDecimal("2.1")
                    ),
                    material = MaterialEsquadria.MADEIRA_FICHA, informacaoAdicional = "Porta de abrir"
                )
            ), informacaoAdicional = "Sala equipada com projetor e quadro branco."
        )
        // Dados - Modelo de entidade salva do banco
        salaAula = SalaAula(
            nome = ambienteReq.nome,
            localizacao = Localizacao(
                bloco = ambienteReq.localizacao.bloco,
                unidade = ambienteReq.localizacao.unidade,
                andar = ambienteReq.localizacao.andar
            ),
            capacidade = ambienteReq.capacidade,
            geometrias = ambienteReq.geometrias.map {
                Retangular(
                    base = it.base, altura = it.altura
                ) as Geometria
            }.toMutableSet(),
            esquadrias = ambienteReq.esquadrias.map {
                when (it.tipo) {
                    TipoEsquadria.JANELA -> Janela(
                        geometria = Retangular(
                            base = it.geometria.base, altura = it.geometria.altura
                        ),
                        material = it.material,
                        alturaPeitoril = it.alturaPeitoril,
                        informacaoAdicional = it.informacaoAdicional
                    )

                    TipoEsquadria.PORTA -> Porta(
                        geometria = Retangular(
                            base = it.geometria.base, altura = it.geometria.altura
                        ), material = it.material, informacaoAdicional = it.informacaoAdicional
                    )

                    else -> throw IllegalArgumentException("Tipo de esquadria desconhecido: ${it.tipo}")
                }
            }.toMutableSet(),
            pesDireitos = ambienteReq.pesDireitos.toMutableSet(),
            informacaoAdicional = ambienteReq.informacaoAdicional
        )

        // Dados - Modelo da resposta que deve ser retornada para o usuário.
        ambienteRes = AmbienteRes(
            id = 0L,
            tipo = salaAula.tipo!!,
            nome = salaAula.nome,
            localizacao = LocalizacaoRes(
                id = 0L,
                bloco = salaAula.localizacao.bloco,
                unidade = salaAula.localizacao.unidade,
                andar = salaAula.localizacao.andar
            ),
            capacidade = salaAula.capacidade,
            geometrias = salaAula.geometrias.map {
                GeometriaAmbienteRes(
                    id = 0L,
                    tipo = it.tipo,
                    base = it.base,
                    altura = it.altura,
                    repeticao = it.repeticao,
                    area = it.calcularAreaTotalM2()
                )
            },
            areaAmbiente = salaAula.calcularAreaAmbienteM2(),
            pesDireitos = salaAula.pesDireitos.toList(),
            esquadriasDetalhes = EsquadriasDetalhesRes(esquadrias = salaAula.esquadrias.map {
                EsquadriaRes(
                    id = 0L,
                    tipo = it.tipo,
                    geometria = GeometriaEsquadriaRes(
                        id = 0L,
                        base = it.geometria.base,
                        altura = it.geometria.altura,
                        repeticao = it.geometria.repeticao,
                        area = it.geometria.calcularAreaTotalM2()
                    ),
                    alturaPeitoril = if (it is Janela) it.alturaPeitoril else BigDecimal.ZERO,
                    area = it.geometria.calcularAreaTotalM2(),
                    material = it.material,
                    informacaoAdicional = it.informacaoAdicional
                )
            }, esquadriasTipoMaterial = salaAula.calcularAreaEsquadriasPorTipoMaterial().map {
                EsquadriaTipoMaterialRes(
                    tipo = it.key.first, material = it.key.second, area = it.value
                )
            }),
            informacaoAdicional = salaAula.informacaoAdicional,
            status = salaAula.status
        )
    }

    private fun criarAmbientePublicado(
        nome: String,
        localizacao: Localizacao,
        capacidade: Int,
        geometrias: MutableSet<Geometria> = mutableSetOf(),
        pesDireitos: MutableSet<BigDecimal> = mutableSetOf(),
        esquadrias: MutableSet<Esquadria> = mutableSetOf(),
        informacaoAdicional: String = ""
    ): SalaAula {
        val sala = SalaAula(
            nome = nome,
            localizacao = localizacao,
            capacidade = capacidade,
            geometrias = geometrias,
            pesDireitos = pesDireitos,
            esquadrias = esquadrias,
            informacaoAdicional = informacaoAdicional
        )
        sala.status = StatusAmbiente.PUBLICADO
        return repoAmb.save(sala)
    }

    @Test
    fun `Deve obter ambiente publicado por id`() {
        // Dado - um ambiente publicado salvo no banco
        val ambienteSalvo = criarAmbientePublicado(
            nome = "Sala de Aula Publicada",
            localizacao = Localizacao(
                bloco = Bloco.BLOCO_10,
                unidade = Unidade.CIDADE_ALTA,
                andar = 1
            ),
            capacidade = 30,
            geometrias = mutableSetOf(
                Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))
            ),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
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
        // Dado - um ambiente com status NAO_PUBLICADO
        val ambienteNaoPublicado = SalaAula(
            nome = "Sala de Aula Não Publicada",
            localizacao = Localizacao(
                bloco = Bloco.BLOCO_10,
                unidade = Unidade.CIDADE_ALTA,
                andar = 1
            ),
            capacidade = 30,
            geometrias = mutableSetOf(
                Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))
            ),
            esquadrias = mutableSetOf(),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Sala não publicada"
        )
        val ambienteSalvo = repoAmb.save(ambienteNaoPublicado)

        // Quando tentar obter um ambiente com status NAO_PUBLICADO
        // Então deve lançar NoSuchElementException
        assertThrows<NoSuchElementException> {
            ambientesPUseCases.obterAmbientePorId(ambienteSalvo.id!!)
        }
    }

    @Test
    fun `Deve listar ambientes publicados com paginacao`() {
        // Dado - múltiplos ambientes publicados salvos no banco
        criarAmbientePublicado(
            nome = "Sala de Aula 1",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
        )

        criarAmbientePublicado(
            nome = "Sala de Aula 2",
            localizacao = Localizacao(Bloco.BLOCO_11, Unidade.CIDADE_ALTA, 2),
            capacidade = 35,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("7.0"), altura = BigDecimal("4.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
        )

        // Quando os ambientes publicados forem listados com paginação
        val pageable = PageRequest.of(0, 10)
        val ambientesPublicados = ambientesPUseCases.listarAmbientes(pageable)

        // Então a lista deve conter todos os ambientes publicados
        assertTrue(ambientesPublicados.ambientes.isNotEmpty())
        assertEquals(2, ambientesPublicados.dadosPaginacao.totalElements)
        assertEquals(1, ambientesPublicados.dadosPaginacao.totalPages)
        assertEquals(0, ambientesPublicados.dadosPaginacao.currentPage)
        assertEquals(false, ambientesPublicados.dadosPaginacao.hasNext)
        assertEquals(false, ambientesPublicados.dadosPaginacao.hasPrevious)
    }

    @Test
    fun `Deve retornar lista vazia ao listar ambientes publicados quando nao ha registros`() {
        // Quando os ambientes publicados forem listados (nenhum salvo)
        val pageable = PageRequest.of(0, 10)
        val ambientesPublicados = ambientesPUseCases.listarAmbientes(pageable)

        // Então a lista deve estar vazia
        assertTrue(ambientesPublicados.ambientes.isEmpty())
        assertEquals(0, ambientesPublicados.dadosPaginacao.totalElements)
        assertEquals(0, ambientesPublicados.dadosPaginacao.totalPages)
    }

    @Test
    fun `Deve listar ambientes publicados por tipo com paginacao`() {
        // Dado - ambientes publicados de diferentes tipos
        criarAmbientePublicado(
            nome = "Sala de Aula 1",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Sala de aula"
        )

        val laboratorioInformatica = LaboratorioInformatica(
            nome = "Laboratório de Informática 1",
            localizacao = Localizacao(
                bloco = Bloco.BLOCO_11,
                unidade = Unidade.CIDADE_ALTA,
                andar = 2
            ),
            capacidade = 40,
            geometrias = mutableSetOf(
                Retangular(base = BigDecimal("8.0"), altura = BigDecimal("4.0"))
            ),
            esquadrias = mutableSetOf(),
            pesDireitos = mutableSetOf(BigDecimal("3.5")),
            informacaoAdicional = "Laboratório"
        )
        val lab = repoAmb.save(laboratorioInformatica)
        lab.status = StatusAmbiente.PUBLICADO
        repoAmb.save(lab)

        // Quando listar por tipo
        val pageable = PageRequest.of(0, 10)
        val ambientesPorTipo = ambientesPUseCases.listarAmbientesPorTipo(TipoAmbiente.SALA_AULA.name, pageable)

        // Então devem estar apenas ambientes do tipo solicitado
        assertEquals(1, ambientesPorTipo.dadosPaginacao.totalElements)
        assertEquals("Sala de Aula 1", ambientesPorTipo.ambientes.first().nome)
    }

    @Test
    fun `Deve retornar lista vazia ao listar ambientes publicados por tipo sem correspondencia`() {
        // Dado - apenas salas de aula publicadas
        criarAmbientePublicado(
            nome = "Sala de Aula Publicada",
            localizacao = Localizacao(Bloco.BLOCO_12, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
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
        // Dado - ambientes publicados de tipos diferentes
        criarAmbientePublicado(
            nome = "Sala de Aula 1",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("5.0"), altura = BigDecimal("4.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Sala 1"
        )

        criarAmbientePublicado(
            nome = "Sala de Aula 2",
            localizacao = Localizacao(Bloco.BLOCO_11, Unidade.CIDADE_ALTA, 2),
            capacidade = 35,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("3.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Sala 2"
        )

        val laboratorioInformatica = LaboratorioInformatica(
            nome = "Laboratório Publicado",
            localizacao = Localizacao(Bloco.BLOCO_3, Unidade.CIDADE_ALTA, 1),
            capacidade = 20,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("10.0"), altura = BigDecimal("2.0"))),
            esquadrias = mutableSetOf(),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Laboratório"
        )
        val lab = repoAmb.save(laboratorioInformatica)
        lab.status = StatusAmbiente.PUBLICADO
        repoAmb.save(lab)

        // Quando listar apenas salas de aula
        val resultado = ambientesPUseCases.listarAmbientesPorTipo(
            TipoAmbiente.SALA_AULA.name,
            PageRequest.of(0, 10)
        )

        // Então a área total considera somente o tipo filtrado (5*4 + 3*3 = 29)
        assertEquals(BigDecimal("29.00"), resultado.areaTotal)
        assertEquals(2, resultado.dadosPaginacao.totalElements)
    }

    @Test
    fun `Deve listar ambientes publicados por nome com paginacao`() {
        // Dado - ambientes publicados salvos
        criarAmbientePublicado(
            nome = "Sala de Aula Publicada",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
        )

        criarAmbientePublicado(
            nome = "Sala de Laboratório",
            localizacao = Localizacao(Bloco.BLOCO_11, Unidade.CIDADE_ALTA, 2),
            capacidade = 25,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("5.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Sala de laboratório"
        )

        // Quando filtrar por nome
        val pageable = PageRequest.of(0, 10)
        val ambientesPublicadosPorNome = ambientesPUseCases.listarAmbientesPorNome("Sala de Aula", pageable)

        // Então a lista deve conter apenas os ambientes que correspondem ao filtro
        assertEquals(1, ambientesPublicadosPorNome.dadosPaginacao.totalElements)
        assertEquals("Sala de Aula Publicada", ambientesPublicadosPorNome.ambientes.first().nome)
    }

    @Test
    fun `Deve listar ambientes publicados por nome com case insensitive`() {
        // Dado - ambiente publicado salvo
        criarAmbientePublicado(
            nome = "Sala de Aula Publicada",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
        )

        // Quando filtrar por nome em maiúsculas
        val pageable = PageRequest.of(0, 10)
        val ambientesPublicadosPorNome = ambientesPUseCases.listarAmbientesPorNome("sala de aula", pageable)

        // Então a lista deve conter os ambientes (case insensitive)
        assertEquals(1, ambientesPublicadosPorNome.dadosPaginacao.totalElements)
        assertEquals("Sala de Aula Publicada", ambientesPublicadosPorNome.ambientes.first().nome)
    }

    @Test
    fun `Deve retornar lista vazia ao filtrar por nome inexistente`() {
        // Dado - ambiente publicado salvo
        criarAmbientePublicado(
            nome = "Sala de Aula Publicada",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
        )

        // Quando filtrar por um nome que não existe
        val pageable = PageRequest.of(0, 10)
        val ambientesPublicadosPorNome = ambientesPUseCases.listarAmbientesPorNome("Nome Inexistente", pageable)

        // Então a lista deve estar vazia
        assertTrue(ambientesPublicadosPorNome.ambientes.isEmpty())
        assertEquals(0, ambientesPublicadosPorNome.dadosPaginacao.totalElements)
    }

    @Test
    fun `Deve listar ambientes publicados por localizacao com paginacao`() {
        // Dado - ambientes publicados em diferentes localizações
        criarAmbientePublicado(
            nome = "Sala de Aula Publicada",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
        )

        criarAmbientePublicado(
            nome = "Sala de Aula em Outro Bloco",
            localizacao = Localizacao(Bloco.BLOCO_11, Unidade.CIDADE_ALTA, 2),
            capacidade = 35,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("7.0"), altura = BigDecimal("4.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Sala em outro bloco"
        )

        // Quando filtrar por localização
        val pageable = PageRequest.of(0, 10)
        val ambientesPublicadosPorLocalizacao = ambientesPUseCases.listarAmbientesPorLocalizacao("BLOCO_10", pageable)

        // Então a lista deve conter apenas os ambientes na localização especificada
        assertEquals(1, ambientesPublicadosPorLocalizacao.dadosPaginacao.totalElements)
        assertEquals("Sala de Aula Publicada", ambientesPublicadosPorLocalizacao.ambientes.first().nome)
    }

    @Test
    fun `Deve listar ambientes publicados por localizacao com case insensitive`() {
        // Dado - ambiente publicado salvo
        criarAmbientePublicado(
            nome = "Sala de Aula Publicada",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
        )

        // Quando filtrar por BLOCO_10 (que deve encontrar a localização)
        val pageable = PageRequest.of(0, 10)
        val ambientesPublicadosPorLocalizacao = ambientesPUseCases.listarAmbientesPorLocalizacao("BLOCO_10", pageable)

        // Então a lista deve conter os ambientes
        assertEquals(1, ambientesPublicadosPorLocalizacao.dadosPaginacao.totalElements)
    }

    @Test
    fun `Deve retornar lista vazia ao filtrar por localizacao inexistente`() {
        // Dado - ambiente publicado salvo
        criarAmbientePublicado(
            nome = "Sala de Aula Publicada",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
        )

        // Quando filtrar por uma localização que não existe
        val pageable = PageRequest.of(0, 10)
        val ambientesPublicadosPorLocalizacao =
            ambientesPUseCases.listarAmbientesPorLocalizacao("LOCALIZACAO_INEXISTENTE", pageable)

        // Então a lista deve estar vazia
        assertTrue(ambientesPublicadosPorLocalizacao.ambientes.isEmpty())
        assertEquals(0, ambientesPublicadosPorLocalizacao.dadosPaginacao.totalElements)
    }

    @Test
    fun `Deve incluir area total nos resultados de listagem`() {
        // Dado - múltiplos ambientes publicados
        criarAmbientePublicado(
            nome = "Sala de Aula 1",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
        )

        criarAmbientePublicado(
            nome = "Sala de Aula 2",
            localizacao = Localizacao(Bloco.BLOCO_11, Unidade.CIDADE_ALTA, 2),
            capacidade = 35,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("4.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Segunda sala"
        )

        // Quando listar ambientes
        val pageable = PageRequest.of(0, 10)
        val resultado = ambientesPUseCases.listarAmbientes(pageable)

        // Então a área total deve ser calculada corretamente (6*3 + 4*3 = 18 + 12 = 30)
        assertEquals(BigDecimal("30.00"), resultado.areaTotal)
    }

    @Test
    fun `Deve incluir area total em filtro por tipo`() {
        // Dado - ambientes publicados de tipos diferentes
        criarAmbientePublicado(
            nome = "Sala de Aula 1",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("5.0"), altura = BigDecimal("4.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Sala 1"
        )

        val laboratorioInformatica = LaboratorioInformatica(
            nome = "Laboratório Publicado 2",
            localizacao = Localizacao(Bloco.BLOCO_11, Unidade.CIDADE_ALTA, 2),
            capacidade = 35,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("3.0"), altura = BigDecimal("3.0"))),
            esquadrias = mutableSetOf(),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Lab"
        )
        val lab = repoAmb.save(laboratorioInformatica)
        lab.status = StatusAmbiente.PUBLICADO
        repoAmb.save(lab)

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
        // Dado - múltiplos ambientes publicados com nomes similares
        for (i in 1..15) {
            criarAmbientePublicado(
                nome = "Sala de Aula $i",
                localizacao = Localizacao(Bloco.BLOCO_1, Unidade.SEDE, i),
                capacidade = 30,
                geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
                pesDireitos = mutableSetOf(BigDecimal("3.0")),
                informacaoAdicional = "Sala $i"
            )
        }

        // Quando listar primeira página
        val pageable1 = PageRequest.of(0, 5)
        val resultado1 = ambientesPUseCases.listarAmbientesPorNome("Sala de Aula", pageable1)

        // Então deve ter 5 elementos
        assertEquals(5, resultado1.ambientes.size)
        assertEquals(15, resultado1.dadosPaginacao.totalElements)
        assertEquals(3, resultado1.dadosPaginacao.totalPages)
        assertEquals(true, resultado1.dadosPaginacao.hasNext)
        assertEquals(false, resultado1.dadosPaginacao.hasPrevious)

        // Quando listar segunda página
        val pageable2 = PageRequest.of(1, 5)
        val resultado2 = ambientesPUseCases.listarAmbientesPorNome("Sala de Aula", pageable2)

        // Então deve ter 5 elementos e indicar que há próxima página
        assertEquals(5, resultado2.ambientes.size)
        assertEquals(1, resultado2.dadosPaginacao.currentPage)
        assertEquals(true, resultado2.dadosPaginacao.hasNext)
        assertEquals(true, resultado2.dadosPaginacao.hasPrevious)
    }

    @Test
    fun `Deve nao listar ambientes nao publicados em nenhum filtro`() {
        // Dado - ambiente publicado e ambiente não publicado
        criarAmbientePublicado(
            nome = "Sala de Aula Publicada",
            localizacao = Localizacao(Bloco.BLOCO_10, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0"))
        )

        val ambienteNaoPublicado = SalaAula(
            nome = "Sala Não Publicada",
            localizacao = Localizacao(
                bloco = Bloco.BLOCO_2,
                unidade = Unidade.SEDE,
                andar = 1
            ),
            capacidade = 30,
            geometrias = mutableSetOf(
                Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))
            ),
            esquadrias = mutableSetOf(),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = "Sala não publicada"
        )
        repoAmb.save(ambienteNaoPublicado)

        // Quando listar ambientes em diferentes formas
        val pageable = PageRequest.of(0, 10)

        // Então não deve incluir ambiente não publicado em nenhum filtro
        assertEquals(1, ambientesPUseCases.listarAmbientes(pageable).dadosPaginacao.totalElements)
        assertEquals(1, ambientesPUseCases.listarAmbientesPorNome("Sala", pageable).dadosPaginacao.totalElements)
        assertEquals(
            1,
            ambientesPUseCases.listarAmbientesPorLocalizacao("BLOCO_10", pageable).dadosPaginacao.totalElements
        )
    }

    @Test
    fun `Deve listar esquadrias dos ambientes publicados com paginacao`() {
        val ambiente1 = criarAmbientePublicado(
            nome = "Ambiente Publicado 1",
            localizacao = Localizacao(Bloco.BLOCO_1, Unidade.CIDADE_ALTA, 1),
            capacidade = 20,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("4.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
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

        val ambiente2 = criarAmbientePublicado(
            nome = "Ambiente Publicado 2",
            localizacao = Localizacao(Bloco.BLOCO_2, Unidade.CIDADE_ALTA, 1),
            capacidade = 25,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("5.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
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
        val ambiente3 = criarAmbientePublicado(
            nome = "Ambiente Publicado 3",
            localizacao = Localizacao(Bloco.BLOCO_3, Unidade.CIDADE_ALTA, 1),
            capacidade = 30,
            geometrias = mutableSetOf(Retangular(base = BigDecimal("6.0"), altura = BigDecimal("3.0"))),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
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

        // Validar que os ambientes retornados têm as esquadrias corretas
        val ambientesRetornados = resultado.ambientes.keys.toList()
        assertTrue(ambientesRetornados.any { it.nome == "Ambiente Publicado 1" })
        assertTrue(ambientesRetornados.any { it.nome == "Ambiente Publicado 2" })

        // Validar que o consolidado de tipos e materiais foi calculado
        assertTrue(resultado.totalTipoMaterial.isNotEmpty())

        // Deve conter a porta tipo ficha de madeira nos resultados
        assertTrue(resultado.totalTipoMaterial.any { it.tipo == TipoEsquadria.PORTA && it.material == MaterialEsquadria.MADEIRA_FICHA })

        // Validar que a área total das janelas de alumínio foi calculada corretamente (apenas dos 2 primeiros ambientes na paginação)
        val areaJanelasAluminio =
            resultado.totalTipoMaterial.find { it.tipo == TipoEsquadria.JANELA && it.material == MaterialEsquadria.ALUMINIO }?.area
        // A área total das janelas de alumínio deve ser a soma das áreas das janelas dos ambientes 1 e 2 (primeira página)
        // Ambiente 1: 1.0 * 1.0 = 1.0 m²
        // Ambiente 2: 1.5 * 1.2 = 1.8 m²
        // Total: 2.8 m²
        val areaEsperada =
            BigDecimal("1.0").multiply(BigDecimal("1.0"))
                .add(BigDecimal("1.5").multiply(BigDecimal("1.2")))
        assertEquals(areaEsperada.setScale(2), areaJanelasAluminio?.setScale(2))
    }

}
