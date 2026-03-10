package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteBasicoRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

open class BaseUseCases(val status: StatusAmbiente) {

    fun obterAmbientePorId(id: Long, repoAmb: AmbienteRepository): AmbienteRes {
        val ambiente = repoAmb.findByIdAndStatus(id, status)
            .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        return AmbienteRes.from(ambiente)
    }

    fun listarAmbientes(pageable: Pageable, repoAmb: AmbienteRepository): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findAllByStatus(status, pageable)
        val ambientesBasicos = page.content.map { AmbienteBasicoRes.from(it) }
        val areaTotal = page.content.fold(BigDecimal.ZERO) { acc, ambiente ->
            acc.add(ambiente.calcularAreaAmbienteM2())
        }
        return AmbientesBasicosPaginadosRes(
            ambientes = ambientesBasicos,
            areaTotal = areaTotal,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            pageSize = page.size,
            hasNext = page.hasNext(),
            hasPrevious = page.hasPrevious()
        )
    }

    fun listarAmbientesPorTipo(
        tipo: String,
        pageable: Pageable,
        repoAmb: AmbienteRepository
    ): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findByTipoAndStatus(tipo, status, pageable)
        val ambientesBasicos = page.content.map { AmbienteBasicoRes.from(it) }
        val areaTotal = page.content.fold(BigDecimal.ZERO) { acc, ambiente ->
            acc.add(ambiente.calcularAreaAmbienteM2())
        }
        return AmbientesBasicosPaginadosRes(
            ambientes = ambientesBasicos,
            areaTotal = areaTotal,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            pageSize = page.size,
            hasNext = page.hasNext(),
            hasPrevious = page.hasPrevious()
        )
    }

    fun listarAmbientesPorNome(
        nome: String,
        pageable: Pageable,
        repoAmb: AmbienteRepository
    ): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findByNomeContainingIgnoreCaseAndStatus(nome, status, pageable)
        val ambientesBasicos = page.content.map { AmbienteBasicoRes.from(it) }
        val areaTotal = page.content.fold(BigDecimal.ZERO) { acc, ambiente ->
            acc.add(ambiente.calcularAreaAmbienteM2())
        }
        return AmbientesBasicosPaginadosRes(
            ambientes = ambientesBasicos,
            areaTotal = areaTotal,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            pageSize = page.size,
            hasNext = page.hasNext(),
            hasPrevious = page.hasPrevious()
        )
    }

    fun listarAmbientesPorLocalizacao(
        localizacao: String,
        pageable: Pageable,
        repoAmb: AmbienteRepository
    ): AmbientesBasicosPaginadosRes {
        val page =
            repoAmb.findByLocalizacaoContainingIgnoreCaseAndStatus(localizacao, status, pageable)
        val ambientesBasicos = page.content.map { AmbienteBasicoRes.from(it) }
        val areaTotal = page.content.fold(BigDecimal.ZERO) { acc, ambiente ->
            acc.add(ambiente.calcularAreaAmbienteM2())
        }
        return AmbientesBasicosPaginadosRes(
            ambientes = ambientesBasicos,
            areaTotal = areaTotal,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            pageSize = page.size,
            hasNext = page.hasNext(),
            hasPrevious = page.hasPrevious()
        )
    }

}