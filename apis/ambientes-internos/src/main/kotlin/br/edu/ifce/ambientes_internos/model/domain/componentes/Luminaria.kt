package br.edu.ifce.ambientes_internos.model.domain.componentes

import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.FixacaoLuminaria
import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.MaterialLuminaria
import java.math.BigDecimal

class Luminaria(
    id: Long? = null,
    var material: MaterialLuminaria,
    var fixacao: FixacaoLuminaria,
    var lampada: Lampada,
    var quantidadeLampadas: Int,
    var aletada: Boolean,
    quantidade: Int = 1,
    informacaoAdicional: String = ""
): ComponenteEletrico(id, quantidade, informacaoAdicional) {

    fun calcularPotenciaLuminariaWatts(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    override fun calcularPotenciaWatts(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}
