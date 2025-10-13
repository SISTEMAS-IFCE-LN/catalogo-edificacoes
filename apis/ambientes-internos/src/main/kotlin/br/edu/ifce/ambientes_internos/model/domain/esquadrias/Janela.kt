package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class Janela(
    id: Long? = null,
    geometria: Geometria,
    componentes: MutableList<ComponenteEsquadria> = mutableListOf(),
    var peitoril: Peitoril,
    informacaoAdicional: String = ""
): Esquadria(id, geometria, componentes, informacaoAdicional) {

}
