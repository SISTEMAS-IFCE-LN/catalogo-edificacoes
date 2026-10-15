package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IBlocoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlocoUseCases(
    private val repoBloco: BlocoRepository
) : IBlocoUseCases {

    @Transactional
    override fun resolverOuCriar(unidade: Unidade, nomeDigitado: String): Bloco {
        val nome = Bloco.normalizar(nomeDigitado)
        require(nome.isNotBlank()) { "O nome do bloco é invalido." }

        repoBloco.findByUnidadeAndNome(unidade, nome)?.let { return it }

        return try {
            repoBloco.saveAndFlush(Bloco(unidade, nome))
        } catch (ex: DataIntegrityViolationException) {
            // corrida: outra transação criou o mesmo (unidade, nome) entre o find e o save
            repoBloco.findByUnidadeAndNome(unidade, nome) ?: throw ex
        }
    }

}