package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import kotlin.math.min

abstract class BaseUseCases(
    protected val status: StatusAmbiente,
    protected val repoAmb: AmbienteRepository
) : IAmbienteUseCases<AmbienteRes> {

    companion object {
        protected const val PAGE_SIZE_MAX = 100
    }

    protected fun limitarPageable(pageable: Pageable): Pageable {
        if (pageable.isUnpaged) {
            return PageRequest.of(0, PAGE_SIZE_MAX)
        }
        return PageRequest.of(pageable.pageNumber, min(pageable.pageSize, PAGE_SIZE_MAX), pageable.sort)
    }

    protected fun obterAmbiente(id: Long): Ambiente = repoAmb.findByIdAndStatus(id, status)
        .orElseThrow { NoSuchElementException("Ambiente não encontrado") }

    @Transactional(readOnly = true)
    override fun obterAmbientePorId(id: Long): AmbienteRes {
        return AmbienteRes.from(obterAmbiente(id))
    }

    @Transactional(readOnly = true)
    override fun listarAmbientes(pageable: Pageable): AmbientesBasicosPaginadosRes {
        val pageableLimitado = limitarPageable(pageable)
        val page = repoAmb.findByStatus(status, pageableLimitado)
        return AmbientesBasicosPaginadosRes.from(page)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorTipo(
        tipo: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        val tipoNormalizado = tipo.trim()
        val pageableLimitado = limitarPageable(pageable)
        val page = repoAmb.findByTipoAndStatus(tipoNormalizado, status, pageableLimitado)
        return AmbientesBasicosPaginadosRes.from(page)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorNome(
        nome: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        val nomeNormalizado = nome.trim()
        val pageableLimitado = limitarPageable(pageable)
        val page = repoAmb.findByNomeContainingIgnoreCaseAndStatus(nomeNormalizado, status, pageableLimitado)
        return AmbientesBasicosPaginadosRes.from(page)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorLocalizacao(
        localizacao: LocalizacaoPesquisaReq,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        if (localizacao.bloco.isNullOrBlank() && localizacao.unidade.isNullOrBlank() && localizacao.andar == null) {
            throw IllegalArgumentException("Pelo menos um campo de localização deve ser preenchido")
        }

        val bloco = localizacao.bloco?.trim()?.ifBlank { null }?.replace(" ", "_")
        val unidade = localizacao.unidade?.trim()?.ifBlank { null }?.replace(" ", "_")
        val andar = localizacao.andar?.toString()
        val pageableLimitado = limitarPageable(pageable)

        val page = repoAmb.findByLocalizacaoContainingIgnoreCaseAndStatus(
            bloco = bloco,
            unidade = unidade,
            andar = andar,
            status = status,
            pageable = pageableLimitado
        )
        return AmbientesBasicosPaginadosRes.from(page)
    }

}