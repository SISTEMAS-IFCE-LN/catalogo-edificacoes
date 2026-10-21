package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.AmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.BlocoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.CampusUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.UnidadeUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.enums.TipoGeometria
import br.edu.ifce.ambientes_internos.model.dto.ambiente.*
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaReq
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
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
@Import(
    AmbienteNaoPublicadoUseCases::class,
    CampusUseCases::class,
    UnidadeUseCases::class,
    BlocoUseCases::class
)
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
    lateinit var repoCampus: CampusRepository

    @Autowired
    lateinit var repoUnidade: UnidadeRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private fun unidade(campusNome: String, unidadeNome: String): Unidade =
        criarUnidade(repoCampus, repoUnidade, campusNome, unidadeNome)

    private fun bloco(campusNome: String, unidadeNome: String, blocoNome: String): Bloco =
        criarBloco(repoBloco, unidade(campusNome, unidadeNome), blocoNome)

    // -------------------------------------------------------------------------
    // Helpers de criação de entidades
    // -------------------------------------------------------------------------

    private fun criarSalaAula(
        nome: String = "Sala de Aula Exemplo",
        bloco: String = "BLOCO 10",
        campus: String = "LIMOEIRO DO NORTE",
        unidade: String = "CIDADE ALTA",
        andar: Int = 1,
        capacidade: Int = 30,
        base: String = "6.0",
        altura: String = "3.0",
        informacaoAdicional: String = "Sala equipada com projetor e quadro branco."
    ): AmbienteReq = AmbienteReq(
        tipo = TipoAmbiente.SALA_AULA,
        nome = nome,
        localizacao = localizacaoReqPorNome(campus, unidade, bloco, andar),
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
        campus: String = "LIMOEIRO DO NORTE",
        unidade: String = "CIDADE ALTA",
        andar: Int = 1,
        capacidade: Int = 25,
        base: String = "8.0",
        altura: String = "4.0",
        informacaoAdicional: String = "Laboratório equipado com computadores e projetor."
    ): AmbienteReq = AmbienteReq(
        tipo = TipoAmbiente.LABORATORIO_INFORMATICA,
        nome = nome,
        localizacao = localizacaoReqPorNome(campus, unidade, bloco, andar),
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

        // E a localização deve ser devolvida como referências {id, nome}
        val campus = repoCampus.findByNome("LIMOEIRO DO NORTE")!!
        val unidade = unidade("LIMOEIRO DO NORTE", "CIDADE ALTA")
        val bloco = repoBloco.findByUnidadeAndNome(unidade, "BLOCO 10")!!
        assertEquals(ReferenciaRes(campus.id!!, "LIMOEIRO DO NORTE"), ambienteSalvo.localizacao.campus)
        assertEquals(ReferenciaRes(unidade.id!!, "CIDADE ALTA"), ambienteSalvo.localizacao.unidade)
        assertEquals(ReferenciaRes(bloco.id!!, "BLOCO 10"), ambienteSalvo.localizacao.bloco)
        assertEquals(1, ambienteSalvo.localizacao.andar)

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
            localizacao = localizacaoReqPorNome(bloco = "BLOCO 11", andar = 2),
            capacidade = 35
        )

        // Quando o ambiente salvo for atualizado
        val ambienteBasicoAtualizado =
            ambientesNPUseCases.atualizarDadosBasicosAmbiente(ambienteSalvo.id, ambienteBasicoReq)

        // Então os dados atualizados devem ser retornados ao usuário
        assertEquals(ambienteBasicoReq.nome, ambienteBasicoAtualizado.nome)
        assertEquals(ambienteBasicoReq.capacidade, ambienteBasicoAtualizado.capacidade)
        assertEquals("BLOCO 11", ambienteBasicoAtualizado.localizacao.bloco.nome)
        assertEquals("CIDADE ALTA", ambienteBasicoAtualizado.localizacao.unidade.nome)
        assertEquals("LIMOEIRO DO NORTE", ambienteBasicoAtualizado.localizacao.campus.nome)
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
            criarSalaAula(nome = "Sala Troca Localizacao", bloco = "BLOCO 10", andar = 1)
        )
        val localizacaoAntigaId = ambienteSalvo.localizacao.id

        val reqAlteracao = criarLaboratorioInformatica(
            nome = "Laboratorio Alterado",
            bloco = "BLOCO 11",
            campus = "FORTALEZA",
            unidade = "UEPE",
            andar = 2
        )

        // Quando o tipo e dados forem alterados com nova localização
        val ambienteAlterado = ambientesNPUseCases.alterarTipoDadosAmbiente(ambienteSalvo.id, reqAlteracao)

        // Então a localização do request deve ser honrada
        assertEquals("BLOCO 11", ambienteAlterado.localizacao.bloco.nome)
        assertEquals("UEPE", ambienteAlterado.localizacao.unidade.nome)
        assertEquals("FORTALEZA", ambienteAlterado.localizacao.campus.nome)
        assertEquals(reqAlteracao.localizacao.andar, ambienteAlterado.localizacao.andar)

        // E o bloco novo deve ter sido resolvido/criado para a unidade informada
        assertNotNull(repoBloco.findByUnidadeAndNome(unidade("FORTALEZA", "UEPE"), "BLOCO 11"))

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
            localizacao = localizacaoReqPorNome(bloco = "BLOCO 11", andar = 1)
        )

        // Quando o ambiente for duplicado
        val ambienteDuplicado = ambientesNPUseCases.duplicarAmbiente(ambienteSalvo.id, ambienteNomeLocalizacaoReq)

        // Então os dados copiados devem corresponder ao original, exceto nome e localização
        assertEquals(ambienteNomeLocalizacaoReq.nome, ambienteDuplicado.nome)
        assertEquals("BLOCO 11", ambienteDuplicado.localizacao.bloco.nome)
        assertEquals("CIDADE ALTA", ambienteDuplicado.localizacao.unidade.nome)
        assertEquals("LIMOEIRO DO NORTE", ambienteDuplicado.localizacao.campus.nome)
        assertEquals(ambienteNomeLocalizacaoReq.localizacao.andar, ambienteDuplicado.localizacao.andar)
        assertEquals(ambienteSalvo.capacidade, ambienteDuplicado.capacidade)
        assertEquals(ambienteSalvo.tipo, ambienteDuplicado.tipo)
        assertEquals(StatusAmbiente.NAO_PUBLICADO, ambienteDuplicado.status)

        // E o bloco novo deve ter sido resolvido/criado para a unidade informada
        assertNotNull(repoBloco.findByUnidadeAndNome(unidade("LIMOEIRO DO NORTE", "CIDADE ALTA"), "BLOCO 11"))
    }

    @Test
    fun `Deve criar campus unidade e bloco em cascata e reusar nas chamadas seguintes`() {
        val primeiro = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(
                nome = "Sala Cascata 1",
                campus = "Campus Cascata",
                unidade = "Unidade Cascata",
                bloco = "Bloco 07",
                andar = 0
            )
        )

        assertEquals("CAMPUS CASCATA", primeiro.localizacao.campus.nome)
        assertEquals("UNIDADE CASCATA", primeiro.localizacao.unidade.nome)
        assertEquals("BLOCO 7", primeiro.localizacao.bloco.nome)

        val segundo = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(
                nome = "Sala Cascata 2",
                campus = "CAMPUS  CASCATA",
                unidade = "unidade cascata",
                bloco = "bloco 0007",
                andar = 1
            )
        )

        // Normalização de caixa/acento/zeros à esquerda garante o reuso da hierarquia
        assertEquals(primeiro.localizacao.campus.id, segundo.localizacao.campus.id)
        assertEquals(primeiro.localizacao.unidade.id, segundo.localizacao.unidade.id)
        assertEquals(primeiro.localizacao.bloco.id, segundo.localizacao.bloco.id)
        assertTrue(primeiro.localizacao.id != segundo.localizacao.id)
    }

    @Test
    fun `Deve rejeitar unidade que nao pertence ao campus informado`() {
        val campusLimoeiro = criarCampus(repoCampus, "LIMOEIRO DO NORTE")
        val unidadeOutroCampus = unidade("FORTALEZA", "UEPE")

        val req = criarSalaAula(nome = "Sala Unidade Divergente").copy(
            localizacao = LocalizacaoReq(
                campus = refCampus(campusLimoeiro),
                unidade = refUnidade(unidadeOutroCampus),
                bloco = refNome("BLOCO NOVO"),
                andar = 1
            )
        )

        val erro = assertThrows<IllegalArgumentException> {
            ambientesNPUseCases.cadastrarAmbiente(req)
        }

        assertEquals("A unidade não pertence ao campus informado.", erro.message)
    }

    @Test
    fun `Deve rejeitar bloco que nao pertence a unidade informada`() {
        val campus = criarCampus(repoCampus, "LIMOEIRO DO NORTE")
        val sede = unidade("LIMOEIRO DO NORTE", "SEDE")
        val cidadeAlta = unidade("LIMOEIRO DO NORTE", "CIDADE ALTA")
        val blocoDaSede = criarBloco(repoBloco, sede, "BLOCO DA SEDE")

        val req = criarSalaAula(nome = "Sala Bloco Divergente").copy(
            localizacao = LocalizacaoReq(
                campus = refCampus(campus),
                unidade = refUnidade(cidadeAlta),
                bloco = refBloco(blocoDaSede),
                andar = 1
            )
        )

        val erro = assertThrows<IllegalArgumentException> {
            ambientesNPUseCases.cadastrarAmbiente(req)
        }

        assertEquals("O bloco não pertence à unidade informada.", erro.message)
    }

    @Test
    fun `Deve rejeitar campus ou unidade inexistente informado por id`() {
        val campus = criarCampus(repoCampus, "LIMOEIRO DO NORTE")
        val cidadeAlta = unidade("LIMOEIRO DO NORTE", "CIDADE ALTA")

        val campusInexistente = criarSalaAula(nome = "Sala Campus Inexistente").copy(
            localizacao = LocalizacaoReq(
                campus = ReferenciaReq(id = Long.MAX_VALUE),
                unidade = refUnidade(cidadeAlta),
                bloco = refNome("BLOCO NOVO"),
                andar = 1
            )
        )
        val erroCampus = assertThrows<IllegalArgumentException> {
            ambientesNPUseCases.cadastrarAmbiente(campusInexistente)
        }
        assertEquals("Campus inválido: ${Long.MAX_VALUE}.", erroCampus.message)

        val unidadeInexistente = criarSalaAula(nome = "Sala Unidade Inexistente").copy(
            localizacao = LocalizacaoReq(
                campus = refCampus(campus),
                unidade = ReferenciaReq(id = Long.MAX_VALUE),
                bloco = refNome("BLOCO NOVO"),
                andar = 1
            )
        )
        val erroUnidade = assertThrows<IllegalArgumentException> {
            ambientesNPUseCases.cadastrarAmbiente(unidadeInexistente)
        }
        assertEquals("Unidade inválida: ${Long.MAX_VALUE}.", erroUnidade.message)
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
    fun `Deve filtrar ambientes nao publicados por campus unidade bloco e andar`() {
        val blocoCidadeAlta = bloco("LIMOEIRO DO NORTE", "CIDADE ALTA", "BLOCO 10")
        val blocoSede = bloco("LIMOEIRO DO NORTE", "SEDE", "BLOCO 11")
        val blocoUepe = bloco("FORTALEZA", "UEPE", "BLOCO 12")

        val campusLimoeiro = criarCampus(repoCampus, "LIMOEIRO DO NORTE")
        val campusFortaleza = criarCampus(repoCampus, "FORTALEZA")
        val unidadeCidadeAlta = unidade("LIMOEIRO DO NORTE", "CIDADE ALTA")
        val unidadeSede = unidade("LIMOEIRO DO NORTE", "SEDE")
        val unidadeUepe = unidade("FORTALEZA", "UEPE")

        val ambienteCidadeAlta = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala Cidade Alta").copy(
                localizacao = localizacaoReq(campusLimoeiro, unidadeCidadeAlta, blocoCidadeAlta, 1)
            )
        )
        val ambienteSede = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala Sede").copy(
                localizacao = localizacaoReq(campusLimoeiro, unidadeSede, blocoSede, 2)
            )
        )
        val ambienteUepe = ambientesNPUseCases.cadastrarAmbiente(
            criarSalaAula(nome = "Sala UEPE").copy(
                localizacao = localizacaoReq(campusFortaleza, unidadeUepe, blocoUepe, 1)
            )
        )

        // Só por campus
        val porCampus = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(
                campusId = campusLimoeiro.id,
                unidadeId = null,
                blocoId = null,
                andar = null
            ),
            PageRequest.of(0, 10)
        )
        assertEquals(2, porCampus.dadosPaginacao.totalElements)
        assertEquals(setOf("Sala Cidade Alta", "Sala Sede"), porCampus.ambientes.map { it.nome }.toSet())

        // Só por unidade
        val porUnidade = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(campusId = null, unidadeId = unidadeSede.id, blocoId = null, andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(1, porUnidade.dadosPaginacao.totalElements)
        assertEquals(ambienteSede.nome, porUnidade.ambientes.first().nome)

        // Só por bloco
        val porBloco = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(campusId = null, unidadeId = null, blocoId = blocoCidadeAlta.id, andar = null),
            PageRequest.of(0, 10)
        )
        assertEquals(1, porBloco.dadosPaginacao.totalElements)
        assertEquals(ambienteCidadeAlta.nome, porBloco.ambientes.first().nome)

        // Combinação completa
        val combinado = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(
                campusId = campusFortaleza.id,
                unidadeId = unidadeUepe.id,
                blocoId = blocoUepe.id,
                andar = 1
            ),
            PageRequest.of(0, 10)
        )
        assertEquals(1, combinado.dadosPaginacao.totalElements)
        assertEquals(ambienteUepe.nome, combinado.ambientes.first().nome)

        // Só por andar
        val porAndar = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(campusId = null, unidadeId = null, blocoId = null, andar = 1),
            PageRequest.of(0, 10)
        )
        assertEquals(2, porAndar.dadosPaginacao.totalElements)

        // Combinação divergente: campus x unidade de outro campus -> página vazia
        val divergente = ambientesNPUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(
                campusId = campusFortaleza.id,
                unidadeId = unidadeSede.id,
                blocoId = null,
                andar = null
            ),
            PageRequest.of(0, 10)
        )
        assertEquals(0, divergente.dadosPaginacao.totalElements)
        assertTrue(divergente.ambientes.isEmpty())
    }

    @Test
    fun `Deve retornar pagina vazia para ids de localizacao inexistentes`() {
        ambientesNPUseCases.cadastrarAmbiente(criarSalaAula())

        listOf(
            LocalizacaoPesquisaReq(campusId = Long.MAX_VALUE, unidadeId = null, blocoId = null, andar = null),
            LocalizacaoPesquisaReq(campusId = null, unidadeId = Long.MAX_VALUE, blocoId = null, andar = null),
            LocalizacaoPesquisaReq(campusId = null, unidadeId = null, blocoId = Long.MAX_VALUE, andar = null)
        ).forEach { filtro ->
            val resultado = ambientesNPUseCases.listarAmbientesPorLocalizacao(filtro, PageRequest.of(0, 10))
            assertEquals(0, resultado.dadosPaginacao.totalElements)
            assertTrue(resultado.ambientes.isEmpty())
        }
    }

    @Test
    fun `Deve exigir ao menos um filtro de localizacao`() {
        assertThrows<IllegalArgumentException> {
            ambientesNPUseCases.listarAmbientesPorLocalizacao(
                LocalizacaoPesquisaReq(campusId = null, unidadeId = null, blocoId = null, andar = null),
                PageRequest.of(0, 10)
            )
        }
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
                campus = ReferenciaReq(id = ambiente1.localizacao.campus.id),
                unidade = ReferenciaReq(id = ambiente1.localizacao.unidade.id),
                bloco = ReferenciaReq(id = ambiente1.localizacao.bloco.id),
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
            criarSalaAula(nome = "Sala Excluir", bloco = "BLOCO 12", unidade = "SEDE", andar = 8)
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
