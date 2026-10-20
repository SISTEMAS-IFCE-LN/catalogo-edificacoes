package br.edu.ifce.ambientes_internos.model.repository

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import org.springframework.data.jpa.repository.JpaRepository

interface UnidadeRepository : JpaRepository<Unidade, Long> {
    fun findByCampusAndNome(campus: Campus, nome: String): Unidade?
    fun findByCampus(campus: Campus): List<Unidade>
    fun findByIdAndCampusId(id: Long, campusId: Long): Unidade?
}
