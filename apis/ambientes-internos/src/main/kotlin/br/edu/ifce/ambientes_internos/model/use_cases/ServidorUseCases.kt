package br.edu.ifce.ambientes_internos.model.use_cases

interface ServidorUseCases<RESPONSE> {

    fun listarAmbientesCompletos(): List<RESPONSE>

    fun listarAmbientesCompletosPorLocalizacao(localizacao: String): List<RESPONSE>

    fun obterAmbienteCompletoPorId(id: Long): RESPONSE

    fun obterAmbienteCompletoPorNome(nome: String): RESPONSE

}