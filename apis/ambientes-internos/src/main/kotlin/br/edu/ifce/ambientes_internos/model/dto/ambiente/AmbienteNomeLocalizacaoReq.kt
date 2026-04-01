package br.edu.ifce.ambientes_internos.model.dto.ambiente

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AmbienteNomeLocalizacaoReq(
    @field:NotBlank(message = "O nome do ambiente é obrigatório.")
    @field:Size(max = 50, message = "O nome do ambiente deve conter no máximo 50 caracteres.")
    val nome: String,
    @field:Valid
    val localizacao: LocalizacaoReq
)
