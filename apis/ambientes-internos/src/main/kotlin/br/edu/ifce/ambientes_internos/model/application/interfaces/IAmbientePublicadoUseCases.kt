package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasAmbientesPaginadosRes
import org.springframework.data.domain.Pageable

interface IAmbientePublicadoUseCases: IAmbienteUseCases<AmbienteRes> {

    fun listarEsquadriasAmbientes(ids: Set<Long>, pageable: Pageable): EsquadriasAmbientesPaginadosRes

}