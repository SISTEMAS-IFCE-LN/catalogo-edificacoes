package br.edu.ifce.ambientes_internos.model.domain.ambientes

import br.edu.ifce.ambientes_internos.model.domain.componentes.Componente
import br.edu.ifce.ambientes_internos.model.domain.componentes.ComponenteEletrico
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Parede
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.ParedeExterna
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.ParedeInterna
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Piso
import br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.Teto
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import java.math.BigDecimal
import java.math.RoundingMode

abstract class Ambiente(
    var id: Long?,
    var nome: String,
    var localizacao: String,
    var capacidade: Int,
    var informacaoAdicional: String,
    var publicado: Boolean,
    val pisos: MutableList<Piso>,
    val paredes: MutableList<Parede>,
    val tetos: MutableList<Teto>,
    val componentes: MutableList<Componente>
) {

    fun calcularAreaParedesExternasM2(): BigDecimal {
        val total = paredes
            .filterIsInstance<ParedeExterna>()
            .fold(BigDecimal.ZERO) { acc, parede -> acc.add(parede.calcularAreaTotalM2()) }
        return total.setScale(2, RoundingMode.HALF_UP)
    }

    fun calcularAreaParedesInternasM2(): BigDecimal {
        val total = paredes
            .filterIsInstance<ParedeInterna>()
            .fold(BigDecimal.ZERO) { acc, parede -> acc.add(parede.calcularAreaTotalM2()) }
        return total.setScale(2, RoundingMode.HALF_UP)
    }

    fun calcularAreaPisosM2(): BigDecimal {
        val total = pisos.fold(BigDecimal.ZERO) { acc, piso -> acc.add(piso.calcularAreaTotalM2()) }
        return total.setScale(2, RoundingMode.HALF_UP)
    }

    fun calcularAreaPortasM2(): BigDecimal {
        val total = paredes
            .flatMap { it.esquadrias }
            .filterIsInstance<Porta>()
            .fold(BigDecimal.ZERO) { acc, porta -> acc.add(porta.geometria.calcularAreaTotalM2()) }
        return total.setScale(2, RoundingMode.HALF_UP)
    }

    fun calcularAreaJanelasM2(): BigDecimal {
        val total = paredes
            .flatMap { it.esquadrias }
            .filterIsInstance<Janela>()
            .fold(BigDecimal.ZERO) { acc, janela -> acc.add(janela.geometria.calcularAreaTotalM2()) }
        return total.setScale(2, RoundingMode.HALF_UP)
    }

    fun calcularCargaAmbienteWatts(): BigDecimal {
        val total = componentes
            .filterIsInstance<ComponenteEletrico>()
            .fold(BigDecimal.ZERO) { acc, comp -> acc.add(comp.calcularPotenciaWatts()) }
        return total.setScale(2, RoundingMode.HALF_UP)
    }

    fun calcularAreaPisosPorTipoRevestimentoM2(): Map<String, BigDecimal> {
        val map = mutableMapOf<String, BigDecimal>()
        pisos.forEach { piso ->
            val tipo = piso.tipo.nome
            val area = piso.calcularAreaTotalM2()
            map[tipo] = map.getOrDefault(tipo, BigDecimal.ZERO).add(area)
        }
        return map
    }

    fun calcularAreaTetosPorTipoRevestimentoM2(): Map<String, BigDecimal> {
        val map = mutableMapOf<String, BigDecimal>()
        tetos.forEach { teto ->
            val tipo = "${teto.tipo.nome} - ${teto.revestimento.nome}"
            val area = teto.calcularAreaTotalM2()
            map[tipo] = map.getOrDefault(tipo, BigDecimal.ZERO).add(area)
        }
        return map
    }

    fun calcularAreaParedesPorTipoRevestimentoM2(): Map<String, BigDecimal> {
        val map = mutableMapOf<String, BigDecimal>()
        paredes.forEach { parede ->
            val tipo = "${parede.tipo.nome} - ${parede.revestimento.nome}"
            val area = parede.calcularAreaTotalM2()
            map[tipo] = map.getOrDefault(tipo, BigDecimal.ZERO).add(area)
        }
        return map
    }

}