package br.edu.ifce.ambientes_internos.model.dto.ambiente

data class AmbienteBasicoReq(
    val nome: String,
    val localizacao: String,
    val capacidade: Int,
)
