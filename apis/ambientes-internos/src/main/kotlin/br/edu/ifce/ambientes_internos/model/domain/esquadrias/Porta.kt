package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class Porta(
    geometria: Geometria,
    materialEsquadria: MaterialEsquadria,
    informacaoAdicional: String = "",
    id: Long? = null
): Esquadria(id, geometria, materialEsquadria, informacaoAdicional) {
}
