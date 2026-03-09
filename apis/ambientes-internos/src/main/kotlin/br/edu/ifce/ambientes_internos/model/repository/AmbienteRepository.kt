package br.edu.ifce.ambientes_internos.model.repository

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface AmbienteRepository : JpaRepository<Ambiente, Long> {

    fun findByIdAndStatus(id: Long, status: StatusAmbiente): Optional<Ambiente>

    fun findAllByIdInAndStatus(ids: Set<Long>, status: StatusAmbiente): List<Ambiente>

    fun findAllByIdInAndStatus(ids: Set<Long>, status: StatusAmbiente, pageable: Pageable): Page<Ambiente>

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

    fun findAllByStatus(status: StatusAmbiente, pageable: Pageable): Page<Ambiente>

    fun findByNomeContainingIgnoreCaseAndStatus(nome: String, status: StatusAmbiente, pageable: Pageable): Page<Ambiente>

    @Query(
        "select a from Ambiente a where a.status = :status and " +
                "(cast(a.localizacao.bloco as string) like concat('%', :localizacao, '%') or " +
                "cast(a.localizacao.unidade as string) like concat('%', :localizacao, '%'))"
    )
    fun findByLocalizacaoContainingIgnoreCaseAndStatus(
        @Param("localizacao") localizacao: String,
        @Param("status") status: StatusAmbiente,
        pageable: Pageable
    ): Page<Ambiente>

    fun findByTipoAndStatus(tipo: String, status: StatusAmbiente, pageable: Pageable): Page<Ambiente>

}