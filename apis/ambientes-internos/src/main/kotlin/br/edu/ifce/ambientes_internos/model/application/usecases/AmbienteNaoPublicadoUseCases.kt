package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
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
class AmbienteNaoPublicadoUseCases(
    repoAmb: AmbienteRepository,
    private val repoLoc: LocalizacaoRepository
) : IAmbienteNaoPublicadoUseCases, BaseUseCases(StatusAmbiente.NAO_PUBLICADO, repoAmb) {

    private fun verificarNomeLocalizacaoCadastro(ambiente: Ambiente) {
        repoLoc.findByLocalizacao(ambiente.localizacao).ifPresent {
            ambiente.localizacao = it
            if (repoAmb.existsByNomeAndLocalizacaoId(ambiente.nome, it.id!!)) {
                throw IllegalArgumentException("Já existe um ambiente com esse nome nessa localização")
            }
        }
    }

    private fun verificarNomeLocalizacaoAlteracao(ambiente: Ambiente) {
        repoLoc.findByLocalizacao(ambiente.localizacao).ifPresent {
            ambiente.localizacao = it
            if (repoAmb.existsByNomeAndLocalizacaoIdAndIdNot(ambiente.nome, it.id!!, ambiente.id!!)) {
                throw IllegalArgumentException("Já existe um ambiente com esse nome nessa localização")
            }
        }
    }

    private fun obterAmbientesPorIds(ids: Set<Long>): List<Ambiente> {
        val ambientes = repoAmb.findAllByIdInAndStatus(ids, status)
        if (ambientes.isEmpty()) {
            throw NoSuchElementException("Nenhum ambiente encontrado para os IDs fornecidos")
        }
        return ambientes
    }

    private fun resolverLocalizacao(
        localizacaoCandidata: Localizacao,
        localizacaoExistente: Localizacao? = null
    ): Localizacao {
        return if (localizacaoCandidata == localizacaoExistente) {
            localizacaoExistente
        } else {
            repoLoc.findByLocalizacao(localizacaoCandidata).orElse(localizacaoCandidata)
        }
    }

    private fun deletarLocalizacaoOrfao(localizacaoExistenteId: Long?, localizacaoAtualizadaId: Long? = null) {
        if (localizacaoExistenteId != localizacaoAtualizadaId) {
            repoLoc.deleteIfOrphan(localizacaoExistenteId ?: return)
        }
    }

    @Transactional
    override fun cadastrarAmbiente(ambienteReq: AmbienteReq): AmbienteRes {
        val ambiente = AmbienteFactory.criar(ambienteReq)
        if (ambiente.esquadrias.none { it.tipo == TipoEsquadria.PORTA }) {
            throw IllegalArgumentException("O ambiente deve possuir pelo menos uma porta")
        }
        val localizacao = resolverLocalizacao(ambiente.localizacao)
        ambiente.localizacao = localizacao
        verificarNomeLocalizacaoCadastro(ambiente)
        return AmbienteRes.from(repoAmb.save(ambiente))
    }

    @Transactional
    override fun atualizarDadosBasicosAmbiente(
        id: Long,
        ambienteBasicoReq: AmbienteBasicoReq
    ): AmbienteBasicoRes {
        val ambienteExistente = obterAmbiente(id)
        val localizacaoExistenteId = ambienteExistente.localizacao.id
        val localizacaoCandidata = Localizacao(
            bloco = ambienteBasicoReq.localizacao.bloco,
            unidade = ambienteBasicoReq.localizacao.unidade,
            andar = ambienteBasicoReq.localizacao.andar
        )
        val localizacaoResolvida = resolverLocalizacao(localizacaoCandidata, ambienteExistente.localizacao)
        ambienteExistente.nome = ambienteBasicoReq.nome
        ambienteExistente.capacidade = ambienteBasicoReq.capacidade
        ambienteExistente.localizacao = localizacaoResolvida
        verificarNomeLocalizacaoAlteracao(ambienteExistente)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        repoAmb.flush()
        deletarLocalizacaoOrfao(localizacaoExistenteId, ambienteAtualizado.localizacao.id)
        return AmbienteBasicoRes.from(ambienteAtualizado)
    }

    @Transactional
    override fun incluirGeometriasAmbiente(
        id: Long,
        geometriasAdd: Set<GeometriaAmbienteReq>
    ): ListaGeometriasAmbienteRes {
        val ambienteExistente = obterAmbiente(id)
        val geometrias = geometriasAdd.map { GeometriaFactory.criar(it) }.toMutableSet()

        ambienteExistente.geometrias.addAll(geometrias)

        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        val geometriasRes = ambienteAtualizado.geometrias.map { GeometriaAmbienteRes.from(it) }

        return ListaGeometriasAmbienteRes(
            geometrias = geometriasRes,
            areaTotal = ambienteAtualizado.calcularAreaAmbienteM2()
        )
    }

    @Transactional
    override fun atualizarGeometriasAmbiente(
        id: Long,
        geometriasAtualizadas: Set<GeometriaAmbienteReq>
    ): ListaGeometriasAmbienteRes {
        val ambienteExistente = obterAmbiente(id)
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

    @Transactional
    override fun incluirPesDireitosAmbiente(
        id: Long,
        pesDireitos: Set<BigDecimal>
    ): Set<BigDecimal> {
        val ambienteExistente = obterAmbiente(id)
        ambienteExistente.pesDireitos.addAll(pesDireitos)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return ambienteAtualizado.pesDireitos
    }

    @Transactional
    override fun atualizarPesDireitosAmbiente(
        id: Long,
        pesDireitos: Set<BigDecimal>
    ): Set<BigDecimal> {
        val ambienteExistente = obterAmbiente(id)
        ambienteExistente.pesDireitos.clear()
        ambienteExistente.pesDireitos.addAll(pesDireitos)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return ambienteAtualizado.pesDireitos
    }

    @Transactional
    override fun incluirEsquadriasAmbiente(
        id: Long,
        esquadrias: Set<EsquadriaReq>
    ): EsquadriasDetalhesRes {
        val ambienteExistente = obterAmbiente(id)
        val esquadriasNovas = esquadrias.map { EsquadriaFactory.criar(it) }
        ambienteExistente.esquadrias.addAll(esquadriasNovas)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return EsquadriasDetalhesRes.from(ambienteAtualizado)
    }

    @Transactional
    override fun atualizarEsquadriasAmbiente(
        id: Long,
        esquadrias: Set<EsquadriaReq>
    ): EsquadriasDetalhesRes {
        val ambienteExistente = obterAmbiente(id)
        val esquadriasAtualizadas = esquadrias.map { EsquadriaFactory.criar(it) }
        ambienteExistente.esquadrias.clear()
        ambienteExistente.esquadrias.addAll(esquadriasAtualizadas)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return EsquadriasDetalhesRes.from(ambienteAtualizado)
    }

    @Transactional
    override fun atualizarInformacaoAdicionalAmbiente(
        id: Long,
        informacaoAdicional: String
    ): String {
        val ambienteExistente = obterAmbiente(id)
        ambienteExistente.informacaoAdicional = informacaoAdicional
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return ambienteAtualizado.informacaoAdicional
    }

    @Transactional
    override fun alterarTipoDadosAmbiente(
        id: Long,
        ambiente: AmbienteReq
    ): AmbienteRes {
        val ambienteExistente = obterAmbiente(id)
        val ambienteAtualizado = AmbienteFactory.criar(ambiente)
        ambienteAtualizado.localizacao = ambienteExistente.localizacao
        verificarNomeLocalizacaoCadastro(ambienteAtualizado)
        val ambienteSalvo = repoAmb.save(ambienteAtualizado)
        repoAmb.delete(ambienteExistente)
        return AmbienteRes.from(ambienteSalvo)
    }

    @Transactional
    override fun duplicarAmbiente(
        id: Long,
        dados: AmbienteNomeLocalizacaoReq
    ): AmbienteRes {
        val ambienteExistente = obterAmbiente(id)
        val ambienteDuplicado = AmbienteFactory.clonar(ambienteExistente)
        val novaLocalizacao = Localizacao(
            bloco = dados.localizacao.bloco,
            unidade = dados.localizacao.unidade,
            andar = dados.localizacao.andar
        )
        ambienteDuplicado.id = null
        ambienteDuplicado.nome = dados.nome
        ambienteDuplicado.localizacao = novaLocalizacao
        verificarNomeLocalizacaoCadastro(ambienteDuplicado)
        return AmbienteRes.from(repoAmb.save(ambienteDuplicado))
    }

    @Transactional
    override fun enviarValidacaoAmbientes(ids: Set<Long>) {
        val ambientes = obterAmbientesPorIds(ids)
        ambientes.forEach { ambiente -> ambiente.status = StatusAmbiente.AGUARDANDO_VALIDACAO }
        repoAmb.saveAll(ambientes)
    }

    @Transactional
    override fun deletarAmbientes(ids: Set<Long>) {
        val ambientes = obterAmbientesPorIds(ids)
        val localizacoesIds = ambientes.mapNotNull { it.localizacao.id }.toSet()

        repoAmb.deleteAll(ambientes)
        repoAmb.flush()

        localizacoesIds.forEach { repoLoc.deleteIfOrphan(it) }
    }

}
