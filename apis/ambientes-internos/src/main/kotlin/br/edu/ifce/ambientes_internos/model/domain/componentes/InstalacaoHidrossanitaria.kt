package br.edu.ifce.ambientes_internos.model.domain.componentes

import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.TipoInstalacaoHidrossanitaria

class InstalacaoHidrossanitaria(
    id: Long? = null,
    var tipo: TipoInstalacaoHidrossanitaria,
    var pontoHidraulico: Int,
    var pontoSanitario: Int,
    quantidade: Int = 1,
    informacaoAdicional: String = ""
): Componente(id, quantidade, informacaoAdicional) {
}