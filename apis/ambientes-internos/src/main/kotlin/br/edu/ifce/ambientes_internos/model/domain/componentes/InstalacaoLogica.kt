package br.edu.ifce.ambientes_internos.model.domain.componentes

import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.TipoInstalacaoLogica

class InstalacaoLogica(
    id: Long? = null,
    var tipo: TipoInstalacaoLogica,
    quantidade: Int = 1,
    informacaoAdicional: String = ""
): Componente(id, quantidade, informacaoAdicional) {
}