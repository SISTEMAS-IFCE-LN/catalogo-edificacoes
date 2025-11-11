package br.edu.ifce.ambientes_internos.model.use_cases

import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteNomeLocalizacaoRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosRes
import br.edu.ifce.ambientes_internos.model.dto.esquadria.EsquadriasDetalhesRes

interface AmbientePublicadoUseCases: AmbienteUseCases<AmbientesBasicosRes, AmbienteRes> {

    fun listarEsquadriasAmbientes(ids: Set<Long>): Pair<Set<AmbienteNomeLocalizacaoRes>, Set<EsquadriasDetalhesRes>>

}