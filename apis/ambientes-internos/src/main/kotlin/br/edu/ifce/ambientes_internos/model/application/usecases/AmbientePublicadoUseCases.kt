package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbientePublicadoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasAmbientesPaginadosRes
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AmbientePublicadoUseCases(
    repoAmb: AmbienteRepository
) : IAmbientePublicadoUseCases, BaseUseCases(StatusAmbiente.PUBLICADO, repoAmb) {

    @Transactional(readOnly = true)
    override fun listarEsquadriasAmbientes(
        ids: Set<Long>,
        pageable: Pageable
    ): EsquadriasAmbientesPaginadosRes {
        val pageableLimitado = limitarPageable(pageable)
        val page = repoAmb.findAllByIdInAndStatus(ids, status, pageableLimitado)
        return EsquadriasAmbientesPaginadosRes.from(page)
    }

}