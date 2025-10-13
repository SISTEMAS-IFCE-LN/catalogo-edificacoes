package br.edu.ifce.ambientes_internos.model.domain.geometrias

import java.math.BigDecimal

class Retangular(
    id: Long? = null,
    base: BigDecimal,
    altura: BigDecimal,
    repeticao: Int = 1
): Geometria(id, base, altura, repeticao) {

    override fun calcularAreaM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}