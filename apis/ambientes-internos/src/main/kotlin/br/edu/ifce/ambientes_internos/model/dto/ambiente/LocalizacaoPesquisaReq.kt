package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade

data class LocalizacaoPesquisaReq(
    val bloco: String?,
    val unidade: String?,
    val andar: Int?
)
