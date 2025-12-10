package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class Porta(
    geometria: Geometria,
    material: MaterialEsquadria,
    informacaoAdicional: String = ""
): Esquadria(
    id = null,
    TipoEsquadria.PORTA,
    geometria,
    material,
    informacaoAdicional) {
}
