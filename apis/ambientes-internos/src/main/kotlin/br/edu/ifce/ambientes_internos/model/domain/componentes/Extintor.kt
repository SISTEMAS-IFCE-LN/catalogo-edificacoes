package br.edu.ifce.ambientes_internos.model.domain.componentes

import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.ClasseIncendio
import br.edu.ifce.ambientes_internos.model.domain.componentes.enums.TipoExtintor

class Extintor(
    id: Long? = null,
    quantidade: Int = 1,
    var capacidade: String,
    var unidadeCapacidade: String,
    var tipo: TipoExtintor,
    val classesIncendio: MutableList<ClasseIncendio>,
    informacaoAdicional: String = ""
): Componente(id, quantidade, informacaoAdicional) {
}