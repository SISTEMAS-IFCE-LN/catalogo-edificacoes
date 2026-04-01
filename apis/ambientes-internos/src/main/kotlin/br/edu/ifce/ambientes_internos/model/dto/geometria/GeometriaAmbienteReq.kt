package br.edu.ifce.ambientes_internos.model.dto.geometria

import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.enums.TipoGeometria
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class GeometriaAmbienteReq(
    val tipo: TipoGeometria,
    @field:Positive(message = "A base deve ser positiva.")
    @field:Digits(integer = 7, fraction = 2, message = "A base deve ter no máximo 7 dígitos inteiros e 2 casas decimais.")
    val base: BigDecimal,
    @field:Positive(message = "A altura deve ser positiva.")
    @field:Digits(integer = 7, fraction = 2, message = "A altura deve ter no máximo 7 dígitos inteiros e 2 casas decimais.")
    val altura: BigDecimal,
    @field:Min(value = 1, message = "A repetição deve ser no mínimo 1.")
    val repeticao: Int = 1
)
