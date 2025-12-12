package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import java.math.BigDecimal

data class AmbienteReq(
    val tipo: TipoAmbiente,
    val nome: String,
    val localizacao: LocalizacaoReq,
    val capacidade: Int,
    val geometrias: Set<GeometriaAmbienteReq>,
    val pesDireitos: Set<BigDecimal>,
    val esquadrias: Set<EsquadriaReq>,
    val informacaoAdicional: String = ""
)
