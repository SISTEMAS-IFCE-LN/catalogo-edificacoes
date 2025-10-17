package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

abstract class Esquadria(
    var id: Long?,
    var geometria: Geometria,
    val componentes: MutableList<ComponenteEsquadria>,
    var quantidade: Int,
    var informacaoAdicional: String
) {

    fun obterQuantidadeFolhasPorEsquadria(): Int {
        throw NotImplementedError("Metodo não implementado")
    }

    fun obterQuantidadeTotalFolhas(): Int {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaFolhasPorEsquadriaM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaTotalFolhasM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun obterQuantidadeBandeirolasPorEsquadria(): Int {
        throw NotImplementedError("Metodo não implementado")
    }

    fun obterQuantidadeTotalBandeirolas(): Int {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaBandeirolasPorEsquadriaM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaTotalBandeirolasM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularComprimentoGuarnicoesPorEsquadriaM(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularComprimentoTotalGuarnicoesM(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaGuarnicoesPorEsquadriaM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaTotalGuarnicoesM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}