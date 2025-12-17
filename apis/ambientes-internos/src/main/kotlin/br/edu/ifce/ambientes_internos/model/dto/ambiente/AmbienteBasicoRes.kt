package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import java.math.BigDecimal

data class AmbienteBasicoRes(
    val id: Long,
    val nome: String,
    val localizacao: LocalizacaoRes,
    val capacidade: Int,
    val area: BigDecimal
) {
    companion object {
        fun from(ambiente: Ambiente): AmbienteBasicoRes {
            return AmbienteBasicoRes(
                id = ambiente.id!!,
                nome = ambiente.nome,
                localizacao = LocalizacaoRes.from(ambiente.localizacao),
                capacidade = ambiente.capacidade,
                area = ambiente.calcularAreaAmbienteM2()
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AmbienteBasicoRes

        if (capacidade != other.capacidade) return false
        if (nome != other.nome) return false
        if (localizacao != other.localizacao) return false
        if (area != other.area) return false

        return true
    }

    override fun hashCode(): Int {
        var result = capacidade
        result = 31 * result + nome.hashCode()
        result = 31 * result + localizacao.hashCode()
        result = 31 * result + area.hashCode()
        return result
    }

}
