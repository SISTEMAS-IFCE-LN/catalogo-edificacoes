package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.dto.bloco.BlocoRes

interface IBlocoUseCases {
    fun resolverOuCriar(unidade: Unidade, nomeDigitado: String): Bloco
    fun listarPorUnidade(unidade: String): List<BlocoRes>
}