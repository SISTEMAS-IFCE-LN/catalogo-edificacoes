package br.edu.ifce.ambientes_internos.model.dto.ambiente

data class LocalizacaoPesquisaReq(
    val campusId: Long?,
    val unidadeId: Long?,
    val blocoId: Long?,
    val andar: Int?
)
