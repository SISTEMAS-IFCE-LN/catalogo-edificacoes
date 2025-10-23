package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos

import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.Revestimento
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoParede
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal
import java.math.RoundingMode

abstract class Parede(
    var tipo: TipoParede,
    var revestimento: Revestimento,
    geometrias: MutableList<Geometria>,
    val esquadrias: MutableList<Esquadria>,
    quantidade: Int,
    informacaoAdicional: String,
    id: Long?
) : ElementoConstrutivo(id, geometrias, quantidade, informacaoAdicional) {

    override fun calcularAreaTotalM2(): BigDecimal {
        val areaBruta = calcularAreaBrutaGeometriasM2()
        val areaEsquadrias = esquadrias.fold(BigDecimal.ZERO) { acc, esquadria ->
            acc.add(esquadria.geometria.calcularAreaTotalM2())
        }
        return areaBruta.subtract(areaEsquadrias)
            .multiply(BigDecimal(quantidade))
            .setScale(2, RoundingMode.HALF_UP)
    }

}