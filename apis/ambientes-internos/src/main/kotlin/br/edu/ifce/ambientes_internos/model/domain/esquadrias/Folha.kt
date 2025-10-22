package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.Abertura
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class Folha(
    geometria: Geometria,
    material: MaterialEsquadria,
    var abertura: Abertura,
    informacaoAdicional: String = "",
    id: Long? = null
): ComponenteEsquadria(id, geometria, material, informacaoAdicional) {
}