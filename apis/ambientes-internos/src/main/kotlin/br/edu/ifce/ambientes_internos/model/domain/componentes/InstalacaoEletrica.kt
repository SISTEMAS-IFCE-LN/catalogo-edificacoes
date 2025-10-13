package br.edu.ifce.ambientes_internos.model.domain.componentes

import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.TipoInstalacaoEletrica
import java.math.BigDecimal

class InstalacaoEletrica(
    id: Long? = null,
    var tipo: TipoInstalacaoEletrica,
    var capacidade: BigDecimal = BigDecimal.ZERO,
    var unidadeCapacidade: String = "",
    quantidade: Int = 1,
    informacaoAdicional: String = ""
): Componente(id, quantidade, informacaoAdicional) {
}