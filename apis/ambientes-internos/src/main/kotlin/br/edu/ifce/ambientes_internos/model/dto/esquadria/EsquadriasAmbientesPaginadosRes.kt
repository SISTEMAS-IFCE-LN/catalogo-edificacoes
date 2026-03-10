package br.edu.ifce.ambientes_internos.model.dto.esquadria

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.dto.DadosPaginacao
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteNomeLocalizacaoRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoRes
import org.springframework.data.domain.Page

data class EsquadriasAmbientesPaginadosRes(
    val ambientes: Map<AmbienteNomeLocalizacaoRes, EsquadriasDetalhesRes>,
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
            return EsquadriasAmbientesPaginadosRes(
                ambientes = ambientes,
                dadosPaginacao = DadosPaginacao.from(page)
            )
        }
    }
}
