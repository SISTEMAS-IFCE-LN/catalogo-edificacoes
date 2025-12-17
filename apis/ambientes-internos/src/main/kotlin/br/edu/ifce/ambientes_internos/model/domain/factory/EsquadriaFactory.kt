package br.edu.ifce.ambientes_internos.model.domain.factory

import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Cobogo
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.EsquadriaOutroAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.VaoAberto
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import java.math.BigDecimal

object EsquadriaFactory {

    private val ESQUADRIAS_MAP: Map<TipoEsquadria, (Retangular, MaterialEsquadria, BigDecimal, String) -> Esquadria> =
        mapOf(
            TipoEsquadria.JANELA to ::Janela,
            TipoEsquadria.PORTA to { geometria, material, alturaPeitoril, informacaoAdicional ->
                Porta(
                    geometria,
                    material,
                    informacaoAdicional
                )
            },
            TipoEsquadria.VAO_ABERTO to { geometria, material, alturaPeitoril, informacaoAdicional ->
                VaoAberto(
                    geometria,
                    informacaoAdicional
                )
            },
            TipoEsquadria.COBOGO to { geometria, material, alturaPeitoril, informacaoAdicional ->
                Cobogo(
                    geometria,
                    informacaoAdicional
                )
            },
            TipoEsquadria.ESQUADRIA_OUTRO_AMBIENTE to { geometria, material, alturaPeitoril, informacaoAdicional ->
                EsquadriaOutroAmbiente(
                    geometria,
                    informacaoAdicional
                )
            }
        )

    fun criar(esquadriaReq: EsquadriaReq): Esquadria {
        val construtor = ESQUADRIAS_MAP[esquadriaReq.tipo]
            ?: throw IllegalArgumentException("O tipo de esquadria é inválido.")

        val geometria = GeometriaFactory.criar(esquadriaReq.geometria) as Retangular

        return construtor(
            geometria,
            esquadriaReq.material,
            esquadriaReq.alturaPeitoril,
            esquadriaReq.informacaoAdicional
        )
    }

}