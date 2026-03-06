package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EsquadriasDetalhesRes) return false

        if (esquadrias.size != other.esquadrias.size) return false
        if (!esquadrias.containsAll(other.esquadrias)) return false

        if (esquadriasTipoMaterial.size != other.esquadriasTipoMaterial.size) return false
        if (!esquadriasTipoMaterial.containsAll(other.esquadriasTipoMaterial)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = esquadrias.hashCode()
        result = 31 * result + esquadriasTipoMaterial.hashCode()
        return result
    }
}
