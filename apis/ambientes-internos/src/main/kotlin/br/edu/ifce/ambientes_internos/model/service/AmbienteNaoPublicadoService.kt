package br.edu.ifce.ambientes_internos.model.service

import br.edu.ifce.ambientes_internos.model.domain.ambientes.factories.AmbienteFactory
import br.edu.ifce.ambientes_internos.model.dto.ambiente.*
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.ListaGeometriasAmbienteRes
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import br.edu.ifce.ambientes_internos.model.use_cases.AmbienteNaoPublicadoUseCases
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AmbienteNaoPublicadoService(val repository: AmbienteRepository) : AmbienteNaoPublicadoUseCases {

    override fun cadastrarAmbiente(ambienteReq: AmbienteReq): AmbienteRes {
        val ambiente = repository.save(AmbienteFactory.criar(ambienteReq))
        return AmbienteRes.from(ambiente)
    }

    override fun atualizarDadosBasicosAmbiente(
        id: Long,
        ambienteAtualizado: AmbienteBasicoReq
    ): AmbienteBasicoRes {
        val ambienteExistente =
            repository.findById(id).orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        ambienteExistente.nome = ambienteAtualizado.nome
        ambienteExistente.capacidade = ambienteAtualizado.capacidade
        ambienteExistente.localizacao.bloco = ambienteAtualizado.localizacao.bloco
        ambienteExistente.localizacao.unidade = ambienteAtualizado.localizacao.unidade
        ambienteExistente.localizacao.andar = ambienteAtualizado.localizacao.andar
        return AmbienteBasicoRes.from(repository.save(ambienteExistente))
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