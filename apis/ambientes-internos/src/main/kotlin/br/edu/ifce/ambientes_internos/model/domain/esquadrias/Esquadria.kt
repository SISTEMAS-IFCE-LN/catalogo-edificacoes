package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

abstract class Esquadria(
    var id: Long?,
    var tipo: TipoEsquadria,
    var geometria: Geometria,
    var material: MaterialEsquadria,
    var informacaoAdicional: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Esquadria

        if (tipo != other.tipo) return false
        if (geometria != other.geometria) return false
        if (material != other.material) return false
        if (informacaoAdicional != other.informacaoAdicional) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tipo.hashCode()
        result = 31 * result + geometria.hashCode()
        result = 31 * result + material.hashCode()
        result = 31 * result + informacaoAdicional.hashCode()
        return result
    }

}