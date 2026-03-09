package br.edu.ifce.ambientes_internos.model.dto.ambiente

import java.math.BigDecimal

data class AmbientesBasicosPaginadosRes(
    val ambientes: List<AmbienteBasicoRes>,
    val areaTotal: BigDecimal,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

