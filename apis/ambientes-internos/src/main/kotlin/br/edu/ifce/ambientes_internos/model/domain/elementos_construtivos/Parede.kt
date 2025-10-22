package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos

import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.Revestimento
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoParede
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

class Parede(
    var tipo: TipoParede,
    var revestimento: Revestimento,
    geometrias: MutableList<Geometria> = mutableListOf(),
    val esquadrias: MutableList<Esquadria> = mutableListOf(),
    quantidade: Int = 1,
    informacaoAdicional: String = "",
    id: Long? = null
): ElementoConstrutivo(id, geometrias, quantidade, informacaoAdicional) {

    override fun calcularAreaPorTipoRevestimentoM2(): Map<String, BigDecimal> {
        throw NotImplementedError("Metodo não implementado")
    }

}