package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.Abertura
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class Folha(
    id: Long? = null,
    geometria: Geometria,
    material: MaterialEsquadria,
    var abertura: Abertura,
    informacaoAdicional: String = ""
): ComponenteEsquadria(id, geometria, material, informacaoAdicional) {
}