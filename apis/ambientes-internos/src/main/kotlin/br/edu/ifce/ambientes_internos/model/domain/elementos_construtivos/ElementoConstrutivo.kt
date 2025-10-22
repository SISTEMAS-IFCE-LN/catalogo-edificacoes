package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

abstract class ElementoConstrutivo(
    var id: Long?,
    val geometrias: MutableList<Geometria>,
    var quantidade: Int,
    var informacaoAdicional: String
) {

    abstract fun calcularAreaTotalM2(): BigDecimal

}