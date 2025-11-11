package br.edu.ifce.ambientes_internos.model.dto.ambiente

import java.math.BigDecimal

data class AmbienteBasicoRes(
    val id: Long,
    val nome: String,
    val localizacao: String,
    val capacidade: Int,
    val area: BigDecimal
)
