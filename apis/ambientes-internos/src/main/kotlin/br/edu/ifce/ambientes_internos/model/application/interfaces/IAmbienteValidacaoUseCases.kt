package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosRes

interface IAmbienteValidacaoUseCases: IAmbienteUseCases<AmbientesBasicosRes, AmbienteRes> {

    fun publicarAmbiente(id: Long): StatusAmbiente

    fun privarAmbiente(id: Long): StatusAmbiente

}