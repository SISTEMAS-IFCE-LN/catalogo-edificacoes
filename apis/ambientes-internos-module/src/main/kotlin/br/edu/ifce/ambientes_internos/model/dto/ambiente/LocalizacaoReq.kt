package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.common.config.MsgsSpringValidation.MSG_MAX_CARACTERES
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LocalizacaoReq(
    @field:NotBlank(message = "O nome do bloco é obrigatório.")
    @field:Size(max = 50, message = MSG_MAX_CARACTERES)
    val bloco: String,
    val unidade: Unidade,
    @field:Min(value = 0, message = "O andar deve ser maior ou igual a 0.")
    val andar: Int = 0
)
