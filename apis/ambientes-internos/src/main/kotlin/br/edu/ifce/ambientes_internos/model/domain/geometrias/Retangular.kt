package br.edu.ifce.ambientes_internos.model.domain.geometrias

import java.math.BigDecimal

class Retangular(
    base: BigDecimal,
    altura: BigDecimal,
    repeticao: Int = 1,
    id: Long? = null
) : Geometria(id, base, altura, repeticao) {

    override fun calcularAreaM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}