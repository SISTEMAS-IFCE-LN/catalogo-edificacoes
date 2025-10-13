package br.edu.ifce.ambientes_internos.model.domain.componentes

abstract class Componente(
    var id: Long?,
    var quantidade: Int,
    var informacaoAdicional: String
) {
}
