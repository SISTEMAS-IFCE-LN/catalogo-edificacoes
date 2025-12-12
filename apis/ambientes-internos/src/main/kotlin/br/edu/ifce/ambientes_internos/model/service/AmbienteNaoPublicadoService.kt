package br.edu.ifce.ambientes_internos.model.service

import br.edu.ifce.ambientes_internos.model.domain.ambientes.factories.AmbienteFactory
import br.edu.ifce.ambientes_internos.model.dto.ambiente.*
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.ListaGeometriasAmbienteRes
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import br.edu.ifce.ambientes_internos.model.use_cases.AmbienteNaoPublicadoUseCase
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AmbienteNaoPublicadoService(val repository: AmbienteRepository) : AmbienteNaoPublicadoUseCase {

    override fun cadastrarAmbiente(ambienteReq: AmbienteReq): AmbienteRes {
        val ambiente = repository.save(AmbienteFactory.criar(ambienteReq))
        return AmbienteRes.from(ambiente)
    }

    override fun atualizarDadosBasicosAmbiente(
        id: Long,
        ambiente: AmbienteBasicoReq
    ): AmbienteBasicoRes {
        TODO("Not yet implemented")
    }

    override fun incluirGeometriasAmbiente(
        id: Long,
        geometrias: Set<GeometriaAmbienteReq>
    ): ListaGeometriasAmbienteRes {
        TODO("Not yet implemented")
    }

    override fun atualizarGeometriasAmbiente(
        id: Long,
        geometrias: Set<GeometriaAmbienteReq>
    ): ListaGeometriasAmbienteRes {
        TODO("Not yet implemented")
    }

    override fun incluirPesDireitosAmbiente(
        id: Long,
        pesDireitos: Set<BigDecimal>
    ): Set<BigDecimal> {
        TODO("Not yet implemented")
    }

    override fun atualizarPesDireitosAmbiente(
        id: Long,
        pesDireitos: Set<BigDecimal>
    ): Set<BigDecimal> {
        TODO("Not yet implemented")
    }

    override fun incluirEsquadriasAmbiente(
        id: Long,
        esquadrias: Set<EsquadriaReq>
    ): EsquadriasDetalhesRes {
        TODO("Not yet implemented")
    }

    override fun atualizarEsquadriasAmbiente(
        id: Long,
        esquadrias: Set<EsquadriaReq>
    ): EsquadriasDetalhesRes {
        TODO("Not yet implemented")
    }

    override fun atualizarInformacaoAdicionalAmbiente(
        id: Long,
        informacaoAdicional: String
    ): String {
        TODO("Not yet implemented")
    }

    override fun alterarTipoDadosAmbiente(
        id: Long,
        ambiente: AmbienteReq
    ): AmbienteRes {
        TODO("Not yet implemented")
    }

    override fun duplicarAmbiente(
        id: Long,
        dados: AmbienteNomeLocalizacaoReq
    ): AmbienteRes {
        TODO("Not yet implemented")
    }

    override fun enviarValidacaoAmbientes(ids: Set<Long>) {
        TODO("Not yet implemented")
    }

    override fun listarAmbientes(): AmbientesBasicosRes {
        TODO("Not yet implemented")
    }

    override fun listarAmbientesPorTipo(tipo: String): AmbientesBasicosRes {
        TODO("Not yet implemented")
    }

    override fun listarAmbientesPorNome(nome: String): AmbientesBasicosRes {
        TODO("Not yet implemented")
    }

    override fun listarAmbientesPorLocalizacao(localizacao: String): AmbientesBasicosRes {
        TODO("Not yet implemented")
    }

    override fun obterAmbientePorId(id: Long): AmbienteRes {
        TODO("Not yet implemented")
    }

    override fun deletarAmbientes(ids: Set<Long>) {
        TODO("Not yet implemented")
    }

}