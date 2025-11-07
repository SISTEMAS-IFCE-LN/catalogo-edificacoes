package br.edu.ifce.ambientes_internos.model.dto.ambiente

data class AmbienteBasicoRes(
    val id: Long,
    val nome: String,
    val localizacao: String,
    val capacidade: Int,
)
