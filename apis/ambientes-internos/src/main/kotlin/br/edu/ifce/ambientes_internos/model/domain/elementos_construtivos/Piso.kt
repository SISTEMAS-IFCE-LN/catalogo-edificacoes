package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos

import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoPiso
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

class Piso(
    var tipo : TipoPiso,
    geometrias: MutableList<Geometria> = mutableListOf(),
    quantidade: Int = 1,
    informacaoAdicional: String = "",
    id: Long? = null
): ElementoConstrutivo(id, geometrias, quantidade, informacaoAdicional) {

    override fun calcularAreaTotalM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}
