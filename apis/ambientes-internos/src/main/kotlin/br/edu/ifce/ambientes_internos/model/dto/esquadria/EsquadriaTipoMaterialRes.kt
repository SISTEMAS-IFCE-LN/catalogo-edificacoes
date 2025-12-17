package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import java.math.BigDecimal

data class EsquadriaTipoMaterialRes(
    val tipo: TipoEsquadria,
    val material: MaterialEsquadria,
    val area: BigDecimal
) {
    companion object {
        fun from(mapaTipoMaterial: Map.Entry<Pair<TipoEsquadria, MaterialEsquadria>, BigDecimal>): EsquadriaTipoMaterialRes {
            return EsquadriaTipoMaterialRes(
                tipo = mapaTipoMaterial.key.first,
                material = mapaTipoMaterial.key.second,
                area = mapaTipoMaterial.value
            )
        }
    }
}
