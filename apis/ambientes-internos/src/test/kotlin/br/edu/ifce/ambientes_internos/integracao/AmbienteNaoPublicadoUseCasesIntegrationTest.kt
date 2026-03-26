package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.AmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Bloco
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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DataJpaTest
@Import(AmbienteNaoPublicadoUseCases::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para os Casos de Uso dos Ambientes Não Publicados")
class AmbienteNaoPublicadoUseCasesIntegrationTest {

    @Autowired
    lateinit var ambientesNPUseCases: IAmbienteNaoPublicadoUseCases

    @Autowired
    lateinit var repoAmb: AmbienteRepository

    private fun blocoPorNome(nome: String): Bloco =
        Bloco.entries.firstOrNull { it.nome == nome }
            ?: throw IllegalArgumentException("Bloco inválido para teste: $nome")

    private fun unidadePorNome(nome: String): Unidade =
        Unidade.entries.firstOrNull { it.nome == nome }
            ?: throw IllegalArgumentException("Unidade inválida para teste: $nome")

    // -------------------------------------------------------------------------
    // Helpers de criação de entidades
    // -------------------------------------------------------------------------

    private fun criarSalaAula(
        nome: String = "Sala de Aula Exemplo",
        bloco: Bloco = Bloco.BLOCO_10,
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
        bloco: Bloco = Bloco.BLOCO_10,
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
            localizacao = LocalizacaoReq(unidade = Unidade.CIDADE_ALTA, bloco = Bloco.BLOCO_10, andar = 2),
            capacidade = 35
        )

        // Quando o ambiente salvo for atualizado
        val ambienteBasicoAtualizado =
            ambientesNPUseCases.atualizarDadosBasicosAmbiente(ambienteSalvo.id, ambienteBasicoReq)

        // Então os dados atualizados devem ser retornados ao usuário
        assertEquals(ambienteBasicoReq.nome, ambienteBasicoAtualizado.nome)
        assertEquals(ambienteBasicoReq.capacidade, ambienteBasicoAtualizado.capacidade)
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
    fun `Deve duplicar um ambiente cadastrado`() {
        val req = criarSalaAula()
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(req)

        val ambienteNomeLocalizacaoReq = AmbienteNomeLocalizacaoReq(
            nome = "Sala de Aula Duplicada",
            localizacao = LocalizacaoReq(unidade = Unidade.CIDADE_ALTA, bloco = Bloco.BLOCO_11, andar = 1)
        )

        // Quando o ambiente for duplicado
        val ambienteDuplicado = ambientesNPUseCases.duplicarAmbiente(ambienteSalvo.id, ambienteNomeLocalizacaoReq)

        // Então os dados copiados devem corresponder ao original, exceto nome e localização
        assertEquals(ambienteNomeLocalizacaoReq.nome, ambienteDuplicado.nome)
        assertEquals(ambienteNomeLocalizacaoReq.localizacao.bloco.nome, ambienteDuplicado.localizacao.bloco)
        assertEquals(ambienteSalvo.capacidade, ambienteDuplicado.capacidade)
        assertEquals(ambienteSalvo.tipo, ambienteDuplicado.tipo)
        assertEquals(StatusAmbiente.NAO_PUBLICADO, ambienteDuplicado.status)
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
                    bloco = Bloco.BLOCO_10,
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
            criarSalaAula(nome = "Sala de Estudos", bloco = Bloco.BLOCO_11)
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

    @ParameterizedTest(name = "Filtro por localizacao ''{0}'' deve retornar {1} item(ns)")
    @MethodSource("filtrosLocalizacaoNaoPublicados")
    fun `Deve filtrar ambientes nao publicados por localizacao em cenarios relevantes`(
        filtroLocalizacao: LocalizacaoPesquisaReq,
        totalEsperado: Int,
        nomeEsperado: String
    ) {
        val ambiente1 = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala de Aula Exemplo", bloco = Bloco.BLOCO_10)
        )
        ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala em Outro Bloco", bloco = Bloco.BLOCO_11)
        )

        val ambientesPorLocalizacao = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            filtroLocalizacao,
            PageRequest.of(0, 10)
        )

        assertEquals(totalEsperado.toLong(), ambientesPorLocalizacao.dadosPaginacao.totalElements)
        if (totalEsperado == 0) {
            assertTrue(ambientesPorLocalizacao.ambientes.isEmpty())
        } else {
            assertEquals(nomeEsperado, ambientesPorLocalizacao.ambientes.first().nome)
            assertTrue(ambientesPorLocalizacao.ambientes.any { it.id == ambiente1.id || it.nome == nomeEsperado })
        }
    }

    companion object {
        @JvmStatic
        fun filtrosNomeNaoPublicados(): Stream<Arguments> = Stream.of(
            Arguments.of("Sala de Aula", 1, "Sala de Aula Exemplo"),
            Arguments.of("sala de aula", 1, "Sala de Aula Exemplo"),
            Arguments.of("Nome Inexistente", 0, "")
        )

        @JvmStatic
        fun filtrosLocalizacaoNaoPublicados(): Stream<Arguments> = Stream.of(
            Arguments.of(
                LocalizacaoPesquisaReq(bloco = "BLOCO_10", unidade = null, andar = null),
                1,
                "Sala de Aula Exemplo"
            ),
            Arguments.of(
                LocalizacaoPesquisaReq(bloco = "bloco 10", unidade = null, andar = null),
                1,
                "Sala de Aula Exemplo"
            ),
            Arguments.of(LocalizacaoPesquisaReq(bloco = "LOCALIZACAO_INEXISTENTE", unidade = null, andar = null), 0, "")
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
            criarSalaAula(nome = "Sala de Aula 2", bloco = Bloco.BLOCO_11, informacaoAdicional = "Segunda sala.")
        )
        val ambiente3 = ambientesNPUseCases.cadastrarAmbiente(
            criarLaboratorioInformatica(bloco = Bloco.BLOCO_12, andar = 2)
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
            criarSalaAula(nome = "Sala Distinta", bloco = Bloco.BLOCO_11, andar = 2, capacidade = 20)
        )

        // Quando tentar atualizar o segundo para nome/localização do primeiro
        val atualizacaoConflitante = AmbienteBasicoReq(
            nome = ambiente1.nome,
            localizacao = LocalizacaoReq(
                unidade = unidadePorNome(ambiente1.localizacao.unidade),
                bloco = blocoPorNome(ambiente1.localizacao.bloco),
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

}
