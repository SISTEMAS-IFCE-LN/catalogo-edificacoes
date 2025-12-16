package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.domain.geometrias.enums.TipoGeometria
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
import br.edu.ifce.ambientes_internos.model.use_cases.AmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.service.AmbienteNaoPublicadoService
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@DataJpaTest
@Import(AmbienteNaoPublicadoService::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para os Casos de Uso dos Ambientes")
class AmbienteUseCasesIntegrationTest {

    @Autowired
    lateinit var ambientesNPUseCases: AmbienteNaoPublicadoUseCases

    @Test
    fun `Deve validar casos de uso dos ambientes nao publicados`() {
        // Dados - Modelo de requisição
        val ambienteReq = AmbienteReq(
            tipo = TipoAmbiente.SALA_AULA, nome = "Sala de Aula Exemplo", localizacao = LocalizacaoReq(
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
                    ), material = MaterialEsquadria.MADEIRA_FICHA, informacaoAdicional = "Porta de abrir"
                )
            ), informacaoAdicional = "Sala equipada com projetor e quadro branco."
        )
        // Dados - Modelo de entidade salva do banco
        val salaAula = SalaAula(
            nome = ambienteReq.nome,
            localizacao = Localizacao(
                bloco = ambienteReq.localizacao.bloco,
                unidade = ambienteReq.localizacao.unidade,
                andar = ambienteReq.localizacao.andar
            ),
            capacidade = ambienteReq.capacidade,
            geometrias = ambienteReq.geometrias.map {
                Retangular(
                    base = it.base,
                    altura = it.altura
                ) as Geometria
            }.toMutableSet(),
            esquadrias = ambienteReq.esquadrias.map {
                when (it.tipo) {
                    TipoEsquadria.JANELA -> Janela(
                        geometria = Retangular(
                            base = it.geometria.base,
                            altura = it.geometria.altura
                        ),
                        material = it.material,
                        alturaPeitoril = it.alturaPeitoril,
                        informacaoAdicional = it.informacaoAdicional
                    )

                    TipoEsquadria.PORTA -> Porta(
                        geometria = Retangular(
                            base = it.geometria.base,
                            altura = it.geometria.altura
                        ),
                        material = it.material,
                        informacaoAdicional = it.informacaoAdicional
                    )

                    else -> throw IllegalArgumentException("Tipo de esquadria desconhecido: ${it.tipo}")
                }
            }.toMutableSet(),
            pesDireitos = ambienteReq.pesDireitos.toMutableSet(),
            informacaoAdicional = ambienteReq.informacaoAdicional
        )

        // Dados - Modelo da resposta que deve ser retornada para o usuário.
        val ambienteRes = AmbienteRes(
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
            esquadriasDetalhes = EsquadriasDetalhesRes(
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
                    EsquadriaTipoMaterialRes(
                        tipo = it.key.first,
                        material = it.key.second,
                        area = it.value
                    )
                }
            ),
            informacaoAdicional = salaAula.informacaoAdicional,
            status = salaAula.status
        )

        // Quando um ambiente for cadastrado
        val resultado = ambientesNPUseCases.cadastrarAmbiente(ambienteReq)

        // Então o ambiente deve ser persistido e retornado ao usuário
        assertEquals(ambienteRes, resultado)
    }

}