package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.dto.ambiente.CampusRes

interface ICampusUseCases {
    fun listar(): List<CampusRes>
    fun obterPorId(id: Long): Campus
    fun resolverOuCriar(nomeDigitado: String): Campus
}
