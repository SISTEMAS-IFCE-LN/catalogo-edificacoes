package br.edu.ifce.ambientes_internos.model.repository

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UnidadeRepository : JpaRepository<Unidade, Long> {
    fun findByCampusAndNome(campus: Campus, nome: String): Unidade?
    fun findByCampus(campus: Campus): List<Unidade>
    fun findByIdAndCampusId(id: Long, campusId: Long): Unidade?

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query(
        """
            delete from Unidade u
            where u.id = :id
            and not exists (
                select 1 from Bloco b
                where b.unidade.id = :id
            )
        """
    )
    fun deleteIfOrphan(@Param("id") id: Long)
}
