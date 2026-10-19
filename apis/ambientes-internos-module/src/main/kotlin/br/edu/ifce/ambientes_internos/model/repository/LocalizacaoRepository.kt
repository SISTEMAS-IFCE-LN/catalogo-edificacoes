package br.edu.ifce.ambientes_internos.model.repository

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface LocalizacaoRepository : JpaRepository<Localizacao, Long> {

    fun findByBlocoAndAndar(bloco: Bloco, andar: Int): Optional<Localizacao>

    fun findByLocalizacao(localizacao: Localizacao): Optional<Localizacao> {
        return findByBlocoAndAndar(localizacao.bloco, localizacao.andar)
    }

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query(
        """
            delete from Localizacao l
            where l.id = :id
            and not exists (
                select 1 from Ambiente a
                where a.localizacao.id = :id
            )
        """
    )
    fun deleteIfOrphan(@Param("id") id: Long)

}