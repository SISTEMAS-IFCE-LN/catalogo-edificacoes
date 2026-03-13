package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes

interface IAmbienteValidacaoUseCases: IAmbienteUseCases<AmbienteRes> {

    fun publicarAmbiente(id: Long)

    fun privarAmbiente(id: Long)

}