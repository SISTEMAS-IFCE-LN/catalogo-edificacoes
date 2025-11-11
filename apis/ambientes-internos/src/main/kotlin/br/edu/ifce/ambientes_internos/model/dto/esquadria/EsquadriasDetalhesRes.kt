package br.edu.ifce.ambientes_internos.model.dto.esquadria

data class EsquadriasDetalhesRes(
    val esquadrias: List<EsquadriaRes>,
    val esquadriasTipoMaterial: List<EsquadriaTipoMaterialRes>
)
