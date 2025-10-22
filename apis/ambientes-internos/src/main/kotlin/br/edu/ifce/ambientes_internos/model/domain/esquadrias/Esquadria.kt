package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal
import java.math.RoundingMode

abstract class Esquadria(
    var id: Long?,
    var geometria: Geometria,
    val componentes: MutableList<ComponenteEsquadria>,
    var informacaoAdicional: String
) {

    fun calcularAreaFolhasM2(): BigDecimal {
        return componentes.filterIsInstance<Folha>()
            .fold(BigDecimal.ZERO) { acc, folha -> acc.add(folha.geometria.calcularAreaTotalM2()) }
            .setScale(2, RoundingMode.HALF_UP)
    }

    fun calcularAreaBandeirolasM2(): BigDecimal {
        return componentes.filterIsInstance<Bandeirola>()
            .fold(BigDecimal.ZERO) { acc, bandeirola -> acc.add(bandeirola.geometria.calcularAreaTotalM2()) }
            .setScale(2, RoundingMode.HALF_UP)
    }

    fun calcularComprimentoGuarnicoesM(): BigDecimal {
        return componentes.filterIsInstance<Guarnicao>()
            .fold(BigDecimal.ZERO) { acc, guarnicao ->
                acc.add(
                    guarnicao.geometria.altura
                        .multiply(BigDecimal(guarnicao.geometria.repeticao))
                )
            }
            .setScale(2, RoundingMode.HALF_UP)
    }

    fun calcularAreaGuarnicoesM2(): BigDecimal {
        return componentes.filterIsInstance<Guarnicao>()
            .fold(BigDecimal.ZERO) { acc, guarnicao -> acc.add(guarnicao.geometria.calcularAreaTotalM2()) }
            .setScale(2, RoundingMode.HALF_UP)
    }

}