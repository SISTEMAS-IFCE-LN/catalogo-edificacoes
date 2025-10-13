package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal

abstract class Esquadria(
    var id: Long?,
    var geometria: Geometria,
    val componentes: MutableList<ComponenteEsquadria>,
    var informacaoAdicional: String
) {

    fun obterQuantidadeFolhas(): Int {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaFolhasM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun obterQuantidadeBandeirolas(): Int {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaBandeirolasM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun obterQuantidadeGuarnicoes(): Int {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaGuarnicoesM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularComprimentoGuarnicoes(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}