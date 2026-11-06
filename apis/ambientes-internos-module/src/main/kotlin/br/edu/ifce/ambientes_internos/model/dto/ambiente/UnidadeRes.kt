package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade

data class UnidadeRes(val id: Long, val nome: String) {
    companion object {
        fun from(unidade: Unidade) = UnidadeRes(unidade.id!!, unidade.nome)
    }
}
