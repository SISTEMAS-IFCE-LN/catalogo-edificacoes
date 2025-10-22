package br.edu.ifce.ambientes_internos.model.domain.geometrias

import java.math.BigDecimal
import java.math.RoundingMode

abstract class Geometria(
    var id: Long?,
    var base: BigDecimal,
    var altura: BigDecimal,
    var repeticao: Int
) {

    abstract fun calcularAreaM2(): BigDecimal

    fun calcularAreaTotalM2(): BigDecimal {
        return calcularAreaM2()
            .multiply(BigDecimal(repeticao))
            .setScale(2, RoundingMode.HALF_UP)
    }

}