package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

class Janela(
    geometria: Geometria,
    tipo: TipoEsquadria,
    var alturaPeitoril: BigDecimal,
    informacaoAdicional: String = "",
    id: Long? = null
): Esquadria(id, geometria, tipo, informacaoAdicional) {

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
