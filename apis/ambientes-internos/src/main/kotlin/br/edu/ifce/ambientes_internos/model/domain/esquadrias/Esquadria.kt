package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

abstract class Esquadria(
    var id: Long?,
    var geometria: Geometria,
    var tipo: TipoEsquadria,
    var informacaoAdicional: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Esquadria

        if (geometria != other.geometria) return false
        if (tipo != other.tipo) return false
        if (informacaoAdicional != other.informacaoAdicional) return false

        return true
    }

    override fun hashCode(): Int {
        var result = geometria.hashCode()
        result = 31 * result + tipo.hashCode()
        result = 31 * result + informacaoAdicional.hashCode()
        return result
    }

}