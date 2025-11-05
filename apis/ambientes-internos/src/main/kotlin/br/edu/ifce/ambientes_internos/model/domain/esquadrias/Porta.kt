package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class Porta(
    geometria: Geometria,
    tipo: TipoEsquadria,
    informacaoAdicional: String = "",
    id: Long? = null
): Esquadria(id, geometria, tipo, informacaoAdicional) {
}
