package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteNomeLocalizacaoRes

data class AmbienteEsquadriasRes(
    val dadosAmbiente: AmbienteNomeLocalizacaoRes,
    val detalhesEsquadrias: EsquadriasDetalhesRes
)

