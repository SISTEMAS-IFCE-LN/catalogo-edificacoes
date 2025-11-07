package br.edu.ifce.ambientes_internos.model.use_cases

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteBasicoRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes

interface AmbienteValidacaoUseCases: AmbienteUseCases<AmbienteBasicoRes, AmbienteRes> {

    fun publicarAmbiente(id: Long): StatusAmbiente

    fun privarAmbiente(id: Long): StatusAmbiente

}