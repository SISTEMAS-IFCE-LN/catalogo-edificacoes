package br.edu.ifce.ambientes_internos.model.domain.ambientes

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal
import kotlin.reflect.KClass

abstract class Ambiente(
    var id: Long?,
    var nome: String,
    var localizacao: String,
    var tipo: TipoAmbiente,
    var capacidade: Int,
    val geometrias: MutableSet<Geometria>,
    val pesDireitos: MutableSet<BigDecimal>,
    val esquadrias: MutableSet<Esquadria>,
    var informacaoAdicional: String,
    var status: StatusAmbiente
) {

    fun calcularAreaAmbientePorGeometriaM2(): Map<Geometria, BigDecimal> {
        return geometrias.associateWith { it.calcularAreaTotalM2() }
    }

    fun calcularAreaAmbienteM2(): BigDecimal {
        return geometrias
            .map { it.calcularAreaTotalM2() }
            .fold(BigDecimal.ZERO) { acc, area -> acc + area }
    }

    fun calcularAreaEsquadriasM2(classeEsquadria: KClass<out Esquadria>): BigDecimal {
        return esquadrias
            .filter { classeEsquadria.isInstance(it) }
            .map { it.geometria.calcularAreaTotalM2() }
            .fold(BigDecimal.ZERO) { acc, area -> acc + area }
    }

    fun calcularAreaEsquadriasPorTipoM2(classeEsquadria: KClass<out Esquadria>): Map<Esquadria, BigDecimal> {
        return esquadrias
            .filter { classeEsquadria.isInstance(it) }
            .associateWith { it.geometria.calcularAreaTotalM2() }
    }

}