package br.edu.ifce.ambientes_internos.model.domain.factory

import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Triangular
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.enums.TipoGeometria
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaReq
import java.math.BigDecimal

object GeometriaFactory {

    private val GEOMETRIAS_MAP: Map<TipoGeometria, (BigDecimal, BigDecimal, Int) -> Geometria> = mapOf(
        TipoGeometria.RETANGULAR to ::Retangular,
        TipoGeometria.TRIANGULAR to ::Triangular
    )

    fun criar(geometriaAmbienteReq: GeometriaAmbienteReq): Geometria {
        val construtor = GEOMETRIAS_MAP[geometriaAmbienteReq.tipo]
            ?: throw IllegalArgumentException("O tipo de geometria é inválido.")
        return construtor(geometriaAmbienteReq.base, geometriaAmbienteReq.altura, geometriaAmbienteReq.repeticao)
    }

    fun criar(geometriaEsquadriaReq: GeometriaEsquadriaReq): Geometria {
        val construtor = GEOMETRIAS_MAP[TipoGeometria.RETANGULAR]
            ?: throw IllegalArgumentException("O tipo de geometria é inválido.")
        return construtor(geometriaEsquadriaReq.base, geometriaEsquadriaReq.altura, geometriaEsquadriaReq.repeticao)
    }

    fun clonar(geometria: Geometria): Geometria {
        val construtor = GEOMETRIAS_MAP[geometria.tipo]
            ?: throw IllegalArgumentException("O tipo de geometria é inválido.")
        return construtor(geometria.base, geometria.altura, geometria.repeticao)
    }

}