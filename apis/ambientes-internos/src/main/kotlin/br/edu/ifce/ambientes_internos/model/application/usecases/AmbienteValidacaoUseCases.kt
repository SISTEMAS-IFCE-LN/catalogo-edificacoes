package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteValidacaoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AmbienteValidacaoUseCases(
    repoAmb: AmbienteRepository
) : IAmbienteValidacaoUseCases, BaseUseCases(StatusAmbiente.AGUARDANDO_VALIDACAO, repoAmb) {

    @Transactional
    override fun publicarAmbiente(id: Long) {
        val ambiente = obterAmbiente(id)
        ambiente.status = StatusAmbiente.PUBLICADO
        repoAmb.save(ambiente)
    }

    @Transactional
    override fun privarAmbiente(id: Long) {
        val ambiente = repoAmb.findByIdAndStatus(id, StatusAmbiente.AGUARDANDO_VALIDACAO)
            .orElseGet {
                repoAmb.findByIdAndStatus(id, StatusAmbiente.PUBLICADO)
                    .orElseThrow { NoSuchElementException("Ambiente não encontrado") }
            }
        ambiente.status = StatusAmbiente.NAO_PUBLICADO
        repoAmb.save(ambiente)
    }

}