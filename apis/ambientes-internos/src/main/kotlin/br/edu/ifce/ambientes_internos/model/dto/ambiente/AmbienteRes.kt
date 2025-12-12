package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteRes
import java.math.BigDecimal

data class AmbienteRes(
    val id: Long,
    val nome: String,
    val localizacao: LocalizacaoRes,
    val tipo: TipoAmbiente,
    val capacidade: Int,
    val geometrias: List<GeometriaAmbienteRes>,
    val areaAmbiente: BigDecimal,
    val pesDireitos: List<BigDecimal>,
    val esquadriasDetalhes: EsquadriasDetalhesRes,
    val informacaoAdicional: String,
    val status: StatusAmbiente
) {
    companion object {
        fun from(ambiente: Ambiente): AmbienteRes {
            return AmbienteRes(
                id = ambiente.id!!,
                nome = ambiente.nome,
                localizacao = LocalizacaoRes.from(ambiente.localizacao),
                tipo = ambiente.tipo!!,
                capacidade = ambiente.capacidade,
                geometrias = ambiente.geometrias.map { GeometriaAmbienteRes.from(it) },
                areaAmbiente = ambiente.calcularAreaAmbienteM2(),
                pesDireitos = ambiente.pesDireitos.toList(),
                esquadriasDetalhes = EsquadriasDetalhesRes.from(ambiente),
                informacaoAdicional = ambiente.informacaoAdicional,
                status = ambiente.status
            )
        }
    }
}
