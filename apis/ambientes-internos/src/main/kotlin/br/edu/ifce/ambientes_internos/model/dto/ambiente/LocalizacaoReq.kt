package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import jakarta.validation.constraints.Min

data class LocalizacaoReq(
    val bloco: Bloco,
    val unidade: Unidade,
    @field:Min(value = 0, message = "O andar deve ser maior ou igual a 0.")
    val andar: Int = 0
)
