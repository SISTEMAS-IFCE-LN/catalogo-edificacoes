package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteValidacaoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbientesBasicosPaginadosRes
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AmbienteValidacaoUseCases(val repoAmb: AmbienteRepository) : IAmbienteValidacaoUseCases,
    BaseUseCases(StatusAmbiente.AGUARDANDO_VALIDACAO) {

    @Transactional(readOnly = true)
    override fun obterAmbientePorId(id: Long): AmbienteRes {
        return obterAmbientePorId(id, repoAmb)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientes(pageable: Pageable): AmbientesBasicosPaginadosRes {
        return listarAmbientes(pageable, repoAmb)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorTipo(
        tipo: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        return listarAmbientesPorTipo(tipo, pageable, repoAmb)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorNome(
        nome: String,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        return listarAmbientesPorNome(nome, pageable, repoAmb)
    }

    @Transactional(readOnly = true)
    override fun listarAmbientesPorLocalizacao(
        localizacao: LocalizacaoPesquisaReq,
        pageable: Pageable
    ): AmbientesBasicosPaginadosRes {
        return listarAmbientesPorLocalizacao(localizacao, pageable, repoAmb)
    }

    @Transactional
    override fun publicarAmbiente(id: Long): StatusAmbiente {
        val ambiente = obterAmbienteMetodos(id, repoAmb)
        ambiente.status = StatusAmbiente.PUBLICADO
        return repoAmb.save(ambiente).status
    }

    @Transactional
    override fun privarAmbiente(id: Long): StatusAmbiente {
        val ambiente = obterAmbienteMetodos(id, repoAmb)
        ambiente.status = StatusAmbiente.NAO_PUBLICADO
        return repoAmb.save(ambiente).status
    }

}