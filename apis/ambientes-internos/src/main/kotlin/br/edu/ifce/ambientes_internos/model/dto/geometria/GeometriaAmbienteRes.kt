package br.edu.ifce.ambientes_internos.model.dto.geometria

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.enums.TipoGeometria
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
}
