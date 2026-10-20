package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.dto.ambiente.UnidadeRes

interface IUnidadeUseCases {
    fun listarPorCampus(campusId: Long): List<UnidadeRes>
    fun obterPorIdEValidarCampus(unidadeId: Long, campusId: Long): Unidade
    fun resolverOuCriar(campus: Campus, nomeDigitado: String): Unidade
}
