package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos

import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.Revestimento
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoTeto
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

class Teto(
    id: Long? = null,
    var tipo : TipoTeto,
    var revestimento: Revestimento,
    geometrias: MutableList<Geometria> = mutableListOf(),
    quantidade: Int = 1,
    informacaoAdicional: String = ""
): ElementoConstrutivo(id, geometrias, quantidade, informacaoAdicional) {

    override fun calcularAreaPorTipoRevestimentoM2(): Map<String, BigDecimal> {
        throw NotImplementedError("Metodo não implementado")
    }

}
