package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteNomeLocalizacaoRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes

interface IAmbientePublicadoUseCases: IAmbienteUseCases<AmbienteRes> {

    fun listarEsquadriasAmbientes(ids: Set<Long>): Pair<Set<AmbienteNomeLocalizacaoRes>, Set<EsquadriasDetalhesRes>>

}