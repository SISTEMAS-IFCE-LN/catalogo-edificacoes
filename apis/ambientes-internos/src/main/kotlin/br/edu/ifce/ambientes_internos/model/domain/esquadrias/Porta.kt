package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

class Porta(
    id: Long? = null,
    geometria: Geometria,
    componentes: MutableList<ComponenteEsquadria> = mutableListOf(),
    informacaoAdicional: String = ""
): Esquadria(id, geometria, componentes, informacaoAdicional) {

    fun calcularVaoUtil(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}
