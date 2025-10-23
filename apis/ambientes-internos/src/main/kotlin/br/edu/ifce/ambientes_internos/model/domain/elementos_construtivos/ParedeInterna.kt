package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos

import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.Revestimento
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoParede
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

// Representa uma divisão dentro do mesmo ambiente
class ParedeInterna(
    tipo: TipoParede,
    revestimento: Revestimento,
    geometrias: MutableList<Geometria> = mutableListOf(),
    esquadrias: MutableList<Esquadria> = mutableListOf(),
    quantidade: Int = 1,
    informacaoAdicional: String = "",
    id: Long? = null
): Parede(tipo, revestimento, geometrias, esquadrias, quantidade, informacaoAdicional, id) {
}