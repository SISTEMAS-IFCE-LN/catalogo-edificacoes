package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.IUnidadeUseCases
import br.edu.ifce.ambientes_internos.model.domain.NormalizadorNome
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.dto.ambiente.UnidadeRes
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UnidadeUseCases(
    private val repoUnidade: UnidadeRepository,
    private val repoCampus: CampusRepository
) : IUnidadeUseCases {

    @Transactional
    override fun resolverOuCriar(campus: Campus, nomeDigitado: String): Unidade {
        val nome = NormalizadorNome.normalizar(nomeDigitado)
        require(nome.isNotBlank()) { "O nome da unidade é inválido." }

        repoUnidade.findByCampusAndNome(campus, nome)?.let { return it }

        return try {
            repoUnidade.saveAndFlush(Unidade(campus, nome))
        } catch (ex: DataIntegrityViolationException) {
            // corrida: outra transação criou o mesmo (campus, nome) entre o find e o save
            repoUnidade.findByCampusAndNome(campus, nome) ?: throw ex
        }
    }

    @Transactional(readOnly = true)
    override fun obterPorIdEValidarCampus(unidadeId: Long, campusId: Long): Unidade {
        if (!repoCampus.existsById(campusId)) {
            throw IllegalArgumentException("Campus inválido: $campusId.")
        }

        repoUnidade.findByIdAndCampusId(unidadeId, campusId)?.let { return it }

        if (repoUnidade.existsById(unidadeId)) {
            throw IllegalArgumentException("A unidade não pertence ao campus informado.")
        }
        throw IllegalArgumentException("Unidade inválida: $unidadeId.")
    }

    @Transactional(readOnly = true)
    override fun listarPorCampus(campusId: Long): List<UnidadeRes> {
        val campus = repoCampus.findById(campusId)
            .orElseThrow { IllegalArgumentException("Campus inválido: $campusId.") }
        return repoUnidade.findByCampus(campus)
            .sortedWith { a, b -> ComparadorNatural.comparar(a.nome, b.nome) }
            .map(UnidadeRes::from)
    }
}
