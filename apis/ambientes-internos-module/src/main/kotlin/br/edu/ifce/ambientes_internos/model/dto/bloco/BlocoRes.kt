package br.edu.ifce.ambientes_internos.model.dto.bloco

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco

data class BlocoRes(val id: Long, val nome: String) {
    companion object {
        fun from(bloco: Bloco) = BlocoRes(bloco.id!!, bloco.nome)
    }
}
