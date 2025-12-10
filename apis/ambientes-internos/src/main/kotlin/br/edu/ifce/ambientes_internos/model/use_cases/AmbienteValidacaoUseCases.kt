package br.edu.ifce.ambientes_internos.model.use_cases

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosRes

interface AmbienteValidacaoUseCases: AmbienteUseCases<AmbientesBasicosRes, AmbienteRes> {

    fun publicarAmbiente(id: Long): StatusAmbiente

    fun privarAmbiente(id: Long): StatusAmbiente

}