package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IBlocoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.dto.ambiente.BlocoRes
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

    @Transactional(readOnly = true)
    override fun listarPorUnidade(unidade: String): List<BlocoRes> {
        val resolvida = Unidade.entries.firstOrNull {
            it.name.equals(unidade, ignoreCase = true) || it.nome.equals(unidade, ignoreCase = true)
        } ?: throw IllegalArgumentException("Unidade inválida: $unidade.")
        return repoBloco.findByUnidade(resolvida)
            .sortedWith(COMPARADOR_NATURAL)
            .map(BlocoRes::from)
    }

    companion object {
        private val TOKENS = Regex("\\d+|\\D+")

        // Ordem natural: "BLOCO 2" < "BLOCO 2A" < "BLOCO 10" < "CASA".
        // `ORDER BY nome` é lexicográfico ("BLOCO 10" antes de "BLOCO 2"), por isso a
        // ordenação acontece em memória e não na consulta.
        val COMPARADOR_NATURAL = Comparator<Bloco> { a, b -> compararNatural(a.nome, b.nome) }

        private fun compararNatural(a: String, b: String): Int {
            val tokensA = TOKENS.findAll(a).map { it.value }.toList()
            val tokensB = TOKENS.findAll(b).map { it.value }.toList()

            for (i in 0 until minOf(tokensA.size, tokensB.size)) {
                val comparacao = compararToken(tokensA[i], tokensB[i])
                if (comparacao != 0) return comparacao
            }
            return tokensA.size - tokensB.size
        }

        private fun compararToken(a: String, b: String): Int =
            if (a.first().isDigit() && b.first().isDigit()) compararNumerico(a, b)
            else a.compareTo(b)

        private fun compararNumerico(a: String, b: String): Int =
            if (a.length != b.length) a.length - b.length
            else a.compareTo(b)
    }

}