package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.dto.ambiente.PaginatedAmbientesBasicosRes
import org.springframework.data.domain.Pageable

interface IAmbienteUseCases<RES2> {

    fun listarAmbientes(pageable: Pageable): PaginatedAmbientesBasicosRes

    fun listarAmbientesPorTipo(tipo: String, pageable: Pageable): PaginatedAmbientesBasicosRes

    fun listarAmbientesPorNome(nome: String, pageable: Pageable): PaginatedAmbientesBasicosRes

    fun listarAmbientesPorLocalizacao(localizacao: String, pageable: Pageable): PaginatedAmbientesBasicosRes

    fun obterAmbientePorId(id: Long): RES2

}