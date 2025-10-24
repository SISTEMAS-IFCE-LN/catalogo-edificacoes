package br.edu.ifce.ambientes_internos.model.domain.componentes

import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.TipoEquipamento
import java.math.BigDecimal
import java.math.RoundingMode

class Equipamento(
    quantidade: Int,
    var tipo: TipoEquipamento,
    var potenciaWatts: BigDecimal,
    informacaoAdicional: String = "",
    id: Long? = null
): ComponenteEletrico(id, quantidade, informacaoAdicional) {

    override fun calcularPotenciaWatts(): BigDecimal {
        return potenciaWatts.multiply(BigDecimal(quantidade))
            .setScale(2, RoundingMode.HALF_UP)
    }

}