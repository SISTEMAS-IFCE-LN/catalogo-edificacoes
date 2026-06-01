package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class AmbienteReq(
    val tipo: TipoAmbiente,
    @field:NotBlank(message = "O nome do ambiente é obrigatório.")
    @field:Size(max = 50, message = "O nome do ambiente deve conter no máximo 50 caracteres.")
    val nome: String,
    @field:Valid
    val localizacao: LocalizacaoReq,
    @field:Positive(message = "A capacidade do ambiente deve ser positiva.")
    val capacidade: Int,
    @field:NotEmpty("O ambiente deve conter pelo menos uma geometria.")
    @field:Valid
    val geometrias: Set<GeometriaAmbienteReq>,
    @field:NotEmpty(message = "O ambiente deve conter pelo menos um pé direito.")
    val pesDireitos: Set<
        @Positive(message = "O pé direito deve ser positivo")
        @Digits(integer = 7, fraction = 2, message = "O pé direito deve ter no máximo 7 dígitos inteiros e 2 casas decimais.")
        BigDecimal
    >,
    @field:NotEmpty(message = "O ambiente deve conter pelo menos uma esquadria.")
    @field:Valid
    val esquadrias: Set<EsquadriaReq>,
    @field:Size(max = 255, message = "A informação adicional deve conter no máximo 255 caracteres.")
    val informacaoAdicional: String = ""
)
