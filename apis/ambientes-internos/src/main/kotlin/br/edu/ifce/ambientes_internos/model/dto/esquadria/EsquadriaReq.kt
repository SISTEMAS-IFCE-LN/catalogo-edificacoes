package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaReq
import jakarta.validation.Valid
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class EsquadriaReq(
    val tipo: TipoEsquadria,
    @field:Valid
    val geometria: GeometriaEsquadriaReq,
    val material: MaterialEsquadria,
    @field:Min(value = 0, message = "A altura do peitoril deve ser maior ou igual a 0.")
    @field:Digits(integer = 7, fraction = 2, message = "A altura do peitoril deve ter no máximo 7 dígitos inteiros e 2 casas decimais.")
    val alturaPeitoril: BigDecimal = BigDecimal.ZERO,
    @field:Size(max = 255, message = "A informação adicional deve conter no máximo 255 caracteres.")
    val informacaoAdicional: String = ""
)