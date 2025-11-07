package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaReq
import java.math.BigDecimal

data class AmbienteReq(
    val nome: String,
    val localizacao: String,
    val tipo: TipoAmbiente,
    val capacidade: Int,
    val geometrias: Set<GeometriaReq>,
    val pesDireitos: Set<BigDecimal>,
    val esquadrias: Set<EsquadriaReq>,
    val informacaoAdicional: String = ""
)
