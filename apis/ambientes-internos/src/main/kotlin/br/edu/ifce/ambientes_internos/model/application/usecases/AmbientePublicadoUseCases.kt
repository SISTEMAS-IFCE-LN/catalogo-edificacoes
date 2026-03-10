package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbientePublicadoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
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

@Service
class AmbientePublicadoUseCases(val repoAmb: AmbienteRepository) : IAmbientePublicadoUseCases,
    BaseUseCases(StatusAmbiente.PUBLICADO) {

    @Transactional(readOnly = true)
    override fun obterAmbientePorId(id: Long): AmbienteRes {
        return obterAmbientePorId(id, repoAmb)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientes(pageable: Pageable): AmbientesBasicosPaginadosRes {
        return listarAmbientes(pageable, repoAmb)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorTipo(
        tipo: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        return listarAmbientesPorTipo(tipo, pageable, repoAmb)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorNome(
        nome: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        return listarAmbientesPorNome(nome, pageable, repoAmb)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorLocalizacao(
        localizacao: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        return listarAmbientesPorLocalizacao(localizacao, pageable, repoAmb)
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