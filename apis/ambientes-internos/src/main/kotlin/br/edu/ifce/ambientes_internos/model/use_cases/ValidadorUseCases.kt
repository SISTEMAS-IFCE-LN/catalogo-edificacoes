package br.edu.ifce.ambientes_internos.model.use_cases

interface ValidadorUseCases<REQUEST, RESPONSE> {

    fun publicarAmbientes(ids: List<Long>)

    fun definirAmbientesNaoPublicos(ids: List<Long>)

    fun atualizarAmbiente(id: Long, request: REQUEST): RESPONSE

    fun deletarAmbiente(id: Long)
}