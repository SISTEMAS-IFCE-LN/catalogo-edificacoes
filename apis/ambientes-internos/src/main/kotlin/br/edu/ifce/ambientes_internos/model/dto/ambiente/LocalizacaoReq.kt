package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade

data class LocalizacaoReq(
    val bloco: Bloco,
    val unidade: Unidade,
    val andar: Int = 0
)
