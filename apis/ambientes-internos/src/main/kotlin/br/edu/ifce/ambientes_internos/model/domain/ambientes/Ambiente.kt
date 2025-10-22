package br.edu.ifce.ambientes_internos.model.domain.ambientes

import br.edu.ifce.ambientes_internos.model.domain.componentes.Componente
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Parede
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Piso
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Teto
import java.math.BigDecimal

abstract class Ambiente(
    var id: Long?,
    var nome: String,
    var localizacao: String,
    var capacidade: Int,
    var informacaoAdicional: String,
    val pisos: MutableList<Piso>,
    val paredes: MutableList<Parede>,
    val tetos: MutableList<Teto>,
    val componentes: MutableList<Componente>
) {

    fun calcularAreaParedesM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaPisosM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaPortasM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularAreaJanelasM2(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

    fun calcularCargaAmbiente(): BigDecimal {
        throw NotImplementedError("Metodo não implementado")
    }

}