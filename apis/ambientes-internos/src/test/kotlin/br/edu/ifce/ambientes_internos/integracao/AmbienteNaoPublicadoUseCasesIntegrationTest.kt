package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
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
import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.AmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteBasicoReq
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteBasicoRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.ListaGeometriasAmbienteRes
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals

@DataJpaTest
@Import(AmbienteNaoPublicadoUseCases::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para os Casos de Uso dos Ambientes")
class AmbienteNaoPublicadoUseCasesIntegrationTest {

    @Autowired
    lateinit var ambientesNPUseCases: IAmbienteNaoPublicadoUseCases

    lateinit var ambienteReq: AmbienteReq
    lateinit var salaAula: SalaAula
    lateinit var ambienteRes: AmbienteRes

    @BeforeEach
    fun setup() {
        // Dados - Modelo de requisição
        ambienteReq = AmbienteReq(
            tipo = TipoAmbiente.SALA_AULA,
            nome = "Sala de Aula Exemplo",
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

    @Test
    fun `Deve cadastrar e recuperar ambiente por id`() {
        // Quando um ambiente for cadastrado
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Então o ambiente deve ser persistido e retornado ao usuário
        assertEquals(ambienteRes, ambienteSalvo)

        // Quando o ambiente cadastrado for recuperado pelo seu ID
        val ambienteRecuperadoPorId = ambientesNPUseCases.obterAmbientePorId(ambienteSalvo.id)

        // Então o ambiente recuperado deve ser igual ao ambiente salvo anteriormente
        assertEquals(ambienteRes, ambienteRecuperadoPorId)
    }

    @Test
    fun `Deve atualizar dados basicos do ambiente`() {
        //Dados - Modelo de requisição para atualização de dados básicos do ambiente
        val ambienteBasicoReq = AmbienteBasicoReq(
            nome = "Sala de Aula Atualizada",
            localizacao = LocalizacaoReq(unidade = Unidade.CIDADE_ALTA, bloco = Bloco.BLOCO_10, andar = 2),
            capacidade = 35
        )

        // Dados - Modelo de resposta esperado após a atualização
        val ambienteBasicoRes = AmbienteBasicoRes(
            id = ambienteRes.id,
            nome = ambienteBasicoReq.nome,
            localizacao = LocalizacaoRes(
                id = ambienteRes.localizacao.id,
                unidade = ambienteBasicoReq.localizacao.unidade,
                bloco = ambienteBasicoReq.localizacao.bloco,
                andar = ambienteBasicoReq.localizacao.andar
            ),
            capacidade = ambienteBasicoReq.capacidade,
            area = ambienteRes.areaAmbiente
        )

        // Dados - ambiente cadastrado
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Quando o ambiente salvo for atualizado
        val ambienteBasicoAtualizado =
            ambientesNPUseCases.atualizarDadosBasicosAmbiente(ambienteSalvo.id, ambienteBasicoReq)

        // Então os dados atualizados devem ser retornados ao usuário
        assertEquals(ambienteBasicoRes, ambienteBasicoAtualizado)
    }

    @Test
    fun `Deve incluir geometrias no ambiente`() {
        // Dados - Geometria a serem incluídas no ambiente
        val geometriasParaIncluir = setOf(
            GeometriaAmbienteReq(
                tipo = TipoGeometria.RETANGULAR, base = BigDecimal("4.0"), altura = BigDecimal("3.0"),
                repeticao = 2
            ),
            GeometriaAmbienteReq(
                tipo = TipoGeometria.TRIANGULAR, base = BigDecimal("2.5"), altura = BigDecimal("2.2")
            )
        )

        // Dados - Criação da lista de geometrias atualizadas do ambiente
        val geometriasAtualizadas = mutableSetOf<GeometriaAmbienteRes>()

        geometriasAtualizadas.addAll(salaAula.geometrias.map {
            GeometriaAmbienteRes(
                id = 0L,
                tipo = it.tipo,
                base = it.base,
                altura = it.altura,
                repeticao = it.repeticao,
                area = it.calcularAreaTotalM2()
            )
        })

        geometriasAtualizadas.addAll(
            geometriasParaIncluir.map {
                GeometriaAmbienteRes(
                    id = 0L,
                    tipo = it.tipo,
                    base = it.base,
                    altura = it.altura,
                    repeticao = it.repeticao,
                    area = when (it.tipo) {
                        TipoGeometria.RETANGULAR ->
                            it.base.multiply(it.altura)
                                .multiply(BigDecimal(it.repeticao))
                                .setScale(2, RoundingMode.HALF_UP)

                        TipoGeometria.TRIANGULAR -> it.base.multiply(it.altura)
                            .divide(BigDecimal("2"))
                            .multiply(BigDecimal(it.repeticao))
                            .setScale(2, RoundingMode.HALF_UP)
                    }
                )
            }
        )

        // Dados - Modelo de resposta esperado após a inclusão das geometrias
        val listaGeometriasEsperadas = ListaGeometriasAmbienteRes(
            geometrias = geometriasAtualizadas.toList(),
            areaTotal = geometriasAtualizadas.fold(BigDecimal.ZERO) { acc, geometria ->
                acc + geometria.area
            }
        )

        // Dados - ambiente cadastrado
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Quando novas geometrias forem adicionadas ao ambiente
        val listaGeometriasAtualizadas =
            ambientesNPUseCases.incluirGeometriasAmbiente(ambienteSalvo.id, geometriasParaIncluir)

        // Então as geometrias atualizadas devem ser retornadas ao usuário
        assertEquals(listaGeometriasEsperadas, listaGeometriasAtualizadas)
    }

    @Test
    fun `Deve atualizar geometrias do ambiente`() {
        // Dados - Geometria para substituir a existente no ambiente
        val geometriasParaSubstituir = setOf(
            GeometriaAmbienteReq(
                tipo = TipoGeometria.RETANGULAR, base = BigDecimal("4.5"), altura = BigDecimal("3.2"),
                repeticao = 2
            )
        )

        // Dados - lista de geometrias esperada apenas com as geometrias fornecidas (substituição completa)
        val geometriasEsperadas = geometriasParaSubstituir.map {
            GeometriaAmbienteRes(
                id = 0L,
                tipo = it.tipo,
                base = it.base,
                altura = it.altura,
                repeticao = it.repeticao,
                area = it.base.multiply(it.altura)
                    .multiply(BigDecimal(it.repeticao))
                    .setScale(2, RoundingMode.HALF_UP)
            )
        }

        val respostaEsperada = ListaGeometriasAmbienteRes(
            geometrias = geometriasEsperadas,
            areaTotal = geometriasEsperadas.first().area
        )

        // Dados - ambiente cadastrado
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Quando as geometrias do ambiente forem atualizadas
        val listaGeometriasAtualizadas =
            ambientesNPUseCases.atualizarGeometriasAmbiente(ambienteSalvo.id, geometriasParaSubstituir)

        // Então as geometrias retornadas devem corresponder exatamente às fornecidas
        assertEquals(respostaEsperada, listaGeometriasAtualizadas)
    }

    @Test
    fun `Deve incluir pes direitos no ambiente`() {
        // Dados - pés-direitos a serem incluídos
        val pesDireitosParaIncluir = setOf(BigDecimal("3.5"), BigDecimal("4.0"))

        // Dados - ambiente cadastrado
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Quando novos pés-direitos forem incluídos
        val pesDireitosRetornados =
            ambientesNPUseCases.incluirPesDireitosAmbiente(ambienteSalvo.id, pesDireitosParaIncluir)

        // Então o conjunto retornado deve conter os originais e os adicionados
        val pesDireitosEsperados = salaAula.pesDireitos + pesDireitosParaIncluir
        assertEquals(pesDireitosEsperados, pesDireitosRetornados)
    }

    @Test
    fun `Deve atualizar pes direitos do ambiente`() {
        // Dados - novos pés-direitos para substituir os existentes
        val pesDireitosParaAtualizar = setOf(BigDecimal("2.8"), BigDecimal("3.2"))

        // Dados - ambiente cadastrado
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Quando os pés-direitos do ambiente forem atualizados
        val pesDireitosRetornados =
            ambientesNPUseCases.atualizarPesDireitosAmbiente(ambienteSalvo.id, pesDireitosParaAtualizar)

        // Então o conjunto retornado deve corresponder exatamente ao fornecido
        assertEquals(pesDireitosParaAtualizar, pesDireitosRetornados)
    }

}