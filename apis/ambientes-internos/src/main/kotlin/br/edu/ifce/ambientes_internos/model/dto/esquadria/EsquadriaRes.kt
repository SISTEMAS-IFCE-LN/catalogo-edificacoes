package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaEsquadriaRes
import java.math.BigDecimal

data class EsquadriaRes(
    val id: Long,
    val tipo: TipoEsquadria,
    val geometria: GeometriaEsquadriaRes,
    val alturaPeitoril: BigDecimal,
    val area: BigDecimal,
    val material: MaterialEsquadria,
    val informacaoAdicional: String
) {
    companion object {
        fun from(esquadria: Esquadria): EsquadriaRes {
            return EsquadriaRes(
                id = esquadria.id!!,
                tipo = esquadria.tipo,
                geometria = GeometriaEsquadriaRes.from(esquadria.geometria),
                alturaPeitoril = if (esquadria is Janela) esquadria.alturaPeitoril else BigDecimal.ZERO,
                area = esquadria.geometria.calcularAreaTotalM2(),
                material = esquadria.material,
                informacaoAdicional = esquadria.informacaoAdicional
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EsquadriaRes) return false

        if (tipo != other.tipo) return false
        if (geometria != other.geometria) return false
        if (alturaPeitoril != other.alturaPeitoril) return false
        if (area != other.area) return false
        if (material != other.material) return false
        if (informacaoAdicional != other.informacaoAdicional) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tipo.hashCode()
        result = 31 * result + geometria.hashCode()
        result = 31 * result + alturaPeitoril.hashCode()
        result = 31 * result + area.hashCode()
        result = 31 * result + material.hashCode()
        result = 31 * result + informacaoAdicional.hashCode()
        return result
    }
}