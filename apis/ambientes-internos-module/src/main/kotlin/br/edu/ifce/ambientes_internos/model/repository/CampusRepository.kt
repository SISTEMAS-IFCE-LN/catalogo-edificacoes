package br.edu.ifce.ambientes_internos.model.repository

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import org.springframework.data.jpa.repository.JpaRepository

interface CampusRepository : JpaRepository<Campus, Long> {
    fun findByNome(nome: String): Campus?
}
