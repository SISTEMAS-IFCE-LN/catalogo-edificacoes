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

    // Igualdade por valores de negócio (ignora id)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AmbienteRes) return false

        if (nome != other.nome) return false
        if (localizacao != other.localizacao) return false
        if (tipo != other.tipo) return false
        if (capacidade != other.capacidade) return false
        if (geometrias.size != other.geometrias.size) return false
        if (!geometrias.containsAll(other.geometrias)) return false
        if (pesDireitos.size != other.pesDireitos.size) return false
        if (!pesDireitos.containsAll(other.pesDireitos)) return false
        if (esquadriasDetalhes != other.esquadriasDetalhes) return false
        if (informacaoAdicional != other.informacaoAdicional) return false
        if (areaAmbiente != other.areaAmbiente) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nome.hashCode()
        result = 31 * result + localizacao.hashCode()
        result = 31 * result + tipo.hashCode()
        result = 31 * result + capacidade
        result = 31 * result + geometrias.hashCode()
        result = 31 * result + pesDireitos.hashCode()
        result = 31 * result + esquadriasDetalhes.hashCode()
        result = 31 * result + informacaoAdicional.hashCode()
        result = 31 * result + areaAmbiente.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }
}
