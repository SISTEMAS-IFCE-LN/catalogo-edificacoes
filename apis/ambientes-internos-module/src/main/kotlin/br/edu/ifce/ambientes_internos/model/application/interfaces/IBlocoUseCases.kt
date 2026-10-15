package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade

interface IBlocoUseCases {
    fun resolverOuCriar(unidade: Unidade, nomeDigitado: String): Bloco
}