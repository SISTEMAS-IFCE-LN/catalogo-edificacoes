package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus

data class CampusRes(val id: Long, val nome: String) {
    companion object {
        fun from(campus: Campus) = CampusRes(campus.id!!, campus.nome)
    }
}
