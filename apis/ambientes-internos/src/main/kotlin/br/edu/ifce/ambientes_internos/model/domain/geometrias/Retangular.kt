package br.edu.ifce.ambientes_internos.model.domain.geometrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.enums.TipoGeometria
import java.math.BigDecimal

class Retangular(
    base: BigDecimal,
    altura: BigDecimal,
    repeticao: Int = 1
) : Geometria(id = null, TipoGeometria.RETANGULAR, base, altura, repeticao) {

    override fun calcularAreaM2(): BigDecimal {
        return base.multiply(altura)
    }

}