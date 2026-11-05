package br.edu.ifce.ambientes_internos.model.repository

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CampusRepository : JpaRepository<Campus, Long> {
    fun findByNome(nome: String): Campus?

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query(
        """
            delete from Campus c
            where c.id = :id
            and not exists (
                select 1 from Unidade u
                where u.campus.id = :id
            )
        """
    )
    fun deleteIfOrphan(@Param("id") id: Long)
}
