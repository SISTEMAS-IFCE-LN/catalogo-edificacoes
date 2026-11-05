package br.edu.ifce.ambientes_internos.model.repository

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BlocoRepository : JpaRepository<Bloco, Long> {
    fun findByUnidadeAndNome(unidade: Unidade, nome: String): Bloco?
    fun findByUnidade(unidade: Unidade): List<Bloco>
    fun findByIdAndUnidadeId(id: Long, unidadeId: Long): Bloco?

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query(
        """
            delete from Bloco b
            where b.id = :id
            and not exists (
                select 1 from Localizacao l
                where l.bloco.id = :id
            )
        """
    )
    fun deleteIfOrphan(@Param("id") id: Long)
}
