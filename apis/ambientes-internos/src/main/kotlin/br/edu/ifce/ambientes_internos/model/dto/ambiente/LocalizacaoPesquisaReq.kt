package br.edu.ifce.ambientes_internos.model.dto.ambiente

data class LocalizacaoPesquisaReq(
    val bloco: String?,
    val unidade: String?,
    val andar: Int?
)
