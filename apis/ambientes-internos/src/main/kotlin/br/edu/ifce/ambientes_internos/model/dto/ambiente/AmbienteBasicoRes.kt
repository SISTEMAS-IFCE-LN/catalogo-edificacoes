package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Ambiente
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
}
