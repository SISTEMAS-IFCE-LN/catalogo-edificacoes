package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos

import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums.TipoPiso
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

class Piso(
    id: Long? = null,
    var tipo : TipoPiso,
    geometrias: MutableList<Geometria> = mutableListOf(),
    quantidade: Int = 1,
    informacaoAdicional: String = ""
): ElementoConstrutivo(id, geometrias, quantidade, informacaoAdicional) {

    override fun calcularAreaPorTipoRevestimentoM2(): Map<String, BigDecimal> {
        throw NotImplementedError("Metodo não implementado")
    }

}
