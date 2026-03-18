package br.edu.ifce.ambientes_internos.model.application.interfaces

import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import org.springframework.data.domain.Pageable

interface IAmbienteUseCases<RES> {

    fun listarAmbientes(pageable: Pageable): AmbientesBasicosPaginadosRes

    fun listarAmbientesPorTipo(tipo: String, pageable: Pageable): AmbientesBasicosPaginadosRes

    fun listarAmbientesPorNome(nome: String, pageable: Pageable): AmbientesBasicosPaginadosRes

    fun listarAmbientesPorLocalizacao(localizacao: LocalizacaoPesquisaReq, pageable: Pageable): AmbientesBasicosPaginadosRes

    fun obterAmbientePorId(id: Long): RES

}