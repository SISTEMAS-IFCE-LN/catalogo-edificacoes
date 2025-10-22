package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

class Guarnicao(
    geometria: Geometria,
    material: MaterialEsquadria,
    var espessura: BigDecimal,
    informacaoAdicional: String = "",
    id: Long? = null
): ComponenteEsquadria(id, geometria, material, informacaoAdicional) {
}