package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

abstract class ComponenteEsquadria(
    var id: Long?,
    var geometria: Geometria,
    var material: MaterialEsquadria,
    var informacaoAdicional: String
) {
}