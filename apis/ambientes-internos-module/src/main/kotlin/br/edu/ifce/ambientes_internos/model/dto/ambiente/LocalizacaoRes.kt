package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao

data class ReferenciaRes(val id: Long, val nome: String)

data class LocalizacaoRes(
    val id: Long,
    val campus: ReferenciaRes,
    val unidade: ReferenciaRes,
    val bloco: ReferenciaRes,
    val andar: Int
) {
    companion object {
        fun from(localizacao: Localizacao): LocalizacaoRes {
            val unidade = localizacao.bloco.unidade
            val campus = unidade.campus
            return LocalizacaoRes(
                id = localizacao.id!!,
                campus = ReferenciaRes(campus.id!!, campus.nome),
                unidade = ReferenciaRes(unidade.id!!, unidade.nome),
                bloco = ReferenciaRes(localizacao.bloco.id!!, localizacao.bloco.nome),
                andar = localizacao.andar
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalizacaoRes) return false

        if (campus.nome != other.campus.nome) return false
        if (unidade.nome != other.unidade.nome) return false
        if (bloco.nome != other.bloco.nome) return false
        if (andar != other.andar) return false

        return true
    }

    override fun hashCode(): Int {
        var result = campus.nome.hashCode()
        result = 31 * result + unidade.nome.hashCode()
        result = 31 * result + bloco.nome.hashCode()
        result = 31 * result + andar
        return result
    }
}
