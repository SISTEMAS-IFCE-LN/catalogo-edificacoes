package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class Janela(
    geometria: Geometria,
    componentes: MutableList<ComponenteEsquadria> = mutableListOf(),
    var peitoril: Peitoril? = null,
    informacaoAdicional: String = "",
    id: Long? = null
): Esquadria(id, geometria, componentes, informacaoAdicional) {
}
