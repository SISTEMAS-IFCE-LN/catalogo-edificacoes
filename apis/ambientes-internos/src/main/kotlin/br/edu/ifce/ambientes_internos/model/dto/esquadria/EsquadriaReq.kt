package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaReq

data class EsquadriaReq(
    val tipo: TipoEsquadria,
    val geometria: GeometriaReq,
    val material: MaterialEsquadria,
    val informacaoAdicional: String = ""
)