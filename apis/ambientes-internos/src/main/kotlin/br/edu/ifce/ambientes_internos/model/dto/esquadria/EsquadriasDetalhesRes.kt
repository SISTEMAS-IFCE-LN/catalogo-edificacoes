package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Ambiente

data class EsquadriasDetalhesRes(
    val esquadrias: List<EsquadriaRes>,
    val esquadriasTipoMaterial: List<EsquadriaTipoMaterialRes>
) {
    companion object {
        fun from(ambiente: Ambiente): EsquadriasDetalhesRes {
            return EsquadriasDetalhesRes(
                esquadrias = ambiente.esquadrias.map { EsquadriaRes.from(it) },
                esquadriasTipoMaterial = ambiente.calcularAreaEsquadriasPorTipoMaterial()
                    .map { EsquadriaTipoMaterialRes.from(it) }
            )
        }
    }
}
