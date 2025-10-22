package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class Porta(
    geometria: Geometria,
    componentes: MutableList<ComponenteEsquadria> = mutableListOf(),
    informacaoAdicional: String = "",
    id: Long? = null
): Esquadria(id, geometria, componentes, informacaoAdicional) {
}
