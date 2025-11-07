package br.edu.ifce.ambientes_internos.model.use_cases

import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteBasicoReq
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteBasicoRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteDuplicadoReq
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaRes
import java.math.BigDecimal

interface AmbienteNaoPublicadoUseCase: AmbienteUseCases<AmbienteBasicoRes, AmbienteRes> {

    fun cadastrarAmbiente(ambiente: AmbienteReq): AmbienteRes

    fun atualizarDadosBasicosAmbiente(id: Long, ambiente: AmbienteBasicoReq): AmbienteBasicoRes

    fun incluirGeometriasAmbiente(id: Long, geometrias: Set<GeometriaReq>): Set<GeometriaRes>

    fun atualizarGeometriasAmbiente(id: Long, geometrias: Set<GeometriaReq>): Set<GeometriaRes>

    fun incluirPesDireitosAmbiente(id: Long, pesDireitos: Set<BigDecimal>): Set<BigDecimal>

    fun atualizarPesDireitosAmbiente(id: Long, pesDireitos: Set<BigDecimal>): Set<BigDecimal>

    fun incluirEsquadriasAmbiente(id: Long, esquadrias: Set<EsquadriaReq>): Set<EsquadriaRes>

    fun atualizarEsquadriasAmbiente(id: Long, esquadrias: Set<EsquadriaReq>): Set<EsquadriaRes>

    fun atualizarInformacaoAdicionalAmbiente(id: Long, informacaoAdicional: String): String

    fun alterarTipoDadosAmbiente(id: Long, ambiente: AmbienteReq): AmbienteRes

    fun duplicarAmbiente(id: Long, dados: AmbienteDuplicadoReq): AmbienteRes

    fun enviarValidacaoAmbientes(ids: Set<Long>)

}