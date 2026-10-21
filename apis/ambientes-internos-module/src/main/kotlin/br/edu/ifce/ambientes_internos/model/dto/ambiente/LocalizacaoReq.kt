package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.common.config.MsgsSpringValidation.MSG_MAX_CARACTERES
import br.edu.ifce.common.config.MsgsSpringValidation.MSG_POSITIVO
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

/**
 * Referência a uma entidade de localização.
 *
 * - `id` presente  -> usa a entidade existente;
 * - `nome` presente -> find-or-create sob o pai já resolvido;
 * - exatamente um dos dois é obrigatório.
 *
 * O formato aninhado (`campus`/`unidade`/`bloco`) espelha a hierarquia e evita seis
 * campos nulos no payload.
 */
data class ReferenciaReq(
    @field:Positive(message = MSG_POSITIVO)
    val id: Long? = null,

    @field:Size(max = 50, message = MSG_MAX_CARACTERES)
    val nome: String? = null
) {
    @get:AssertTrue(message = "Informe exatamente um: id ou nome.")
    val valida: Boolean
        get() = (id != null) xor !nome.isNullOrBlank()
}

data class LocalizacaoReq(
    @field:Valid
    val campus: ReferenciaReq,

    @field:Valid
    val unidade: ReferenciaReq,

    @field:Valid
    val bloco: ReferenciaReq,

    @field:Min(value = 0, message = "O andar deve ser maior ou igual a 0.")
    val andar: Int = 0
)
