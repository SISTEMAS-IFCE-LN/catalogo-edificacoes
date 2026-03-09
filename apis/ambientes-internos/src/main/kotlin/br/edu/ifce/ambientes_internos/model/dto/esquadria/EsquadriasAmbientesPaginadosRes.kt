package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteNomeLocalizacaoRes

data class EsquadriasAmbientesPaginadosRes(
    val ambientes: Map<AmbienteNomeLocalizacaoRes, EsquadriasDetalhesRes>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
