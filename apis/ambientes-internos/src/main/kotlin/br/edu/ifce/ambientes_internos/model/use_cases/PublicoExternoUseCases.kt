package br.edu.ifce.ambientes_internos.model.use_cases

interface PublicoExternoUseCases<RESPONSE> {

    fun listarAmbientesSimples(): List<RESPONSE>

    fun listarAmbientesSimplesPorLocalizacao(localizacao: String): List<RESPONSE>

    fun obterAmbienteSimplesPorId(id: Long): RESPONSE

    fun obterAmbienteSimplesPorNome(nome: String): RESPONSE

}