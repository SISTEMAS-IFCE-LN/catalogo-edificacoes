package br.edu.ifce.ambientes_internos.model.repository

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import org.springframework.data.jpa.repository.JpaRepository

interface BlocoRepository : JpaRepository<Bloco, Long> {
    fun findByUnidadeAndNome(unidade: Unidade, nome: String): Bloco?
    fun findByUnidade(unidade: Unidade): List<Bloco>
    fun findByIdAndUnidadeId(id: Long, unidadeId: Long): Bloco?
}
