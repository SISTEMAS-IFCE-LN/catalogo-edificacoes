package br.edu.ifce.ambientes_internos.model.dto.ambiente

import java.math.BigDecimal

data class AmbientesBasicosRes(
    val ambientes: List<AmbienteBasicoRes>,
    val areaTotal: BigDecimal
)
