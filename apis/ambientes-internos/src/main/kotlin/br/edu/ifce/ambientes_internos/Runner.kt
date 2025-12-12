package br.edu.ifce.ambientes_internos

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.enums.TipoGeometria
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaReq
import br.edu.ifce.ambientes_internos.model.use_cases.AmbienteNaoPublicadoUseCase
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
class Runner(val ambienteNaoPublicadoUseCase: AmbienteNaoPublicadoUseCase) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {

        val ambienteReq = AmbienteReq(
            tipo = TipoAmbiente.SALA_AULA,
            nome = "Sala de Aula Exemplo",
            localizacao = LocalizacaoReq(
                unidade = Unidade.CIDADE_ALTA,
                bloco = Bloco.BLOCO_10,
                andar = 1
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
                    tipo = TipoEsquadria.JANELA,
                    geometria = GeometriaEsquadriaReq(
                        base = BigDecimal("1.5"),
                        altura = BigDecimal("1.2")
                    ),
                    material = MaterialEsquadria.ALUMINIO,
                    alturaPeitoril = BigDecimal("0.90"),
                    informacaoAdicional = "Janela de correr"
                ),
                EsquadriaReq(
                    tipo = TipoEsquadria.PORTA,
                    geometria = GeometriaEsquadriaReq(
                        base = BigDecimal("0.9"),
                        altura = BigDecimal("2.1")
                    ),
                    material = MaterialEsquadria.MADEIRA_FICHA,
                    informacaoAdicional = "Porta de abrir"
                )
            ),
            informacaoAdicional = "Sala equipada com projetor e quadro branco."
        )

        val ambiente = ambienteNaoPublicadoUseCase.cadastrarAmbiente(ambienteReq)

        println(ambiente)

    }

}