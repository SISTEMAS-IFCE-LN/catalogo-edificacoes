package br.edu.ifce.ambientes_internos.model.domain.componentes

import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.FixacaoLuminaria
import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.MaterialLuminaria
import java.math.BigDecimal
import java.math.RoundingMode

class Luminaria(
    var material: MaterialLuminaria,
    var fixacao: FixacaoLuminaria,
    var lampada: Lampada,
    var quantidadeLampadas: Int = 1,
    var aletada: Boolean = false,
    quantidade: Int = 1,
    informacaoAdicional: String = "",
    id: Long? = null
): ComponenteEletrico(id, quantidade, informacaoAdicional) {

    fun calcularPotenciaLuminariaWatts(): BigDecimal {
        return lampada.potenciaWatts.multiply(BigDecimal(quantidadeLampadas))
            .setScale(2, RoundingMode.HALF_UP)
    }

    override fun calcularPotenciaWatts(): BigDecimal {
        return calcularPotenciaLuminariaWatts().multiply(BigDecimal(quantidade))
            .setScale(2, RoundingMode.HALF_UP)
    }

}
