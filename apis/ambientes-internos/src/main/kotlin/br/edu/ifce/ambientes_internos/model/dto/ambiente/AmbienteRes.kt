package br.edu.ifce.ambientes_internos.model.dto.ambiente

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaReq
import java.math.BigDecimal

data class AmbienteRes(
    val id: Long,
    val nome: String,
    val localizacao: String,
    val tipo: TipoAmbiente,
    val capacidade: Int,
    val geometrias: Set<GeometriaReq>,
    val pesDireitos: Set<BigDecimal>,
    val esquadrias: Set<EsquadriaRes>,
    val informacaoAdicional: String,
    val status: StatusAmbiente,
    val areaAmbiente: BigDecimal,
    val areaEsquadrias: BigDecimal
)
