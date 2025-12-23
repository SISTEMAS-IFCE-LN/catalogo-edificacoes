package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.factory.AmbienteFactory
import br.edu.ifce.ambientes_internos.model.domain.factory.EsquadriaFactory
import br.edu.ifce.ambientes_internos.model.domain.factory.GeometriaFactory
import br.edu.ifce.ambientes_internos.model.dto.ambiente.*
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.ListaGeometriasAmbienteRes
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import br.edu.ifce.ambientes_internos.model.repository.LocalizacaoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AmbienteNaoPublicadoUseCases(val repoAmb: AmbienteRepository, val repoLoc: LocalizacaoRepository) :
    IAmbienteNaoPublicadoUseCases {

    @Transactional
    override fun cadastrarAmbiente(ambienteReq: AmbienteReq): AmbienteRes {
        val ambiente = AmbienteFactory.criar(ambienteReq)
        repoLoc.findByLocalizacao(ambiente.localizacao).ifPresent {
            ambiente.localizacao = it
            if (repoAmb.existsByNomeAndLocalizacaoId(ambiente.nome, it.id!!)) {
                throw IllegalArgumentException("Já existe um ambiente com esse nome nessa localização")
            }
        }
        return AmbienteRes.from(repoAmb.save(ambiente))
    }

    override fun obterAmbientePorId(id: Long): AmbienteRes {
        val ambiente =
            repoAmb.findByIdAndStatus(id, StatusAmbiente.NAO_PUBLICADO)
                .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        return AmbienteRes.from(ambiente)
    }

    @Transactional
    override fun atualizarDadosBasicosAmbiente(
        id: Long,
        ambienteAtualizado: AmbienteBasicoReq
    ): AmbienteBasicoRes {
        val ambienteExistente =
            repoAmb.findByIdAndStatus(id, StatusAmbiente.NAO_PUBLICADO)
                .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        ambienteExistente.nome = ambienteAtualizado.nome
        ambienteExistente.capacidade = ambienteAtualizado.capacidade
        ambienteExistente.localizacao.bloco = ambienteAtualizado.localizacao.bloco
        ambienteExistente.localizacao.unidade = ambienteAtualizado.localizacao.unidade
        ambienteExistente.localizacao.andar = ambienteAtualizado.localizacao.andar
        repoLoc.findByLocalizacao(ambienteExistente.localizacao).ifPresent {
            ambienteExistente.localizacao = it
            if (repoAmb.existsByNomeAndLocalizacaoIdAndIdNot(
                    ambienteExistente.nome,
                    it.id!!,
                    ambienteExistente.id!!
                )
            ) throw IllegalArgumentException("Já existe um ambiente com esse nome nessa localização")
        }
        return AmbienteBasicoRes.from(repoAmb.save(ambienteExistente))
    }

    override fun incluirGeometriasAmbiente(
        id: Long,
        geometriasAdd: Set<GeometriaAmbienteReq>
    ): ListaGeometriasAmbienteRes {
        val ambienteExistente =
            repoAmb.findByIdAndStatus(id, StatusAmbiente.NAO_PUBLICADO)
                .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        val geometrias = geometriasAdd.map { GeometriaFactory.criar(it) }.toMutableSet()

        ambienteExistente.geometrias.addAll(geometrias)

        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        val geometriasRes = ambienteAtualizado.geometrias.map { GeometriaAmbienteRes.from(it) }

        return ListaGeometriasAmbienteRes(
            geometrias = geometriasRes,
            areaTotal = ambienteAtualizado.calcularAreaAmbienteM2()
        )
    }

    override fun atualizarGeometriasAmbiente(
        id: Long,
        geometriasAtualizadas: Set<GeometriaAmbienteReq>
    ): ListaGeometriasAmbienteRes {
        val ambienteExistente =
            repoAmb.findByIdAndStatus(id, StatusAmbiente.NAO_PUBLICADO)
                .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        val geometrias = geometriasAtualizadas.map { GeometriaFactory.criar(it) }.toMutableSet()

        ambienteExistente.geometrias.clear()
        ambienteExistente.geometrias.addAll(geometrias)

        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        val geometriasRes = ambienteAtualizado.geometrias.map { GeometriaAmbienteRes.from(it) }

        return ListaGeometriasAmbienteRes(
            geometrias = geometriasRes,
            areaTotal = ambienteAtualizado.calcularAreaAmbienteM2()
        )
    }

    override fun incluirPesDireitosAmbiente(
        id: Long,
        pesDireitos: Set<BigDecimal>
    ): Set<BigDecimal> {
        val ambienteExistente =
            repoAmb.findByIdAndStatus(id, StatusAmbiente.NAO_PUBLICADO)
                .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        ambienteExistente.pesDireitos.addAll(pesDireitos)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return ambienteAtualizado.pesDireitos
    }

    override fun atualizarPesDireitosAmbiente(
        id: Long,
        pesDireitos: Set<BigDecimal>
    ): Set<BigDecimal> {
        val ambienteExistente =
            repoAmb.findByIdAndStatus(id, StatusAmbiente.NAO_PUBLICADO)
                .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        ambienteExistente.pesDireitos.clear()
        ambienteExistente.pesDireitos.addAll(pesDireitos)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return ambienteAtualizado.pesDireitos
    }

    override fun incluirEsquadriasAmbiente(
        id: Long,
        esquadrias: Set<EsquadriaReq>
    ): EsquadriasDetalhesRes {
        val ambienteExistente =
            repoAmb.findByIdAndStatus(id, StatusAmbiente.NAO_PUBLICADO)
                .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        val esquadriasNovas = esquadrias.map { EsquadriaFactory.criar(it) }
        ambienteExistente.esquadrias.addAll(esquadriasNovas)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return EsquadriasDetalhesRes.from(ambienteAtualizado)
    }

    override fun atualizarEsquadriasAmbiente(
        id: Long,
        esquadrias: Set<EsquadriaReq>
    ): EsquadriasDetalhesRes {
        val ambienteExistente =
            repoAmb.findByIdAndStatus(id, StatusAmbiente.NAO_PUBLICADO)
                .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        val esquadriasAtualizadas = esquadrias.map { EsquadriaFactory.criar(it) }
        ambienteExistente.esquadrias.clear()
        ambienteExistente.esquadrias.addAll(esquadriasAtualizadas)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return EsquadriasDetalhesRes.from(ambienteAtualizado)
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

    override fun deletarAmbientes(ids: Set<Long>) {
        TODO("Not yet implemented")
    }

}