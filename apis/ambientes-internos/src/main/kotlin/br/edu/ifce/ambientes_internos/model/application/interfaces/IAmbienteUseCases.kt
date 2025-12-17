package br.edu.ifce.ambientes_internos.model.application.interfaces

interface IAmbienteUseCases<RES1, RES2> {

    fun listarAmbientes(): RES1

    fun listarAmbientesPorTipo(tipo: String): RES1

    fun listarAmbientesPorNome(nome: String): RES1

    fun listarAmbientesPorLocalizacao(localizacao: String): RES1

    fun obterAmbientePorId(id: Long): RES2

}