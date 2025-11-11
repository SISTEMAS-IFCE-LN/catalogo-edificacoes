package br.edu.ifce.ambientes_internos.model.dto.geometria

import java.math.BigDecimal

data class GeometriaEsquadriaReq(
    val base: BigDecimal,
    val altura: BigDecimal,
    val repeticao: Int = 1
)
