package br.edu.ifce.ambientes_internos.model.domain.ambientes

import br.edu.ifce.ambientes_internos.model.domain.componentes.Componente
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Parede
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Piso
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Teto

class SalaAula(
    nome: String,
    localizacao: String,
    capacidade: Int,
    pisos: MutableList<Piso> = mutableListOf(),
    paredes: MutableList<Parede> = mutableListOf(),
    tetos: MutableList<Teto> = mutableListOf(),
    componentes: MutableList<Componente> = mutableListOf(),
    publicado: Boolean = false,
    informacaoAdicional: String = "",
    id: Long? = null
): Ambiente(id, nome, localizacao, capacidade, informacaoAdicional, publicado, pisos, paredes, tetos, componentes) {
}