package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaRes

data class EsquadriaRes(
    val id: Long,
    val tipo: TipoEsquadria,
    val geometria: GeometriaEsquadriaRes,
    val material: MaterialEsquadria,
    val informacaoAdicional: String
) {
    companion object {
        fun from(esquadria: Esquadria): EsquadriaRes {
            return EsquadriaRes(
                id = esquadria.id!!,
                tipo = esquadria.tipo,
                geometria = GeometriaEsquadriaRes.from(esquadria.geometria),
                material = esquadria.material,
                informacaoAdicional = esquadria.informacaoAdicional
            )
        }
    }
}