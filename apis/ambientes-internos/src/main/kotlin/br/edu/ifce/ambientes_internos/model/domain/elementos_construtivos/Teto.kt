package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos

import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.Revestimento
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoTeto
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

class Teto(
    var tipo : TipoTeto,
    var revestimento: Revestimento,
    geometrias: MutableList<Geometria> = mutableListOf(),
    quantidade: Int = 1,
    informacaoAdicional: String = "",
    id: Long? = null
): ElementoConstrutivo(id, geometrias, quantidade, informacaoAdicional) {

    override fun calcularAreaTotalM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}
