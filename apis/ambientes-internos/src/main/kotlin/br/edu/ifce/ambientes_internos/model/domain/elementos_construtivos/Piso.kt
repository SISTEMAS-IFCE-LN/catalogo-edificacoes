package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos

import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoPiso
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal
import java.math.RoundingMode

class Piso(
    var tipo : TipoPiso,
    geometrias: MutableList<Geometria> = mutableListOf(),
    quantidade: Int = 1,
    informacaoAdicional: String = "",
    id: Long? = null
): ElementoConstrutivo(id, geometrias, quantidade, informacaoAdicional) {

    override fun calcularAreaTotalM2(): BigDecimal {
        return calcularAreaBrutaGeometriasM2()
            .multiply(BigDecimal(quantidade))
            .setScale(2, RoundingMode.HALF_UP)
    }

}
