package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbientePublicadoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteBasicoRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteNomeLocalizacaoRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasAmbientesPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AmbientePublicadoUseCases(val repoAmb: AmbienteRepository) : IAmbientePublicadoUseCases {

    @Transactional(readOnly = true)
    override fun obterAmbientePorId(id: Long): AmbienteRes {
        val ambiente = repoAmb.findByIdAndStatus(id, StatusAmbiente.PUBLICADO)
            .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
        return AmbienteRes.from(ambiente)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientes(pageable: Pageable): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findAllByStatus(StatusAmbiente.PUBLICADO, pageable)
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

    @Transactional(readOnly = true)
    override fun listarAmbientesPorTipo(
        tipo: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findByTipoAndStatus(tipo, StatusAmbiente.PUBLICADO, pageable)
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

    @Transactional(readOnly = true)
    override fun listarAmbientesPorNome(
        nome: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        val page = repoAmb.findByNomeContainingIgnoreCaseAndStatus(nome, StatusAmbiente.PUBLICADO, pageable)
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

    @Transactional(readOnly = true)
    override fun listarAmbientesPorLocalizacao(
        localizacao: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        val page =
            repoAmb.findByLocalizacaoContainingIgnoreCaseAndStatus(localizacao, StatusAmbiente.PUBLICADO, pageable)
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

    @Transactional(readOnly = true)
    override fun listarEsquadriasAmbientes(
        ids: Set<Long>,
        pageable: Pageable
    ): EsquadriasAmbientesPaginadosRes {
        val page = repoAmb.findAllByIdInAndStatus(ids, StatusAmbiente.PUBLICADO, pageable)
        val ambientes = page.content.associate { ambiente ->
            AmbienteNomeLocalizacaoRes(
                id = ambiente.id!!,
                nome = ambiente.nome,
                localizacao = LocalizacaoRes.from(ambiente.localizacao)
            ) to EsquadriasDetalhesRes.from(ambiente)
        }
        return EsquadriasAmbientesPaginadosRes(
            ambientes = ambientes,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            pageSize = page.size,
            hasNext = page.hasNext(),
            hasPrevious = page.hasPrevious()
        )
    }

}