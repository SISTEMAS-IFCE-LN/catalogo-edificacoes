package br.edu.ifce.ambientes_internos.model.domain.componentes

import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.FormatoLampada
import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.TipoLampada
import java.math.BigDecimal

class Lampada(
    var tipo: TipoLampada,
    var formato: FormatoLampada,
    var potenciaWatts: BigDecimal,
    var id: Long? = null,
) {
}
