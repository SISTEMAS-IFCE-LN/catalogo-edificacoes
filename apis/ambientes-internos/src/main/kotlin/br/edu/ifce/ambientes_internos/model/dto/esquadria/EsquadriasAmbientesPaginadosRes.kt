package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.dto.DadosPaginacao
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteNomeLocalizacaoRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoRes
import org.springframework.data.domain.Page
import java.math.BigDecimal

data class EsquadriasAmbientesPaginadosRes(
    val ambientes: Map<AmbienteNomeLocalizacaoRes, EsquadriasDetalhesRes>,
    val totalTipoMaterial: List<EsquadriaTipoMaterialRes>,
    val dadosPaginacao: DadosPaginacao
) {
    companion object {
        fun from(page: Page<Ambiente>): EsquadriasAmbientesPaginadosRes {
            val ambientes = page.content.associate { ambiente ->
                AmbienteNomeLocalizacaoRes(
                    id = ambiente.id!!,
                    nome = ambiente.nome,
                    localizacao = LocalizacaoRes.from(ambiente.localizacao)
                ) to EsquadriasDetalhesRes.from(ambiente)
            }
            val totalTipoMaterial = page.content.flatMap { ambiente ->
                ambiente.calcularAreaEsquadriasPorTipoMaterial().map { EsquadriaTipoMaterialRes.from(it) }
            }.groupBy { Pair(it.tipo, it.material) }
                .mapValues { entry ->
                    entry.value.fold(BigDecimal.ZERO) { acc, res ->
                        acc.add(res.area)
                    }
                }.map { EsquadriaTipoMaterialRes.from(it) }
            return EsquadriasAmbientesPaginadosRes(
                ambientes = ambientes,
                totalTipoMaterial = totalTipoMaterial,
                dadosPaginacao = DadosPaginacao.from(page)
            )
        }
    }
}
