package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class Janela(
    geometria: Geometria,
    var peitoril: Peitoril,
    quantidade: Int = 1,
    componentes: MutableList<ComponenteEsquadria> = mutableListOf(),
    informacaoAdicional: String = "",
    id: Long? = null
): Esquadria(id, geometria, componentes, quantidade, informacaoAdicional) {
}
