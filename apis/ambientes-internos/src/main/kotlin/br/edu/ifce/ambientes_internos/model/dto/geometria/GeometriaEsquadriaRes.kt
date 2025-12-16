package br.edu.ifce.ambientes_internos.model.dto.geometria

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

data class GeometriaEsquadriaRes(
    val id: Long,
    val base: BigDecimal,
    val altura: BigDecimal,
    val repeticao: Int,
    val area: BigDecimal
) {
    companion object {
        fun from(geometria: Geometria): GeometriaEsquadriaRes {
            return GeometriaEsquadriaRes(
                id = geometria.id!!,
                base = geometria.base,
                altura = geometria.altura,
                repeticao = geometria.repeticao,
                area = geometria.calcularAreaTotalM2()
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeometriaEsquadriaRes) return false

        if (base != other.base) return false
        if (altura != other.altura) return false
        if (repeticao != other.repeticao) return false
        if (area != other.area) return false

        return true
    }

    override fun hashCode(): Int {
        var result = base.hashCode()
        result = 31 * result + altura.hashCode()
        result = 31 * result + repeticao
        result = 31 * result + area.hashCode()
        return result
    }
}
