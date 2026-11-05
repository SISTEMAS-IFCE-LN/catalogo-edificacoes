package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteNaoPublicadoUseCases
import br.edu.ifce.ambientes_internos.model.application.interfaces.IBlocoUseCases
import br.edu.ifce.ambientes_internos.model.application.interfaces.ICampusUseCases
import br.edu.ifce.ambientes_internos.model.application.interfaces.IUnidadeUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.factory.AmbienteFactory
import br.edu.ifce.ambientes_internos.model.domain.factory.EsquadriaFactory
import br.edu.ifce.ambientes_internos.model.domain.factory.GeometriaFactory
import br.edu.ifce.ambientes_internos.model.domain.factory.LocalizacaoFactory
import br.edu.ifce.ambientes_internos.model.dto.ambiente.*
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriaReq
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteReq
import br.edu.ifce.ambientes_internos.model.dto.geometria.GeometriaAmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.geometria.ListaGeometriasAmbienteRes
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import br.edu.ifce.ambientes_internos.model.repository.LocalizacaoRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AmbienteNaoPublicadoUseCases(
    repoAmb: AmbienteRepository,
    private val repoLoc: LocalizacaoRepository,
    private val repoBloco: BlocoRepository,
    private val repoUnidade: UnidadeRepository,
    private val repoCampus: CampusRepository,
    private val campusUC: ICampusUseCases,
    private val unidadeUC: IUnidadeUseCases,
    private val blocoUC: IBlocoUseCases
) : IAmbienteNaoPublicadoUseCases, BaseUseCases(StatusAmbiente.NAO_PUBLICADO, repoAmb) {

    private fun verificarNomeLocalizacaoCadastro(ambiente: Ambiente) {
        ambiente.localizacao = resolverLocalizacao(ambiente.localizacao)

        repoLoc.findByLocalizacao(ambiente.localizacao).ifPresent { localizacao ->
            if (repoAmb.existsByNomeAndLocalizacaoId(ambiente.nome, localizacao.id!!)) {
                throw IllegalArgumentException("Já existe um ambiente com esse nome nessa localização")
            }
            ambiente.localizacao = localizacao
        }
    }

    // Na alteração de tipo o ambiente antigo é removido e um novo é criado.
    // A unicidade (RN-1.7) deve excluir o registro substituído, que ainda
    // existe no banco no momento da validação.
    private fun verificarNomeLocalizacaoSubstituicao(ambiente: Ambiente, idSubstituido: Long) {
        ambiente.localizacao = resolverLocalizacao(ambiente.localizacao)

        repoLoc.findByLocalizacao(ambiente.localizacao).ifPresent { localizacao ->
            if (repoAmb.existsByNomeAndLocalizacaoIdAndIdNot(ambiente.nome, localizacao.id!!, idSubstituido)) {
                throw IllegalArgumentException("Já existe um ambiente com esse nome nessa localização")
            }
            ambiente.localizacao = localizacao
        }
    }

    private fun verificarNomeLocalizacaoAlteracao(
        ambiente: Ambiente,
        novoNome: String,
        localizacaoCandidata: Localizacao
    ) {
        val localizacaoResolvida = resolverLocalizacao(localizacaoCandidata)

        repoLoc.findByLocalizacao(localizacaoResolvida).ifPresentOrElse(
            { localizacao ->
                if (repoAmb.existsByNomeAndLocalizacaoIdAndIdNot(novoNome, localizacao.id!!, ambiente.id!!)) {
                    throw IllegalArgumentException("Já existe um ambiente com esse nome nessa localização")
                }
                ambiente.localizacao = localizacao
            },
            {
                ambiente.localizacao = localizacaoResolvida
            }
        )
    }

    private fun obterAmbientesPorIds(ids: Set<Long>): List<Ambiente> {
        val ambientes = repoAmb.findAllByIdInAndStatus(ids, status)
        if (ambientes.isEmpty()) {
            throw NoSuchElementException("Nenhum ambiente encontrado para os IDs fornecidos")
        }
        return ambientes
    }

    private fun deletarLocalizacaoOrfao(localizacaoExistenteId: Long?, localizacaoAtualizadaId: Long? = null) {
        if (localizacaoExistenteId == localizacaoAtualizadaId) return
        val id = localizacaoExistenteId ?: return
        val localizacao = repoLoc.findById(id).orElse(null) ?: return
        deletarLocalizacoesOrfasDoBloco(localizacao.bloco.id!!)
    }

    // Remove, em ordem, as localizações sem ambiente do bloco e as referências
    // (bloco -> unidade -> campus) que ficarem sem filhos (RN-1.19).
    private fun deletarLocalizacoesOrfasDoBloco(blocoId: Long) {
        val bloco = repoBloco.findById(blocoId).orElse(null) ?: return
        val unidadeId = bloco.unidade.id
        val campusId = bloco.unidade.campus.id

        repoLoc.deleteOrphansByBloco(blocoId)
        repoBloco.deleteIfOrphan(blocoId)
        unidadeId?.let { repoUnidade.deleteIfOrphan(it) }
        campusId?.let { repoCampus.deleteIfOrphan(it) }
    }

    private fun resolverLocalizacao(localizacao: Localizacao): Localizacao {
        val blocoRef = localizacao.bloco
        val unidadeRef = blocoRef.unidade
        val campusRef = unidadeRef.campus

        val campus = campusRef.id?.let { campusUC.obterPorId(it) }
            ?: campusUC.resolverOuCriar(campusRef.nome)

        val unidade = unidadeRef.id?.let { unidadeUC.obterPorIdEValidarCampus(it, campus.id!!) }
            ?: unidadeUC.resolverOuCriar(campus, unidadeRef.nome)

        val bloco = blocoRef.id?.let { blocoUC.obterPorIdEValidarUnidade(it, unidade.id!!) }
            ?: blocoUC.resolverOuCriar(unidade, blocoRef.nome)

        return Localizacao(bloco = bloco, andar = localizacao.andar)
    }

    @Transactional
    override fun cadastrarAmbiente(ambienteReq: AmbienteReq): AmbienteRes {
        val ambiente = AmbienteFactory.criar(ambienteReq)
        if (ambiente.esquadrias.none { it.tipo == TipoEsquadria.PORTA }) {
            throw IllegalArgumentException("O ambiente deve possuir pelo menos uma porta")
        }
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
        val localizacaoCandidata = LocalizacaoFactory.criar(ambienteBasicoReq.localizacao)
        // Valida antes de mutar a entidade: a query de unicidade faz auto-flush
        // e a constraint não pode ser atingida antes da mensagem amigável.
        verificarNomeLocalizacaoAlteracao(ambienteExistente, ambienteBasicoReq.nome, localizacaoCandidata)
        ambienteExistente.nome = ambienteBasicoReq.nome
        ambienteExistente.capacidade = ambienteBasicoReq.capacidade
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
        val geometrias = geometriasAdd.map { GeometriaFactory.criar(it) }.toSet()

        ambienteExistente.adicionarGeometrias(geometrias)

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
        val geometrias = geometriasAtualizadas.map { GeometriaFactory.criar(it) }.toSet()

        ambienteExistente.substituirGeometrias(geometrias)

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
        val esquadriasNovas = esquadrias.map { EsquadriaFactory.criar(it) }.toSet()
        ambienteExistente.adicionarEsquadrias(esquadriasNovas)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return EsquadriasDetalhesRes.from(ambienteAtualizado)
    }

    @Transactional
    override fun atualizarEsquadriasAmbiente(
        id: Long,
        esquadrias: Set<EsquadriaReq>
    ): EsquadriasDetalhesRes {
        val ambienteExistente = obterAmbiente(id)
        val esquadriasAtualizadas = esquadrias.map { EsquadriaFactory.criar(it) }.toSet()
        ambienteExistente.substituirEsquadrias(esquadriasAtualizadas)
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return EsquadriasDetalhesRes.from(ambienteAtualizado)
    }

    @Transactional
    override fun atualizarInformacaoAdicionalAmbiente(
        id: Long,
        informacaoAdicional: String
    ): String {
        val novaInformacaoAdicional = informacaoAdicional.trim()
        val ambienteExistente = obterAmbiente(id)
        // PATCH idempotente: repetir o valor atual (inclusive vazio) não é erro.
        if (novaInformacaoAdicional == ambienteExistente.informacaoAdicional) {
            return ambienteExistente.informacaoAdicional
        }
        ambienteExistente.informacaoAdicional = novaInformacaoAdicional
        val ambienteAtualizado = repoAmb.save(ambienteExistente)
        return ambienteAtualizado.informacaoAdicional
    }

    @Transactional
    override fun alterarTipoDadosAmbiente(
        id: Long,
        ambiente: AmbienteReq
    ): AmbienteRes {
        val ambienteExistente = obterAmbiente(id)
        val localizacaoExistenteId = ambienteExistente.localizacao.id
        val ambienteAtualizado = AmbienteFactory.criar(ambiente)
        verificarNomeLocalizacaoSubstituicao(ambienteAtualizado, ambienteExistente.id!!)

        // A constraint uk_ambiente_nome_localizacao é checada no INSERT (IDENTITY).
        // Remove e faz flush do antigo antes de inserir o novo; o @Transactional
        // desfaz o delete se a inserção falhar.
        repoAmb.delete(ambienteExistente)
        repoAmb.flush()

        val ambienteSalvo = repoAmb.save(ambienteAtualizado)
        repoAmb.flush()
        deletarLocalizacaoOrfao(localizacaoExistenteId, ambienteSalvo.localizacao.id)
        return AmbienteRes.from(ambienteSalvo)
    }

    @Transactional
    override fun duplicarAmbiente(
        id: Long,
        dados: AmbienteNomeLocalizacaoReq
    ): AmbienteRes {
        val ambienteExistente = obterAmbiente(id)
        val ambienteDuplicado = AmbienteFactory.clonar(ambienteExistente)

        ambienteDuplicado.id = null
        ambienteDuplicado.nome = dados.nome
        ambienteDuplicado.localizacao = LocalizacaoFactory.criar(dados.localizacao)
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
        val blocosIds = ambientes.mapNotNull { it.localizacao.bloco.id }.toSet()

        repoAmb.deleteAll(ambientes)
        repoAmb.flush()

        blocosIds.forEach { deletarLocalizacoesOrfasDoBloco(it) }
    }

}
