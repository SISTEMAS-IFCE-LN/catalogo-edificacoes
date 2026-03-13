package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

abstract class BaseUseCases(
    protected val status: StatusAmbiente,
    protected val repoAmb: AmbienteRepository
) : IAmbienteUseCases<AmbienteRes> {

    protected fun obterAmbiente(id: Long): Ambiente = repoAmb.findByIdAndStatus(id, status)
        .orElseThrow { NoSuchElementException("Ambiente não encontrado") }

    @Transactional(readOnly = true)
    override fun obterAmbientePorId(id: Long): AmbienteRes {
        return AmbienteRes.from(obterAmbiente(id))
    }

    @Transactional(readOnly = true)
    override fun listarAmbientes(pageable: Pageable): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findAllByStatus(status, pageable)
        return AmbientesBasicosPaginadosRes.from(page)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorTipo(
        tipo: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findByTipoAndStatus(tipo, status, pageable)
        return AmbientesBasicosPaginadosRes.from(page)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorNome(
        nome: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findByNomeContainingIgnoreCaseAndStatus(nome, status, pageable)
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

        val page = repoAmb.findByLocalizacaoContainingIgnoreCaseAndStatus(
            bloco = bloco,
            unidade = unidade,
            andar = andar,
            status = status,
            pageable = pageable
        )
        return AmbientesBasicosPaginadosRes.from(page)
    }

}