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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Geometria

        if (repeticao != other.repeticao) return false
        if (base != other.base) return false
        if (altura != other.altura) return false

        return true
    }

    override fun hashCode(): Int {
        var result = repeticao
        result = 31 * result + base.hashCode()
        result = 31 * result + altura.hashCode()
        return result
    }

}