package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade

data class LocalizacaoRes(
    val id: Long,
    val bloco: Bloco,
    val unidade: Unidade,
    val andar: Int
) {
    companion object {
        fun from(localizacao: Localizacao): LocalizacaoRes {
            return LocalizacaoRes(
                id = localizacao.id!!,
                bloco = localizacao.bloco,
                unidade = localizacao.unidade,
                andar = localizacao.andar
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalizacaoRes) return false

        if (bloco != other.bloco) return false
        if (unidade != other.unidade) return false
        if (andar != other.andar) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bloco.hashCode()
        result = 31 * result + unidade.hashCode()
        result = 31 * result + andar
        return result
    }
}
