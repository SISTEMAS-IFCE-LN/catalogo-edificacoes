package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IBlocoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.dto.ambiente.BlocoRes
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlocoUseCases(
    private val repoBloco: BlocoRepository,
    private val repoUnidade: UnidadeRepository
) : IBlocoUseCases {

    @Transactional
    override fun resolverOuCriar(unidade: Unidade, nomeDigitado: String): Bloco {
        val nome = Bloco.normalizar(nomeDigitado)
        require(nome.isNotBlank()) { "O nome do bloco é inválido." }

        repoBloco.findByUnidadeAndNome(unidade, nome)?.let { return it }

        return try {
            repoBloco.saveAndFlush(Bloco(unidade, nome))
        } catch (ex: DataIntegrityViolationException) {
            // corrida: outra transação criou o mesmo (unidade, nome) entre o find e o save
            repoBloco.findByUnidadeAndNome(unidade, nome) ?: throw ex
        }
    }

    @Transactional(readOnly = true)
    override fun obterPorIdEValidarUnidade(blocoId: Long, unidadeId: Long): Bloco {
        if (!repoUnidade.existsById(unidadeId)) {
            throw IllegalArgumentException("Unidade inválida: $unidadeId.")
        }
        return repoBloco.findByIdAndUnidadeId(blocoId, unidadeId)
            ?: throw IllegalArgumentException("O bloco não pertence à unidade informada.")
    }

    @Transactional(readOnly = true)
    override fun listarPorUnidade(unidadeId: Long): List<BlocoRes> {
        val unidade = repoUnidade.findById(unidadeId)
            .orElseThrow { IllegalArgumentException("Unidade inválida: $unidadeId.") }
        return repoBloco.findByUnidade(unidade)
            .sortedWith(COMPARADOR_NATURAL)
            .map(BlocoRes::from)
    }

    companion object {
        val COMPARADOR_NATURAL = Comparator<Bloco> { a, b ->
            ComparadorNatural.comparar(a.nome, b.nome)
        }
    }

}
