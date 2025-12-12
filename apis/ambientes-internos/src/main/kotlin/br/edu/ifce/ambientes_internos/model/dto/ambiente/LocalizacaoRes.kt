package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Unidade

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
}
