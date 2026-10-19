package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.AmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.BlocoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.enums.TipoGeometria
import br.edu.ifce.ambientes_internos.model.dto.ambiente.*
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaReq
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@DataJpaTest
@Import(AmbienteNaoPublicadoUseCases::class, BlocoUseCases::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para os Casos de Uso dos Ambientes Não Publicados")
class AmbienteNaoPublicadoUseCasesIntegrationTest {

    @Autowired
    lateinit var ambientesNPUseCases: IAmbienteNaoPublicadoUseCases

    @Autowired
    lateinit var repoAmb: AmbienteRepository

    @Autowired
    lateinit var repoBloco: BlocoRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private fun unidadePorNome(nome: String): Unidade =
        Unidade.entries.firstOrNull { it.nome == nome }
            ?: throw IllegalArgumentException("Unidade inválida para teste: $nome")

    private fun idDoBloco(nome: String, unidade: Unidade): Long =
        repoBloco.findByUnidadeAndNome(unidade, Bloco.normalizar(nome))?.id
            ?: throw IllegalArgumentException("Bloco não encontrado para teste: $nome/$unidade")

    // -------------------------------------------------------------------------
    // Helpers de criação de entidades
    // -------------------------------------------------------------------------

    private fun criarSalaAula(
        nome: String = "Sala de Aula Exemplo",
        bloco: String = "BLOCO 10",
        unidade: Unidade = Unidade.CIDADE_ALTA,
        andar: Int = 1,
        capacidade: Int = 30,
        base: String = "6.0",
        altura: String = "3.0",
        informacaoAdicional: String = "Sala equipada com projetor e quadro branco."
    ): AmbienteReq = AmbienteReq(
        tipo = TipoAmbiente.SALA_AULA,
        nome = nome,
        localizacao = LocalizacaoReq(unidade = unidade, bloco = bloco, andar = andar),
        capacidade = capacidade,
        geometrias = setOf(
            GeometriaAmbienteReq(
                tipo = TipoGeometria.RETANGULAR,
                base = BigDecimal(base),
                altura = BigDecimal(altura)
            )
        ),
        pesDireitos = setOf(BigDecimal("3.0")),
        esquadrias = setOf(
            EsquadriaReq(
                tipo = TipoEsquadria.JANELA,
                geometria = GeometriaEsquadriaReq(base = BigDecimal("1.5"), altura = BigDecimal("1.2")),
                material = MaterialEsquadria.ALUMINIO,
                alturaPeitoril = BigDecimal("0.90"),
                informacaoAdicional = "Janela de correr"
            ),
            EsquadriaReq(
                tipo = TipoEsquadria.PORTA,
                geometria = GeometriaEsquadriaReq(base = BigDecimal("0.9"), altura = BigDecimal("2.1")),
                material = MaterialEsquadria.MADEIRA_FICHA,
                informacaoAdicional = "Porta de abrir"
            )
        ),
        informacaoAdicional = informacaoAdicional
    )

    private fun criarLaboratorioInformatica(
        nome: String = "Laboratório de Informática Exemplo",
        bloco: String = "BLOCO 10",
        unidade: Unidade = Unidade.CIDADE_ALTA,
        andar: Int = 1,
        capacidade: Int = 25,
        base: String = "8.0",
        altura: String = "4.0",
        informacaoAdicional: String = "Laboratório equipado com computadores e projetor."
    ): AmbienteReq = AmbienteReq(
        tipo = TipoAmbiente.LABORATORIO_INFORMATICA,
        nome = nome,
        localizacao = LocalizacaoReq(unidade = unidade, bloco = bloco, andar = andar),
        capacidade = capacidade,
        geometrias = setOf(
            GeometriaAmbienteReq(
                tipo = TipoGeometria.RETANGULAR,
                base = BigDecimal(base),
                altura = BigDecimal(altura)
            )
        ),
        pesDireitos = setOf(BigDecimal("3.5")),
        esquadrias = setOf(
            EsquadriaReq(
                tipo = TipoEsquadria.PORTA,
                geometria = GeometriaEsquadriaReq(base = BigDecimal("0.8"), altura = BigDecimal("2.10")),
                material = MaterialEsquadria.ALUMINIO
            ),
            EsquadriaReq(
                tipo = TipoEsquadria.JANELA,
                geometria = GeometriaEsquadriaReq(base = BigDecimal("1.2"), altura = BigDecimal("1.0")),
                material = MaterialEsquadria.ALUMINIO,
                alturaPeitoril = BigDecimal("1.0")
            )
        ),
        informacaoAdicional = informacaoAdicional
    )

    // -------------------------------------------------------------------------
    // Testes
    // -------------------------------------------------------------------------

    @Test
    fun `Deve cadastrar e recuperar ambiente por id`() {
        val req = criarSalaAula()

        // Quando um ambiente for cadastrado
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        // Então o ambiente deve ser persistido com status NAO_PUBLICADO
        assertEquals(req.nome, ambienteSalvo.nome)
        assertEquals(StatusAmbiente.NAO_PUBLICADO, ambienteSalvo.status)
        assertEquals(req.capacidade, ambienteSalvo.capacidade)

        // Quando o ambiente cadastrado for recuperado pelo seu ID
        val ambienteRecuperadoPorId = ambientesNPUseCases.obterAmbientePorId(ambienteSalvo.id)

        // Então o ambiente recuperado deve ser igual ao ambiente salvo anteriormente
        assertEquals(ambienteSalvo, ambienteRecuperadoPorId)
    }

    @Test
    fun `Deve atualizar dados basicos do ambiente`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        val ambienteBasicoReq = AmbienteBasicoReq(
            nome = "Sala de Aula Atualizada",
            localizacao = LocalizacaoReq(unidade = Unidade.CIDADE_ALTA, bloco = "BLOCO 11", andar = 2),
            capacidade = 35
        )

        // Quando o ambiente salvo for atualizado
        val ambienteBasicoAtualizado =
            ambientesNPUseCases.atualizarDadosBasicosAmbiente(ambienteSalvo.id, ambienteBasicoReq)

        // Então os dados atualizados devem ser retornados ao usuário
        assertEquals(ambienteBasicoReq.nome, ambienteBasicoAtualizado.nome)
        assertEquals(ambienteBasicoReq.capacidade, ambienteBasicoAtualizado.capacidade)
        assertEquals(ambienteBasicoReq.localizacao.bloco, ambienteBasicoAtualizado.localizacao.bloco)
        assertEquals(ambienteBasicoReq.localizacao.unidade.nome, ambienteBasicoAtualizado.localizacao.unidade)
        assertEquals(ambienteBasicoReq.localizacao.andar, ambienteBasicoAtualizado.localizacao.andar)
    }

    @Test
    fun `Deve incluir geometrias no ambiente`() {
        val req = criarSalaAula()

        val geometriasParaIncluir = setOf(
            GeometriaAmbienteReq(
                tipo = TipoGeometria.RETANGULAR, base = BigDecimal("4.0"), altura = BigDecimal("3.0"), repeticao = 2
            ),
            GeometriaAmbienteReq(
                tipo = TipoGeometria.TRIANGULAR, base = BigDecimal("2.5"), altura = BigDecimal("2.2")
            )
        )

        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        // Quando novas geometrias forem adicionadas ao ambiente
        val listaGeometriasAtualizadas =
            ambientesNPUseCases.incluirGeometriasAmbiente(ambienteSalvo.id, geometriasParaIncluir)

        // Então o total deve ser original + adicionadas
        assertEquals(req.geometrias.size + geometriasParaIncluir.size, listaGeometriasAtualizadas.geometrias.size)

        // E a área total deve bater com a soma de todas as geometrias retornadas
        val areaTotalEsperada = listaGeometriasAtualizadas.geometrias.fold(BigDecimal.ZERO) { acc, g -> acc + g.area }
        assertEquals(areaTotalEsperada, listaGeometriasAtualizadas.areaTotal)
    }

    @Test
    fun `Deve atualizar geometrias do ambiente`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        val geometriasParaSubstituir = setOf(
            GeometriaAmbienteReq(
                tipo = TipoGeometria.RETANGULAR, base = BigDecimal("4.5"), altura = BigDecimal("3.2"), repeticao = 2
            )
        )
        val areaEsperada = BigDecimal("4.5").multiply(BigDecimal("3.2"))
            .multiply(BigDecimal(2))
            .setScale(2, RoundingMode.HALF_UP)

        // Quando as geometrias do ambiente forem substituídas
        val listaGeometriasAtualizadas =
            ambientesNPUseCases.atualizarGeometriasAmbiente(ambienteSalvo.id, geometriasParaSubstituir)

        // Então deve conter apenas as geometrias fornecidas
        assertEquals(1, listaGeometriasAtualizadas.geometrias.size)
        assertEquals(areaEsperada, listaGeometriasAtualizadas.areaTotal)
    }

    @Test
    fun `Deve incluir pes direitos no ambiente`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        val pesDireitosParaIncluir = setOf(BigDecimal("3.5"), BigDecimal("4.0"))

        // Quando novos pés-direitos forem incluídos
        val pesDireitosRetornados =
            ambientesNPUseCases.incluirPesDireitosAmbiente(ambienteSalvo.id, pesDireitosParaIncluir)

        // Então o conjunto retornado deve conter os originais e os adicionados
        val pesDireitosEsperados = req.pesDireitos + pesDireitosParaIncluir
        assertEquals(pesDireitosEsperados, pesDireitosRetornados)
    }

    @Test
    fun `Deve atualizar pes direitos do ambiente`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        val pesDireitosParaAtualizar = setOf(BigDecimal("2.8"), BigDecimal("3.2"))

        // Quando os pés-direitos do ambiente forem substituídos
        val pesDireitosRetornados =
            ambientesNPUseCases.atualizarPesDireitosAmbiente(ambienteSalvo.id, pesDireitosParaAtualizar)

        // Então o conjunto retornado deve corresponder exatamente ao fornecido
        assertEquals(pesDireitosParaAtualizar, pesDireitosRetornados)
    }

    @Test
    fun `Deve incluir esquadrias no ambiente`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        val esquadriasParaIncluir = setOf(
            EsquadriaReq(
                tipo = TipoEsquadria.JANELA,
                geometria = GeometriaEsquadriaReq(base = BigDecimal("1.2"), altura = BigDecimal("1.2"), repeticao = 2),
                material = MaterialEsquadria.ALUMINIO,
                alturaPeitoril = BigDecimal("0.80")
            )
        )

        // Quando novas esquadrias forem adicionadas ao ambiente
        val esquadriasDetalhesAtualizados = ambientesNPUseCases.incluirEsquadriasAmbiente(
            ambienteSalvo.id,
            esquadriasParaIncluir
        )

        // Então o total de esquadrias deve ser original + adicionadas
        assertEquals(
            req.esquadrias.size + esquadriasParaIncluir.size,
            esquadriasDetalhesAtualizados.esquadrias.size
        )
    }

    @Test
    fun `Deve atualizar as esquadrias no ambiente`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        val esquadriasParaAtualizar = setOf(
            EsquadriaReq(
                tipo = TipoEsquadria.PORTA,
                geometria = GeometriaEsquadriaReq(base = BigDecimal("0.8"), altura = BigDecimal("2.1")),
                material = MaterialEsquadria.MADEIRA_FICHA
            )
        )

        // Quando as esquadrias forem substituídas
        val esquadriasDetalhesAtualizados = ambientesNPUseCases.atualizarEsquadriasAmbiente(
            ambienteSalvo.id,
            esquadriasParaAtualizar
        )

        // Então deve conter apenas as esquadrias fornecidas
        assertEquals(1, esquadriasDetalhesAtualizados.esquadrias.size)
        assertEquals(TipoEsquadria.PORTA, esquadriasDetalhesAtualizados.esquadrias.first().tipo)
    }

    @Test
    fun `Deve atualizar informacao adicional do ambiente`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        val informacaoAdicionalAtualizada = "Sala equipada com ar-condicionado e projetor."

        // Quando a informação adicional do ambiente for atualizada
        val informacaoAdicionalRetornada =
            ambientesNPUseCases.atualizarInformacaoAdicionalAmbiente(ambienteSalvo.id, informacaoAdicionalAtualizada)

        // Então a informação adicional retornada deve corresponder à fornecida
        assertEquals(informacaoAdicionalAtualizada, informacaoAdicionalRetornada)
    }

    @Test
    fun `Deve alterar tipo e dados do ambiente`() {
        val reqOriginal = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(reqOriginal)

        val reqAlteracao = criarLaboratorioInformatica()

        // Quando o tipo e dados do ambiente forem alterados
        val ambienteAlterado = ambientesNPUseCases.alterarTipoDadosAmbiente(ambienteSalvo.id, reqAlteracao)

        // Então o tipo deve ter mudado e a localização permanecer a mesma
        assertEquals(TipoAmbiente.LABORATORIO_INFORMATICA.nome, ambienteAlterado.tipo)
        assertEquals(ambienteSalvo.localizacao, ambienteAlterado.localizacao)

        // E o ID original não deve mais existir
        assertThrows<NoSuchElementException> { ambientesNPUseCases.obterAmbientePorId(ambienteSalvo.id) }
    }

    @Test
    fun `Deve honrar a localizacao do request ao alterar tipo e dados`() {
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala Troca Localizacao", bloco = "BLOCO 10", unidade = Unidade.CIDADE_ALTA, andar = 1)
        )
        val localizacaoAntigaId = ambienteSalvo.localizacao.id

        val reqAlteracao = criarLaboratorioInformatica(
            nome = "Laboratorio Alterado",
            bloco = "BLOCO 11",
            unidade = Unidade.SEDE,
            andar = 2
        )

        // Quando o tipo e dados forem alterados com nova localização
        val ambienteAlterado = ambientesNPUseCases.alterarTipoDadosAmbiente(ambienteSalvo.id, reqAlteracao)

        // Então a localização do request deve ser honrada
        assertEquals(reqAlteracao.localizacao.bloco, ambienteAlterado.localizacao.bloco)
        assertEquals(reqAlteracao.localizacao.unidade.nome, ambienteAlterado.localizacao.unidade)
        assertEquals(reqAlteracao.localizacao.andar, ambienteAlterado.localizacao.andar)

        // E o bloco novo deve ter sido resolvido/criado para a unidade informada
        assertNotNull(repoBloco.findByUnidadeAndNome(Unidade.SEDE, "BLOCO 11"))

        // E a localização antiga (órfã) deve ser removida
        repoAmb.flush()
        assertEquals(0, contarPorId("localizacao", localizacaoAntigaId))
    }

    @Test
    fun `Deve duplicar um ambiente cadastrado`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        val ambienteNomeLocalizacaoReq = AmbienteNomeLocalizacaoReq(
            nome = "Sala de Aula Duplicada",
            localizacao = LocalizacaoReq(unidade = Unidade.CIDADE_ALTA, bloco = "BLOCO 11", andar = 1)
        )

        // Quando o ambiente for duplicado
        val ambienteDuplicado = ambientesNPUseCases.duplicarAmbiente(ambienteSalvo.id, ambienteNomeLocalizacaoReq)

        // Então os dados copiados devem corresponder ao original, exceto nome e localização
        assertEquals(ambienteNomeLocalizacaoReq.nome, ambienteDuplicado.nome)
        assertEquals(ambienteNomeLocalizacaoReq.localizacao.bloco, ambienteDuplicado.localizacao.bloco)
        assertEquals(ambienteNomeLocalizacaoReq.localizacao.unidade.nome, ambienteDuplicado.localizacao.unidade)
        assertEquals(ambienteNomeLocalizacaoReq.localizacao.andar, ambienteDuplicado.localizacao.andar)
        assertEquals(ambienteSalvo.capacidade, ambienteDuplicado.capacidade)
        assertEquals(ambienteSalvo.tipo, ambienteDuplicado.tipo)
        assertEquals(StatusAmbiente.NAO_PUBLICADO, ambienteDuplicado.status)

        // E o bloco novo deve ter sido resolvido/criado para a unidade informada
        assertNotNull(repoBloco.findByUnidadeAndNome(Unidade.CIDADE_ALTA, "BLOCO 11"))
    }

    @Test
    fun `Deve alterar o status de um ambiente de NAO_PUBLICADO para AGUARDANDO_VALIDACAO`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        // Quando o ambiente for enviado para validação
        ambientesNPUseCases.enviarValidacaoAmbientes(setOf(ambienteSalvo.id))

        // Então seu status deve ser alterado para AGUARDANDO_VALIDACAO
        val ambienteAguardandoValidacao = repoAmb.findById(ambienteSalvo.id)
        assertEquals(StatusAmbiente.AGUARDANDO_VALIDACAO, ambienteAguardandoValidacao.get().status)
    }

    @Test
    fun `Deve listar os ambientes com status NAO_PUBLICADO`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        // Quando os ambientes com status NAO_PUBLICADO forem listados com paginação
        val pageable = PageRequest.of(0, 10)
        val ambientesNaoPublicados = ambientesNPUseCases.listarAmbientes(pageable)

        // Então a lista deve conter o ambiente cadastrado
        assertTrue(ambientesNaoPublicados.ambientes.any { it.id == ambienteSalvo.id })
        assertEquals(1, ambientesNaoPublicados.dadosPaginacao.totalElements)
        assertEquals(1, ambientesNaoPublicados.dadosPaginacao.totalPages)
        assertEquals(0, ambientesNaoPublicados.dadosPaginacao.currentPage)
        assertEquals(10, ambientesNaoPublicados.dadosPaginacao.pageSize)
        assertEquals(false, ambientesNaoPublicados.dadosPaginacao.hasNext)
        assertEquals(false, ambientesNaoPublicados.dadosPaginacao.hasPrevious)
    }

    @Test
    fun `Deve limitar pagina para no maximo 100 ambientes nao publicados`() {
        (1..120).forEach { indice ->
            ambientesNPUseCases.cadastrarAmbiente(
                criarSalaAula(
                    nome = "Sala NP $indice",
                    bloco = "BLOCO 10",
                    unidade = Unidade.CIDADE_ALTA,
                    andar = indice,
                    capacidade = 30
                )
            )
        }

        val resultado = ambientesNPUseCases.listarAmbientes(PageRequest.of(0, 1000))

        assertEquals(120, resultado.dadosPaginacao.totalElements)
        assertEquals(100, resultado.dadosPaginacao.pageSize)
        assertEquals(100, resultado.ambientes.size)
    }

    @ParameterizedTest(name = "Filtro por nome ''{0}'' deve retornar {1} item(ns)")
    @MethodSource("filtrosNomeNaoPublicados")
    fun `Deve filtrar ambientes nao publicados por nome em cenarios relevantes`(
        filtroNome: String,
        totalEsperado: Int,
        nomeEsperado: String
    ) {
        val ambiente1 = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala de Aula Exemplo")
        )
        ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala de Estudos", bloco = "BLOCO 11")
        )

        val ambientesPorNome = ambientesNPUseCases.listarAmbientesPorNome(filtroNome, PageRequest.of(0, 10))

        assertEquals(totalEsperado.toLong(), ambientesPorNome.dadosPaginacao.totalElements)
        if (totalEsperado == 0) {
            assertTrue(ambientesPorNome.ambientes.isEmpty())
        } else {
            assertEquals(nomeEsperado, ambientesPorNome.ambientes.first().nome)
            assertTrue(ambientesPorNome.ambientes.any { it.id == ambiente1.id || it.nome == nomeEsperado })
        }
    }

    @Test
    fun `Deve filtrar ambientes nao publicados por id de bloco e andar`() {
        ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala de Aula Exemplo", bloco = "BLOCO 10", andar = 1)
        )
        val ambienteOutroAndar = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala em Outro Andar", bloco = "BLOCO 10", andar = 2)
        )
        val blocoId = idDoBloco("BLOCO 10", Unidade.CIDADE_ALTA)

        val porBloco = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = blocoId, unidade = null, andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(2, porBloco.dadosPaginacao.totalElements)

        val porBlocoEAndar = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = blocoId, unidade = null, andar = 2),
            PageRequest.of(0, 10)
        )
        assertEquals(1, porBlocoEAndar.dadosPaginacao.totalElements)
        assertEquals(ambienteOutroAndar.nome, porBlocoEAndar.ambientes.first().nome)

        val andarInexistente = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = blocoId, unidade = null, andar = 99),
            PageRequest.of(0, 10)
        )
        assertEquals(0, andarInexistente.dadosPaginacao.totalElements)
        assertTrue(andarInexistente.ambientes.isEmpty())
    }

    @Test
    fun `Deve filtrar ambientes nao publicados por unidade com match exato normalizado`() {
        val ambiente1 = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala Cidade Alta", unidade = Unidade.CIDADE_ALTA)
        )
        ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala UEPE", unidade = Unidade.UEPE, bloco = "BLOCO 11")
        )

        // Exato: aceita o nome técnico e o nome de exibição, normalizados
        val porNomeTecnico = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = null, unidade = "CIDADE_ALTA", andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(1, porNomeTecnico.dadosPaginacao.totalElements)
        assertEquals(ambiente1.nome, porNomeTecnico.ambientes.first().nome)

        val porNomeExibicao = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = null, unidade = "Cidade Alta", andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(1, porNomeExibicao.dadosPaginacao.totalElements)
        assertEquals(ambiente1.nome, porNomeExibicao.ambientes.first().nome)

        // Parcial não casa: o select do frontend envia a unidade completa
        val parcial = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = null, unidade = "alta", andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(0, parcial.dadosPaginacao.totalElements)
        assertTrue(parcial.ambientes.isEmpty())
    }

    @Test
    fun `Deve filtrar ambientes nao publicados por id de bloco`() {
        val ambienteBiblioteca = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala na Biblioteca", bloco = "biblioteca academica")
        )
        ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala no Bloco 1", bloco = "Bloco 1", andar = 1)
        )
        val blocoId = idDoBloco("biblioteca academica", Unidade.CIDADE_ALTA)

        val resultado = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = blocoId, unidade = null, andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(1, resultado.dadosPaginacao.totalElements)
        assertEquals(ambienteBiblioteca.nome, resultado.ambientes.first().nome)

        val inexistente = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = Long.MAX_VALUE, unidade = null, andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(0, inexistente.dadosPaginacao.totalElements)
        assertTrue(inexistente.ambientes.isEmpty())
    }

    @Test
    fun `Deve escopar filtro por id de bloco pela unidade`() {
        val ambienteCidadeAlta = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala Biblioteca Cidade Alta", bloco = "BIBLIOTECA", unidade = Unidade.CIDADE_ALTA)
        )
        val ambienteSede = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala Biblioteca Sede", bloco = "BIBLIOTECA", unidade = Unidade.SEDE)
        )

        // Bloco homônimo criado em duas unidades distintas (unique composto unidade+nome)
        assertEquals(2, repoBloco.findAllByNome("BIBLIOTECA").size)
        val blocoCidadeAltaId = idDoBloco("BIBLIOTECA", Unidade.CIDADE_ALTA)
        val blocoSedeId = idDoBloco("BIBLIOTECA", Unidade.SEDE)

        // O id já identifica o bloco; a unidade confirma o escopo
        val somenteSede = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = blocoSedeId, unidade = "SEDE", andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(1, somenteSede.dadosPaginacao.totalElements)
        assertEquals(ambienteSede.nome, somenteSede.ambientes.first().nome)

        val somenteCidadeAlta = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = blocoCidadeAltaId, unidade = "CIDADE_ALTA", andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(1, somenteCidadeAlta.dadosPaginacao.totalElements)
        assertEquals(ambienteCidadeAlta.nome, somenteCidadeAlta.ambientes.first().nome)

        // Unidade divergente do bloco: página vazia
        val divergente = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(blocoId = blocoSedeId, unidade = "CIDADE_ALTA", andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(0, divergente.dadosPaginacao.totalElements)
        assertTrue(divergente.ambientes.isEmpty())
    }

    companion object {
        @JvmStatic
        fun filtrosNomeNaoPublicados(): Stream<Arguments> = Stream.of(
            Arguments.of("Sala de Aula", 1, "Sala de Aula Exemplo"),
            Arguments.of("sala de aula", 1, "Sala de Aula Exemplo"),
            Arguments.of("Nome Inexistente", 0, "")
        )
    }

    @Test
    fun `Deve deletar um ambiente existente com sucesso`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        // Quando o ambiente for deletado
        ambientesNPUseCases.deletarAmbientes(setOf(ambienteSalvo.id))

        // Então o ambiente não deve mais existir
        assertThrows<NoSuchElementException> { ambientesNPUseCases.obterAmbientePorId(ambienteSalvo.id) }
    }

    @Test
    fun `Deve deletar múltiplos ambientes com sucesso`() {
        val ambiente1 = ambientesNPUseCases.cadastrarAmbiente(criarSalaAula())
        val ambiente2 = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala de Aula 2", bloco = "BLOCO 11", informacaoAdicional = "Segunda sala.")
        )
        val ambiente3 = ambientesNPUseCases.cadastrarAmbiente(
            criarLaboratorioInformatica(bloco = "BLOCO 12", andar = 2)
        )

        // Quando todos os ambientes forem deletados
        ambientesNPUseCases.deletarAmbientes(setOf(ambiente1.id, ambiente2.id, ambiente3.id))

        // Então nenhum dos ambientes deve mais existir
        assertThrows<NoSuchElementException> { ambientesNPUseCases.obterAmbientePorId(ambiente1.id) }
        assertThrows<NoSuchElementException> { ambientesNPUseCases.obterAmbientePorId(ambiente2.id) }
        assertThrows<NoSuchElementException> { ambientesNPUseCases.obterAmbientePorId(ambiente3.id) }

        // E a lista de ambientes deve estar vazia
        val ambientesRestantes = ambientesNPUseCases.listarAmbientes(PageRequest.of(0, 10))
        assertEquals(0, ambientesRestantes.dadosPaginacao.totalElements)
    }

    @Test
    fun `Deve lançar exceção ao tentar deletar ambiente inexistente`() {
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(criarSalaAula())

        // Quando tentar deletar um ID que não existe
        assertThrows<NoSuchElementException> {
            ambientesNPUseCases.deletarAmbientes(setOf(ambienteSalvo.id + 999L))
        }
    }

    @Test
    fun `Deve lancar excecao ao cadastrar ambiente com nome e localizacao ja existentes`() {
        val req = criarSalaAula()
        ambientesNPUseCases.cadastrarAmbiente(req)

        // Quando cadastrar outro ambiente com o mesmo nome e mesma localização
        assertThrows<IllegalArgumentException> {
            ambientesNPUseCases.cadastrarAmbiente(req)
        }
    }

    @Test
    fun `Deve lancar excecao ao cadastrar ambiente sem porta`() {
        val reqSemPorta = criarSalaAula(
            nome = "Sala sem porta"
        ).copy(
            esquadrias = setOf(
                EsquadriaReq(
                    tipo = TipoEsquadria.JANELA,
                    geometria = GeometriaEsquadriaReq(base = BigDecimal("1.5"), altura = BigDecimal("1.2")),
                    material = MaterialEsquadria.ALUMINIO,
                    alturaPeitoril = BigDecimal("0.90"),
                    informacaoAdicional = "Janela de correr"
                )
            )
        )

        val erro = assertThrows<IllegalArgumentException> {
            ambientesNPUseCases.cadastrarAmbiente(reqSemPorta)
        }

        assertEquals("O ambiente deve possuir pelo menos uma porta", erro.message)
    }

    @Test
    fun `Deve lancar excecao ao atualizar dados basicos para nome e localizacao ja existentes`() {
        val ambiente1 = ambientesNPUseCases.cadastrarAmbiente(criarSalaAula())
        val ambiente2 = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala Distinta", bloco = "BLOCO 11", andar = 2, capacidade = 20)
        )

        // Quando tentar atualizar o segundo para nome/localização do primeiro
        val atualizacaoConflitante = AmbienteBasicoReq(
            nome = ambiente1.nome,
            localizacao = LocalizacaoReq(
                unidade = unidadePorNome(ambiente1.localizacao.unidade),
                bloco = ambiente1.localizacao.bloco,
                andar = ambiente1.localizacao.andar
            ),
            capacidade = ambiente2.capacidade
        )

        // Então deve falhar por violação de unicidade
        val erro = assertThrows<Exception> {
            ambientesNPUseCases.atualizarDadosBasicosAmbiente(ambiente2.id, atualizacaoConflitante)
        }
        assertTrue(erro is IllegalArgumentException || erro is DataIntegrityViolationException)
    }

    @Test
    fun `Deve lancar excecao ao enviar validacao com ids inexistentes`() {
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(criarSalaAula())

        // Quando enviar para validação um ID que não existe
        assertThrows<NoSuchElementException> {
            ambientesNPUseCases.enviarValidacaoAmbientes(setOf(ambienteSalvo.id + 999L))
        }
    }

    @Test
    fun `Deve lancar excecao ao enviar validacao com conjunto vazio`() {
        // Quando enviar para validação sem IDs
        assertThrows<NoSuchElementException> {
            ambientesNPUseCases.enviarValidacaoAmbientes(emptySet())
        }
    }

    private fun contarPorId(tabela: String, id: Long): Int {
        return jdbcTemplate.queryForObject(
            "select count(1) from $tabela where id = ?",
            Int::class.java,
            id
        ) ?: 0
    }

    @Test
    fun `Deve manter vinculo reverso ao incluir e atualizar geometrias`() {
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(criarSalaAula())

        ambientesNPUseCases.incluirGeometriasAmbiente(
            ambienteSalvo.id,
            setOf(GeometriaAmbienteReq(tipo = TipoGeometria.TRIANGULAR, base = BigDecimal("2.0"), altura = BigDecimal("1.5")))
        )

        ambientesNPUseCases.atualizarGeometriasAmbiente(
            ambienteSalvo.id,
            setOf(GeometriaAmbienteReq(tipo = TipoGeometria.RETANGULAR, base = BigDecimal("4.0"), altura = BigDecimal("2.0")))
        )

        val ambientePersistido = repoAmb.findById(ambienteSalvo.id).get()
        assertEquals(true, ambientePersistido.geometrias.all { it.ambiente.id == ambientePersistido.id })
    }

    @Test
    fun `Deve manter vinculo reverso ao incluir e atualizar esquadrias`() {
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(criarSalaAula())

        ambientesNPUseCases.incluirEsquadriasAmbiente(
            ambienteSalvo.id,
            setOf(
                EsquadriaReq(
                    tipo = TipoEsquadria.JANELA,
                    geometria = GeometriaEsquadriaReq(base = BigDecimal("1.1"), altura = BigDecimal("1.0")),
                    material = MaterialEsquadria.ALUMINIO,
                    alturaPeitoril = BigDecimal("0.8")
                )
            )
        )

        ambientesNPUseCases.atualizarEsquadriasAmbiente(
            ambienteSalvo.id,
            setOf(
                EsquadriaReq(
                    tipo = TipoEsquadria.PORTA,
                    geometria = GeometriaEsquadriaReq(base = BigDecimal("0.9"), altura = BigDecimal("2.1")),
                    material = MaterialEsquadria.MADEIRA_FICHA
                )
            )
        )

        val ambientePersistido = repoAmb.findById(ambienteSalvo.id).get()
        assertEquals(true, ambientePersistido.esquadrias.all { it.ambiente.id == ambientePersistido.id })
    }

    @Test
    fun `Deve remover filhos antigos ao substituir colecoes`() {
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(criarSalaAula())
        val antes = repoAmb.findById(ambienteSalvo.id).get()
        val idsGeometriasAntigas = antes.geometrias.mapNotNull { it.id }
        val idsEsquadriasAntigas = antes.esquadrias.mapNotNull { it.id }

        ambientesNPUseCases.atualizarGeometriasAmbiente(
            ambienteSalvo.id,
            setOf(GeometriaAmbienteReq(tipo = TipoGeometria.RETANGULAR, base = BigDecimal("7.0"), altura = BigDecimal("2.0")))
        )
        ambientesNPUseCases.atualizarEsquadriasAmbiente(
            ambienteSalvo.id,
            setOf(
                EsquadriaReq(
                    tipo = TipoEsquadria.PORTA,
                    geometria = GeometriaEsquadriaReq(base = BigDecimal("1.0"), altura = BigDecimal("2.2")),
                    material = MaterialEsquadria.ALUMINIO
                )
            )
        )

        repoAmb.flush()
        idsGeometriasAntigas.forEach { assertEquals(0, contarPorId("geometria", it)) }
        idsEsquadriasAntigas.forEach { assertEquals(0, contarPorId("esquadria", it)) }
    }

    @Test
    fun `Deve deletar ambiente removendo filhos e localizacao orfa`() {
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala Excluir", bloco = "BLOCO 12", unidade = Unidade.SEDE, andar = 8)
        )
        val antes = repoAmb.findById(ambienteSalvo.id).get()
        val localizacaoId = antes.localizacao.id!!
        val idsGeometrias = antes.geometrias.mapNotNull { it.id }
        val idsEsquadrias = antes.esquadrias.mapNotNull { it.id }

        ambientesNPUseCases.deletarAmbientes(setOf(ambienteSalvo.id))

        repoAmb.flush()
        idsGeometrias.forEach { assertEquals(0, contarPorId("geometria", it)) }
        idsEsquadrias.forEach { assertEquals(0, contarPorId("esquadria", it)) }
        assertEquals(0, contarPorId("localizacao", localizacaoId))
    }

}
