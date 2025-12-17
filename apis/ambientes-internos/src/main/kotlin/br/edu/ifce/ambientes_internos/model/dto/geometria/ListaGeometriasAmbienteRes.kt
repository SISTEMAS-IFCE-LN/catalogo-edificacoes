package br.edu.ifce.ambientes_internos.model.dto.geometria

import java.math.BigDecimal

data class ListaGeometriasAmbienteRes(
    val geometrias: List<GeometriaAmbienteRes>,
    val areaTotal: BigDecimal
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListaGeometriasAmbienteRes

        if (!geometrias.containsAll(other.geometrias)) return false
        if (areaTotal != other.areaTotal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = geometrias.hashCode()
        result = 31 * result + areaTotal.hashCode()
        return result
    }
}
