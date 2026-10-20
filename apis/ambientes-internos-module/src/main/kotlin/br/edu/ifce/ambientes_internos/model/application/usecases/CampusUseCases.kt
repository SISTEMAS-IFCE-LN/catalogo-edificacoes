package br.edu.ifce.ambientes_internos.model.application.usecases

import br.edu.ifce.ambientes_internos.model.application.interfaces.ICampusUseCases
import br.edu.ifce.ambientes_internos.model.domain.NormalizadorNome
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.dto.ambiente.CampusRes
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CampusUseCases(
    private val repoCampus: CampusRepository
) : ICampusUseCases {

    @Transactional
    override fun resolverOuCriar(nomeDigitado: String): Campus {
        val nome = NormalizadorNome.normalizar(nomeDigitado)
        require(nome.isNotBlank()) { "O nome do campus é inválido." }

        repoCampus.findByNome(nome)?.let { return it }

        return try {
            repoCampus.saveAndFlush(Campus(nome))
        } catch (ex: DataIntegrityViolationException) {
            // corrida: outra transação criou o mesmo nome entre o find e o save
            repoCampus.findByNome(nome) ?: throw ex
        }
    }

    @Transactional(readOnly = true)
    override fun obterPorId(id: Long): Campus = repoCampus.findById(id)
        .orElseThrow { IllegalArgumentException("Campus inválido: $id.") }

    @Transactional(readOnly = true)
    override fun listar(): List<CampusRes> = repoCampus.findAll()
        .sortedWith { a, b -> ComparadorNatural.comparar(a.nome, b.nome) }
        .map(CampusRes::from)
}
