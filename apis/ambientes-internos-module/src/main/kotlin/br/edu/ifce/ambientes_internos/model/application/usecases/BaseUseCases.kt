package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.text.Normalizer
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

    protected fun resolverUnidade(filtro: String?): Unidade? {
        val texto = filtro?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val normalizado = texto.normalizarTexto()
        return Unidade.entries.firstOrNull { unidade ->
            unidade.name.normalizarTexto() == normalizado ||
                    unidade.nome.normalizarTexto() == normalizado
        }
    }

    protected fun resolverTipos(filtro: String?): Set<TipoAmbiente>? {
        val textoNormalizado = filtro?.trim()?.takeIf { it.isNotBlank() }?.normalizarTexto() ?: return null
        return TipoAmbiente.entries.filter { tipo ->
            tipo.name.normalizarTexto().contains(textoNormalizado) ||
                    tipo.nome.normalizarTexto().contains(textoNormalizado)
        }.toSet()
    }

    protected fun paginaVazia(pageable: Pageable): Page<Ambiente> = Page.empty(pageable)

    private fun String.normalizarTexto(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
            .replace(Regex("[^\\p{L}\\p{Nd}]"), "")
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
        val tipos = resolverTipos(tipo)
        if (tipos == null) {
            // Tipo em branco: sem filtro (equivale ao LIKE '%' anterior)
            return listarAmbientes(pageable)
        }
        if (tipos.isEmpty()) {
            return AmbientesBasicosPaginadosRes.from(paginaVazia(limitarPageable(pageable)))
        }
        val pageableLimitado = limitarPageable(pageable)
        val page = repoAmb.findByTiposAndStatus(tipos, status, pageableLimitado)
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
        if (localizacao.blocoId == null && localizacao.unidade.isNullOrBlank() && localizacao.andar == null) {
            throw IllegalArgumentException("Pelo menos um campo de localização deve ser preenchido")
        }

        val unidadeSolicitada = localizacao.unidade?.trim()?.takeIf { it.isNotBlank() }
        val unidade = resolverUnidade(unidadeSolicitada)

        if (unidadeSolicitada != null && unidade == null) {
            return AmbientesBasicosPaginadosRes.from(paginaVazia(limitarPageable(pageable)))
        }

        val pageableLimitado = limitarPageable(pageable)

        val page = repoAmb.findByLocalizacaoAndStatus(
            blocoId = localizacao.blocoId,
            unidades = unidade?.let { setOf(it) } ?: Unidade.entries.toSet(),
            filtrarBloco = localizacao.blocoId != null,
            filtrarUnidades = unidadeSolicitada != null,
            andar = localizacao.andar,
            status = status,
            pageable = pageableLimitado
        )
        return AmbientesBasicosPaginadosRes.from(page)
    }

}