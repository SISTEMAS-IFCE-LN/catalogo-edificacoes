package br.edu.ifce.ambientes_internos.model.dto.geometria

import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.enums.TipoGeometria
import java.math.BigDecimal

data class GeometriaAmbienteRes(
    val id: Long,
    val tipo: TipoGeometria,
    val base: BigDecimal,
    val altura: BigDecimal,
    val repeticao: Int,
    val area: BigDecimal
) {
    companion object {
        fun from(geometria: Geometria): GeometriaAmbienteRes {
            return GeometriaAmbienteRes(
                id = geometria.id!!,
                tipo = geometria.tipo,
                base = geometria.base,
                altura = geometria.altura,
                repeticao = geometria.repeticao,
                area = geometria.calcularAreaTotalM2()
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeometriaAmbienteRes) return false

        if (tipo != other.tipo) return false
        if (base != other.base) return false
        if (altura != other.altura) return false
        if (repeticao != other.repeticao) return false
        if (area != other.area) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tipo.hashCode()
        result = 31 * result + base.hashCode()
        result = 31 * result + altura.hashCode()
        result = 31 * result + repeticao
        result = 31 * result + area.hashCode()
        return result
    }
}
