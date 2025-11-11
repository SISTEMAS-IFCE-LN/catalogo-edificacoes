package br.edu.ifce.ambientes_internos.model.domain.ambientes

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaTipoMaterialRes
import java.math.BigDecimal

abstract class Ambiente(
    var id: Long?,
    var nome: String,
    var localizacao: String,
    var tipo: TipoAmbiente,
    var capacidade: Int,
    val geometrias: MutableSet<Geometria>,
    val pesDireitos: MutableSet<BigDecimal>,
    val esquadrias: MutableSet<Esquadria>,
    var informacaoAdicional: String,
    var status: StatusAmbiente
) {

    fun calcularAreaAmbienteM2(): BigDecimal {
        return geometrias
            .map { it.calcularAreaTotalM2() }
            .fold(BigDecimal.ZERO) { acc, area -> acc + area }
    }

    fun calcularAreaEsquadriasPorTipoMaterial(): Map<Pair<TipoEsquadria, MaterialEsquadria>, BigDecimal> {
        return esquadrias
            .groupBy { Pair(it.tipo, it.material) }
            .mapValues { entry ->
                entry.value
                    .map { it.geometria.calcularAreaTotalM2() }
                    .fold(BigDecimal.ZERO) { acc, area -> acc + area }
            }
    }

}