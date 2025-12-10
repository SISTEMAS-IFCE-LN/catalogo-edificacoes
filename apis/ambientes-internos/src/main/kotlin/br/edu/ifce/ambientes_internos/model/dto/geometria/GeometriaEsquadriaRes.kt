package br.edu.ifce.ambientes_internos.model.dto.geometria

import br.edu.ifce.ambientes_internos.model.domain.geometrias.enums.TipoGeometria
import java.math.BigDecimal

data class GeometriaEsquadriaRes(
    val base: BigDecimal,
    val altura: BigDecimal,
    val repeticao: Int,
    val area: BigDecimal
)
