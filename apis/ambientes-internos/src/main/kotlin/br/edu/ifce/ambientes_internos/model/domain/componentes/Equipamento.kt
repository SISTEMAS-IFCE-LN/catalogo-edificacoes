package br.edu.ifce.ambientes_internos.model.domain.componentes

import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.TipoEquipamento
import java.math.BigDecimal

class Equipamento(
    id: Long? = null,
    quantidade: Int,
    var tipo: TipoEquipamento,
    var potenciaWatts: BigDecimal,
    informacaoAdicional: String = ""
): ComponenteEletrico(id, quantidade, informacaoAdicional) {

    override fun calcularPotenciaWatts(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}