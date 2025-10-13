package br.edu.ifce.ambientes_internos.model.domain.geometrias

import java.math.BigDecimal

abstract class Geometria(
    var id: Long?,
    var base: BigDecimal,
    var altura: BigDecimal,
    var repeticao: Int
) {

    abstract fun calcularAreaM2(): BigDecimal

    fun calcularAreaTotalM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}