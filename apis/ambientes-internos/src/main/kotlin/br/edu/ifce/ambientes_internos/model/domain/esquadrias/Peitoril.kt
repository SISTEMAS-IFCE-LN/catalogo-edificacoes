package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialPeitoril
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

class Peitoril(
    var geometria: Geometria,
    var altura: BigDecimal,
    var material: MaterialPeitoril,
    var informacaoAdicional: String = "",
    var id: Long? = null
) {
}
