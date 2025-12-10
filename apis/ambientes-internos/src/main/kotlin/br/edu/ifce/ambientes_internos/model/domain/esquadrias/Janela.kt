package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.math.BigDecimal

@Entity
@DiscriminatorValue("JANELA")
class Janela(
    geometria: Geometria,
    material: MaterialEsquadria,

    @Column(nullable = false, columnDefinition = "DECIMAL(9, 2) DEFAULT 0.00")
    var alturaPeitoril: BigDecimal,

    informacaoAdicional: String = ""
): Esquadria(
    id = null,
    TipoEsquadria.JANELA,
    geometria,
    material,
    informacaoAdicional) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Janela

        return alturaPeitoril == other.alturaPeitoril
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + alturaPeitoril.hashCode()
        return result
    }

}
