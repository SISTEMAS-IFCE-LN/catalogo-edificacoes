package br.edu.ifce.ambientes_internos.model.use_cases

interface GestorSistemaUseCases<REQUEST, RESPONSE> {

    fun listarAmbientesNaoPublicados(): List<RESPONSE>

    fun obterAmbienteNaoPublicadoPorLocalizacao(localizacao: String): List<RESPONSE>

    fun obterAmbienteNaoPublicadoPorId(id: Long): RESPONSE

    fun obterAmbienteNaoPublicadoPorNome(nome: String): RESPONSE

    fun cadastrarAmbienteNaoPublicado(request: REQUEST): RESPONSE

    fun atualizarAmbienteNaoPublicado(id: Long, request: REQUEST): RESPONSE

    fun deletarAmbienteNaoPublicado(id: Long)

    fun enviarAmbientesParaPublicacao(ids: List<Long>)

}