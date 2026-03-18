package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.dto.DadosPaginacao
import org.springframework.data.domain.Page
import java.math.BigDecimal

data class AmbientesBasicosPaginadosRes(
    val ambientes: List<AmbienteBasicoRes>,
    val areaTotal: BigDecimal,
    val dadosPaginacao: DadosPaginacao
) {
    companion object {
        fun from(page: Page<Ambiente>): AmbientesBasicosPaginadosRes {
            return AmbientesBasicosPaginadosRes(
                ambientes = page.content.map { AmbienteBasicoRes.from(it) },
                areaTotal = page.content.fold(BigDecimal.ZERO) { acc, ambiente ->
                    acc.add(ambiente.calcularAreaAmbienteM2())
                },
                dadosPaginacao = DadosPaginacao.from(page)
            )
        }
    }
}

