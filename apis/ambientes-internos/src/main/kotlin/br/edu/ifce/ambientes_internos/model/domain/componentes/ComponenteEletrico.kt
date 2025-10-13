package br.edu.ifce.ambientes_internos.model.domain.componentes

import java.math.BigDecimal

abstract class ComponenteEletrico(
    id: Long?,
    quantidade: Int,
    informacaoAdicional: String
): Componente(id, quantidade, informacaoAdicional) {
    abstract fun calcularPotenciaWatts(): BigDecimal
}