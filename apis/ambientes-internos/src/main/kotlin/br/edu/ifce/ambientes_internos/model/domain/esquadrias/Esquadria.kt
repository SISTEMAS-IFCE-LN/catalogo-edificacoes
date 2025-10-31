package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal
import java.math.RoundingMode

abstract class Esquadria(
    var id: Long?,
    var geometria: Geometria,
    var materialEsquadria: MaterialEsquadria,
    var informacaoAdicional: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Esquadria

        if (geometria != other.geometria) return false
        if (materialEsquadria != other.materialEsquadria) return false
        if (informacaoAdicional != other.informacaoAdicional) return false

        return true
    }

    override fun hashCode(): Int {
        var result = geometria.hashCode()
        result = 31 * result + materialEsquadria.hashCode()
        result = 31 * result + informacaoAdicional.hashCode()
        return result
    }

}