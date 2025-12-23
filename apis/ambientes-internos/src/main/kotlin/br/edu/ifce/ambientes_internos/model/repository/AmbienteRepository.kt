package br.edu.ifce.ambientes_internos.model.repository

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface AmbienteRepository : JpaRepository<Ambiente, Long> {

    fun findByIdAndStatus(id: Long, status: StatusAmbiente): Optional<Ambiente>

    fun existsByNomeAndLocalizacaoId(nome: String, localizacaoId: Long): Boolean

    @Query(
        "select case when (count(a) > 0) then true else false end " +
                "from Ambiente a " +
                "where a.nome = :nome and a.localizacao.id = :localizacaoId and a.id <> :id"
    )
    fun existsByNomeAndLocalizacaoIdAndIdNot(
        @Param("nome") nome: String,
        @Param("localizacaoId") localizacaoId: Long,
        @Param("id") id: Long
    ): Boolean

}