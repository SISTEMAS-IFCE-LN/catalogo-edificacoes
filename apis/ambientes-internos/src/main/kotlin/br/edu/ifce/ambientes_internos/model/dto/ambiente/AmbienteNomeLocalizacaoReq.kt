package br.edu.ifce.ambientes_internos.model.dto.ambiente

data class AmbienteNomeLocalizacaoReq(
    val nome: String,
    val localizacao: LocalizacaoReq
)
