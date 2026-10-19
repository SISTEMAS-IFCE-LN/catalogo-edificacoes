package br.edu.ifce.ambientes_internos.model.dto.ambiente

data class LocalizacaoPesquisaReq(
    val blocoId: Long?,
    val unidade: String?,
    val andar: Int?
)
