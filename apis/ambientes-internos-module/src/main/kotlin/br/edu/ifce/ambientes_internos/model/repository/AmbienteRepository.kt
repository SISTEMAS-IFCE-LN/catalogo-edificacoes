package br.edu.ifce.ambientes_internos.model.repository

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface AmbienteRepository : JpaRepository<Ambiente, Long> {

    @Query(
        """
            select a from Ambiente a
            left join fetch a.localizacao
            left join fetch a.localizacao.bloco
            left join fetch a.localizacao.bloco.unidade
            left join fetch a.localizacao.bloco.unidade.campus
            left join fetch a.geometrias
            left join fetch a.pesDireitos
            left join fetch a.esquadrias
            where a.id = :id and a.status = :status
        """
    )
    fun findByIdAndStatus(id: Long, status: StatusAmbiente): Optional<Ambiente>

    @EntityGraph(
        attributePaths = [
            "localizacao",
            "localizacao.bloco",
            "localizacao.bloco.unidade",
            "localizacao.bloco.unidade.campus"
        ]
    )
    fun findAllByIdInAndStatus(ids: Set<Long>, status: StatusAmbiente): List<Ambiente>

    @EntityGraph(
        attributePaths = [
            "localizacao",
            "localizacao.bloco",
            "localizacao.bloco.unidade",
            "localizacao.bloco.unidade.campus"
        ]
    )
    fun findAllByIdInAndStatus(ids: Set<Long>, status: StatusAmbiente, pageable: Pageable): Page<Ambiente>

    fun existsByNomeAndLocalizacaoId(nome: String, localizacaoId: Long): Boolean

    @Query(
        """
            select case when (count(a) > 0) then true else false end from Ambiente a
            where a.nome = :nome and a.localizacao.id = :localizacaoId and a.id <> :id
        """
    )
    fun existsByNomeAndLocalizacaoIdAndIdNot(
        @Param("nome") nome: String,
        @Param("localizacaoId") localizacaoId: Long,
        @Param("id") id: Long
    ): Boolean

    @EntityGraph(
        attributePaths = [
            "localizacao",
            "localizacao.bloco",
            "localizacao.bloco.unidade",
            "localizacao.bloco.unidade.campus"
        ]
    )
    fun findByStatus(
        @Param("status") status: StatusAmbiente,
        pageable: Pageable
    ): Page<Ambiente>

    @EntityGraph(
        attributePaths = [
            "localizacao",
            "localizacao.bloco",
            "localizacao.bloco.unidade",
            "localizacao.bloco.unidade.campus"
        ]
    )
    @Query(
        """
            select a from Ambiente a
            where a.status = :status
            and upper(a.nome) like concat('%', upper(:nome), '%')
        """
    )
    fun findByNomeContainingIgnoreCaseAndStatus(
        nome: String,
        status: StatusAmbiente,
        pageable: Pageable
    ): Page<Ambiente>

    @EntityGraph(
        attributePaths = [
            "localizacao",
            "localizacao.bloco",
            "localizacao.bloco.unidade",
            "localizacao.bloco.unidade.campus"
        ]
    )
    @Query(
        """
            select a from Ambiente a
            where a.status = :status
            and (:filtrarCampus = false or a.localizacao.bloco.unidade.campus.id = :campusId)
            and (:filtrarUnidade = false or a.localizacao.bloco.unidade.id = :unidadeId)
            and (:filtrarBloco = false or a.localizacao.bloco.id = :blocoId)
            and (:andar is null or a.localizacao.andar = :andar)
        """
    )
    fun findByLocalizacaoAndStatus(
        @Param("campusId") campusId: Long?,
        @Param("unidadeId") unidadeId: Long?,
        @Param("blocoId") blocoId: Long?,
        @Param("filtrarCampus") filtrarCampus: Boolean,
        @Param("filtrarUnidade") filtrarUnidade: Boolean,
        @Param("filtrarBloco") filtrarBloco: Boolean,
        @Param("andar") andar: Int?,
        @Param("status") status: StatusAmbiente,
        pageable: Pageable
    ): Page<Ambiente>

    @EntityGraph(
        attributePaths = [
            "localizacao",
            "localizacao.bloco",
            "localizacao.bloco.unidade",
            "localizacao.bloco.unidade.campus"
        ]
    )
    @Query(
        """
            select a from Ambiente a
            where a.status = :status
            and a.tipo in :tipos
        """
    )
    fun findByTiposAndStatus(
        @Param("tipos") tipos: Set<TipoAmbiente>,
        @Param("status") status: StatusAmbiente,
        pageable: Pageable
    ): Page<Ambiente>

}