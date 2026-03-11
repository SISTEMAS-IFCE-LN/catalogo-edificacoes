package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.data.domain.Pageable

open class BaseUseCases(val status: StatusAmbiente) {

    fun obterAmbienteMetodos(id: Long, repoAmb: AmbienteRepository): Ambiente = repoAmb.findByIdAndStatus(id, status)
        .orElseThrow { NoSuchElementException("Ambiente não encontrado") }

    fun obterAmbientePorId(id: Long, repoAmb: AmbienteRepository): AmbienteRes {
        val ambiente = obterAmbienteMetodos(id, repoAmb)
        return AmbienteRes.from(ambiente)
    }

    fun listarAmbientes(pageable: Pageable, repoAmb: AmbienteRepository): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findAllByStatus(status, pageable)
        return AmbientesBasicosPaginadosRes.from(page)
    }

    fun listarAmbientesPorTipo(
        tipo: String,
        pageable: Pageable,
        repoAmb: AmbienteRepository
    ): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findByTipoAndStatus(tipo, status, pageable)
        return AmbientesBasicosPaginadosRes.from(page)
    }

    fun listarAmbientesPorNome(
        nome: String,
        pageable: Pageable,
        repoAmb: AmbienteRepository
    ): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findByNomeContainingIgnoreCaseAndStatus(nome, status, pageable)
        return AmbientesBasicosPaginadosRes.from(page)
    }

    fun listarAmbientesPorLocalizacao(
        localizacao: LocalizacaoPesquisaReq,
        pageable: Pageable,
        repoAmb: AmbienteRepository
    ): AmbientesBasicosPaginadosRes {
        if (localizacao.bloco.isNullOrBlank()
            && localizacao.unidade.isNullOrBlank()
            && localizacao.andar == null
        ) {
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