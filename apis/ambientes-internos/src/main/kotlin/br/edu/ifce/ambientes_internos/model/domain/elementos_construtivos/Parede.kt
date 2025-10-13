package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos

import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.Revestimento
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoParede
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class Parede(
    id: Long? = null,
    var tipo: TipoParede,
    var revestimento: Revestimento,
    geometrias: MutableList<Geometria> = mutableListOf(),
    quantidade: Int = 1,
    informacaoAdicional: String = ""
): ElementoConstrutivo(id, geometrias, quantidade, informacaoAdicional) {
}