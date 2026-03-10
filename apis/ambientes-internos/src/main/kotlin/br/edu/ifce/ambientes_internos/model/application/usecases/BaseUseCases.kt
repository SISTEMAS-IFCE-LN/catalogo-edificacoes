package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.data.domain.Pageable

open class BaseUseCases(val status: StatusAmbiente) {

    fun obterAmbientePorId(id: Long, repoAmb: AmbienteRepository): AmbienteRes {
        val ambiente = repoAmb.findByIdAndStatus(id, status)
            .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
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
        localizacao: String,
        pageable: Pageable,
        repoAmb: AmbienteRepository
    ): AmbientesBasicosPaginadosRes {
        val page =
            repoAmb.findByLocalizacaoContainingIgnoreCaseAndStatus(localizacao, status, pageable)
        return AmbientesBasicosPaginadosRes.from(page)
    }

}