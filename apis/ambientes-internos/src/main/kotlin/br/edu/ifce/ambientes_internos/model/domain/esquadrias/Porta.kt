package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

class Porta(
    geometria: Geometria,
    quantidade: Int = 1,
    componentes: MutableList<ComponenteEsquadria> = mutableListOf(),
    informacaoAdicional: String = "",
    id: Long? = null
): Esquadria(id, geometria, componentes, quantidade, informacaoAdicional) {

    fun calcularVaoUtil(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}
