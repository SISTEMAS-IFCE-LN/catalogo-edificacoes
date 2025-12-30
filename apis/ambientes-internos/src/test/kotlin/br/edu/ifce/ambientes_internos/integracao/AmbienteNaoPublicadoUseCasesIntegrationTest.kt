package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.AmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.LaboratorioInformatica
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.enums.TipoGeometria
import br.edu.ifce.ambientes_internos.model.domain.factory.AmbienteFactory
import br.edu.ifce.ambientes_internos.model.dto.ambiente.*
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaTipoMaterialRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
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

    @Test
    fun `Deve incluir esquadrias no ambiente`() {
        // Dados - Esquadrias a serem incluídas no ambiente
        val esquadriasParaIncluir = setOf(
            EsquadriaReq(
                tipo = TipoEsquadria.JANELA,
                geometria = GeometriaEsquadriaReq(
                    base = BigDecimal("1.2"), altura = BigDecimal("1.2"), repeticao = 2
                ),
                material = MaterialEsquadria.ALUMINIO,
                alturaPeitoril = BigDecimal("0.80")
            )
        )

        // Dados - Criação da lista de esquadrias atualizadas do ambiente
        salaAula.esquadrias.addAll(esquadriasParaIncluir.map {
            Janela(
                geometria = Retangular(
                    base = it.geometria.base,
                    altura = it.geometria.altura,
                    repeticao = it.geometria.repeticao
                ),
                material = it.material,
                alturaPeitoril = it.alturaPeitoril
            )
        })

        // Dados - Modelo de resposta esperado após a inclusão das esquadrias
        val esquadriasDetalhesRes = EsquadriasDetalhesRes(
            esquadrias = salaAula.esquadrias.map {
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
            },
            esquadriasTipoMaterial = salaAula.calcularAreaEsquadriasPorTipoMaterial().map {
                EsquadriaTipoMaterialRes(tipo = it.key.first, material = it.key.second, area = it.value)
            }
        )

        // Dados - ambiente cadastrado
        val ambienteRes = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Quando novas esquadrias forem adicionadas ao ambiente
        val esquadriasDetalhesAtualizados = ambientesNPUseCases.incluirEsquadriasAmbiente(
            ambienteRes.id,
            esquadriasParaIncluir
        )

        // Então as esquadrias atualizadas devem ser retornadas ao usuário
        assertEquals(esquadriasDetalhesRes, esquadriasDetalhesAtualizados)
    }

    @Test
    fun `Deve atualizar as esquadrias no ambiente`() {
        // Dados - Esquadrias a serem incluídas no ambiente
        val esquadriasParaAtualizar = setOf(
            EsquadriaReq(
                tipo = TipoEsquadria.PORTA,
                geometria = GeometriaEsquadriaReq(
                    base = BigDecimal("0.8"), altura = BigDecimal("2.1")
                ),
                material = MaterialEsquadria.MADEIRA_FICHA
            )
        )

        // Dados - Criação da lista de esquadrias atualizadas do ambiente
        salaAula.esquadrias.clear()
        salaAula.esquadrias.addAll(esquadriasParaAtualizar.map {
            Porta(
                geometria = Retangular(
                    base = it.geometria.base,
                    altura = it.geometria.altura,
                    repeticao = it.geometria.repeticao
                ),
                material = it.material,
                informacaoAdicional = it.informacaoAdicional
            )
        })

        // Dados - Modelo de resposta esperado após a inclusão das esquadrias
        val esquadriasDetalhesRes = EsquadriasDetalhesRes(
            esquadrias = salaAula.esquadrias.map {
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
                    alturaPeitoril = BigDecimal.ZERO,
                    area = it.geometria.calcularAreaTotalM2(),
                    material = it.material,
                    informacaoAdicional = it.informacaoAdicional
                )
            },
            esquadriasTipoMaterial = salaAula.calcularAreaEsquadriasPorTipoMaterial().map {
                EsquadriaTipoMaterialRes(tipo = it.key.first, material = it.key.second, area = it.value)
            }
        )

        // Dados - ambiente cadastrado
        val ambienteRes = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Quando novas esquadrias forem adicionadas ao ambiente
        val esquadriasDetalhesAtualizados = ambientesNPUseCases.atualizarEsquadriasAmbiente(
            ambienteRes.id,
            esquadriasParaAtualizar
        )

        // Então as esquadrias atualizadas devem ser retornadas ao usuário
        assertEquals(esquadriasDetalhesRes, esquadriasDetalhesAtualizados)
    }

    @Test
    fun `Deve atualizar informacao adicional do ambiente`() {
        // Dados - nova informação adicional para o ambiente
        val informacaoAdicionalAtualizada = "Sala equipada com ar-condicionado e projetor."

        // Dados - ambiente cadastrado
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Quando a informação adicional do ambiente for atualizada
        val informacaoAdicionalRetornada =
            ambientesNPUseCases.atualizarInformacaoAdicionalAmbiente(ambienteSalvo.id, informacaoAdicionalAtualizada)

        // Então a informação adicional retornada deve corresponder à fornecida
        assertEquals(informacaoAdicionalAtualizada, informacaoAdicionalRetornada)
    }

    @Test
    fun `Deve alterar tipo e dados do ambiente`() {
        // Dados - Modelo de requisição para alteração do tipo e dados do ambiente
        val ambienteAlteracaoReq = AmbienteReq(
            tipo = TipoAmbiente.LABORATORIO_INFORMATICA,
            nome = "Laboratório de Informática Exemplo",
            localizacao = LocalizacaoReq(
                unidade = Unidade.CIDADE_ALTA, bloco = Bloco.BLOCO_10, andar = 1
            ), capacidade = 25, geometrias = setOf(
                GeometriaAmbienteReq(
                    tipo = TipoGeometria.RETANGULAR, base = BigDecimal("8.0"), altura = BigDecimal("4.0")
                )
            ), pesDireitos = setOf(BigDecimal("3.5")), esquadrias = setOf(
                EsquadriaReq(
                    tipo = TipoEsquadria.PORTA,
                    geometria = GeometriaEsquadriaReq(
                        base = BigDecimal("0.8"), altura = BigDecimal("2.10")
                    ),
                    material = MaterialEsquadria.ALUMINIO
                ),
                EsquadriaReq(
                    tipo = TipoEsquadria.JANELA, geometria = GeometriaEsquadriaReq(
                        base = BigDecimal("1.2"), altura = BigDecimal("1.0")
                    ),
                    material = MaterialEsquadria.ALUMINIO, alturaPeitoril = BigDecimal("1.0")
                )
            ), informacaoAdicional = "Laboratório equipado com computadores e projetor."
        )

        // Dados - ambiente para salvar a partir da requisição de alteração
        val laboratorioInformatica = LaboratorioInformatica(
            nome = ambienteAlteracaoReq.nome,
            localizacao = Localizacao(
                bloco = ambienteAlteracaoReq.localizacao.bloco,
                unidade = ambienteAlteracaoReq.localizacao.unidade,
                andar = ambienteAlteracaoReq.localizacao.andar
            ),
            capacidade = ambienteAlteracaoReq.capacidade,
            geometrias = ambienteAlteracaoReq.geometrias.map {
                Retangular(
                    base = it.base, altura = it.altura
                ) as Geometria
            }.toMutableSet(),
            esquadrias = ambienteAlteracaoReq.esquadrias.map {
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
            pesDireitos = ambienteAlteracaoReq.pesDireitos.toMutableSet(),
            informacaoAdicional = ambienteAlteracaoReq.informacaoAdicional
        )

        // Dados - ambiente alterado esperado
        val ambienteEsperado = AmbienteRes(
            id = 0L,
            tipo = laboratorioInformatica.tipo!!,
            nome = laboratorioInformatica.nome,
            localizacao = LocalizacaoRes(
                id = 0L,
                bloco = laboratorioInformatica.localizacao.bloco,
                unidade = laboratorioInformatica.localizacao.unidade,
                andar = laboratorioInformatica.localizacao.andar
            ),
            capacidade = laboratorioInformatica.capacidade,
            geometrias = laboratorioInformatica.geometrias.map {
                GeometriaAmbienteRes(
                    id = 0L,
                    tipo = it.tipo,
                    base = it.base,
                    altura = it.altura,
                    repeticao = it.repeticao,
                    area = it.calcularAreaTotalM2()
                )
            },
            areaAmbiente = laboratorioInformatica.calcularAreaAmbienteM2(),
            pesDireitos = laboratorioInformatica.pesDireitos.toList(),
            esquadriasDetalhes = EsquadriasDetalhesRes(esquadrias = laboratorioInformatica.esquadrias.map {
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
            }, esquadriasTipoMaterial = laboratorioInformatica.calcularAreaEsquadriasPorTipoMaterial().map {
                EsquadriaTipoMaterialRes(
                    tipo = it.key.first, material = it.key.second, area = it.value
                )
            }),
            informacaoAdicional = laboratorioInformatica.informacaoAdicional,
            status = laboratorioInformatica.status
        )

        // Dados - ambiente cadastrado
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Quando o tipo e dados do ambiente forem alterados
        val ambienteAlterado = ambientesNPUseCases.alterarTipoDadosAmbiente(ambienteSalvo.id, ambienteAlteracaoReq)

        // Então não deve ocorrer nenhuma exceção durante o processo
        assertEquals(ambienteEsperado, ambienteAlterado)
        assertEquals(ambienteSalvo.localizacao, ambienteAlterado.localizacao)
        assertThrows <NoSuchElementException> { ambientesNPUseCases.obterAmbientePorId(ambienteSalvo.id) }
    }

    @Test
    fun `Deve duplicar um ambiente cadastrado`() {
        // Dados - Modelo requisição para duplicação do ambiente
        val ambienteNomeLocalizacaoReq = AmbienteNomeLocalizacaoReq(
            nome = "Sala de Aula Duplicada",
            localizacao = LocalizacaoReq(
                unidade = Unidade.CIDADE_ALTA, bloco = Bloco.BLOCO_10, andar = 1
            )
        )

        // Dados - Modelo de resposta esperado após a duplicação do ambiente
        val salaAulaDuplicada = SalaAula(
            nome = ambienteNomeLocalizacaoReq.nome,
            localizacao = Localizacao(
                bloco = ambienteNomeLocalizacaoReq.localizacao.bloco,
                unidade = ambienteNomeLocalizacaoReq.localizacao.unidade,
                andar = ambienteNomeLocalizacaoReq.localizacao.andar
            ),
            capacidade = salaAula.capacidade,
            geometrias = salaAula.geometrias.map {
                Retangular(
                    base = it.base, altura = it.altura
                ) as Geometria
            }.toMutableSet(),
            esquadrias = salaAula.esquadrias.map {
                when (it.tipo) {
                    TipoEsquadria.JANELA -> Janela(
                        geometria = Retangular(
                            base = it.geometria.base, altura = it.geometria.altura
                        ),
                        material = it.material,
                        alturaPeitoril = if (it is Janela) it.alturaPeitoril else BigDecimal.ZERO,
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
            pesDireitos = salaAula.pesDireitos.toMutableSet(),
            informacaoAdicional = salaAula.informacaoAdicional
        )

        val ambienteDuplicadoEsperado = AmbienteRes(
            id = 0L,
            tipo = salaAulaDuplicada.tipo!!,
            nome = salaAulaDuplicada.nome,
            localizacao = LocalizacaoRes(
                id = 0L,
                bloco = salaAulaDuplicada.localizacao.bloco,
                unidade = salaAulaDuplicada.localizacao.unidade,
                andar = salaAulaDuplicada.localizacao.andar
            ),
            capacidade = salaAulaDuplicada.capacidade,
            geometrias = salaAulaDuplicada.geometrias.map {
                GeometriaAmbienteRes(
                    id = 0L,
                    tipo = it.tipo,
                    base = it.base,
                    altura = it.altura,
                    repeticao = it.repeticao,
                    area = it.calcularAreaTotalM2()
                )
            },
            areaAmbiente = salaAulaDuplicada.calcularAreaAmbienteM2(),
            pesDireitos = salaAulaDuplicada.pesDireitos.toList(),
            esquadriasDetalhes = EsquadriasDetalhesRes(esquadrias = salaAulaDuplicada.esquadrias.map {
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
            }, esquadriasTipoMaterial = salaAulaDuplicada.calcularAreaEsquadriasPorTipoMaterial().map {
                EsquadriaTipoMaterialRes(
                    tipo = it.key.first, material = it.key.second, area = it.value
                )
            }),
            informacaoAdicional = salaAulaDuplicada.informacaoAdicional,
            status = salaAulaDuplicada.status
        )

        // Dados - ambiente cadastrado
        val ambienteSalvo = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Quando o ambiente for duplicado
        val ambienteDuplicado = ambientesNPUseCases.duplicarAmbiente(ambienteSalvo.id, ambienteNomeLocalizacaoReq)

        // Então o ambiente duplicado deve ser retornado ao usuário
        assertEquals(ambienteDuplicadoEsperado, ambienteDuplicado)
    }

}