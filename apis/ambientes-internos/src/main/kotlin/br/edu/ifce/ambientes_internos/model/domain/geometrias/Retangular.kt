package br.edu.ifce.ambientes_internos.model.domain.geometrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.enums.TipoGeometria
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.math.BigDecimal

@Entity
@DiscriminatorValue("RETANGULAR")
class Retangular(
    base: BigDecimal,
    altura: BigDecimal,
    repeticao: Int = 1
) : Geometria(id = null, TipoGeometria.RETANGULAR, base, altura, repeticao) {

    override fun calcularAreaM2(): BigDecimal {
        return base.multiply(altura)
    }

}