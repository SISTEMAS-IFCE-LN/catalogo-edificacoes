package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import java.math.BigDecimal

data class EsquadriaTipoMaterialRes(
    val tipo: TipoEsquadria,
    val material: MaterialEsquadria,
    val area: BigDecimal
)
