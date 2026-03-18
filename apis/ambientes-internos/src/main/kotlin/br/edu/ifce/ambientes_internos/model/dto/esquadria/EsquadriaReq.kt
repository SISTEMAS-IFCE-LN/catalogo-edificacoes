package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaReq
import java.math.BigDecimal

data class EsquadriaReq(
    val tipo: TipoEsquadria,
    val geometria: GeometriaEsquadriaReq,
    val material: MaterialEsquadria,
    val alturaPeitoril: BigDecimal = BigDecimal.ZERO,
    val informacaoAdicional: String = ""
)