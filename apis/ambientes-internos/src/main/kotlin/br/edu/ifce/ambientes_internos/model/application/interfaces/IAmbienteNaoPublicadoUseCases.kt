package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.dto.ambiente.*
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.ListaGeometriasAmbienteRes
import java.math.BigDecimal

interface IAmbienteNaoPublicadoUseCases : IAmbienteUseCases<AmbientesBasicosRes, AmbienteRes> {

    fun cadastrarAmbiente(ambienteReq: AmbienteReq): AmbienteRes

    fun atualizarDadosBasicosAmbiente(id: Long, ambienteAtualizado: AmbienteBasicoReq): AmbienteBasicoRes

    fun incluirGeometriasAmbiente(id: Long, geometriasAdd: Set<GeometriaAmbienteReq>): ListaGeometriasAmbienteRes

    fun atualizarGeometriasAmbiente(id: Long, geometrias: Set<GeometriaAmbienteReq>): ListaGeometriasAmbienteRes

    fun incluirPesDireitosAmbiente(id: Long, pesDireitos: Set<BigDecimal>): Set<BigDecimal>

    fun atualizarPesDireitosAmbiente(id: Long, pesDireitos: Set<BigDecimal>): Set<BigDecimal>

    fun incluirEsquadriasAmbiente(id: Long, esquadrias: Set<EsquadriaReq>): EsquadriasDetalhesRes

    fun atualizarEsquadriasAmbiente(id: Long, esquadrias: Set<EsquadriaReq>): EsquadriasDetalhesRes

    fun atualizarInformacaoAdicionalAmbiente(id: Long, informacaoAdicional: String): String

    fun alterarTipoDadosAmbiente(id: Long, ambiente: AmbienteReq): AmbienteRes

    fun duplicarAmbiente(id: Long, dados: AmbienteNomeLocalizacaoReq): AmbienteRes

    fun enviarValidacaoAmbientes(ids: Set<Long>)

    fun deletarAmbientes(ids: Set<Long>)

}