package br.edu.ifce.ambientes_internos.model.dto.geometria

import java.math.BigDecimal

data class ListaGeometriasAmbienteRes(
    val geometrias: List<GeometriaAmbienteRes>,
    val areaTotal: BigDecimal
)
