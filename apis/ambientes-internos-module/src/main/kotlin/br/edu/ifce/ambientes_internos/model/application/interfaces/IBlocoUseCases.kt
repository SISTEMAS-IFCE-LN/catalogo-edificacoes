package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.dto.ambiente.BlocoRes

interface IBlocoUseCases {
    fun resolverOuCriar(unidade: Unidade, nomeDigitado: String): Bloco
    fun obterPorIdEValidarUnidade(blocoId: Long, unidadeId: Long): Bloco
    fun listarPorUnidade(unidadeId: Long): List<BlocoRes>
}
