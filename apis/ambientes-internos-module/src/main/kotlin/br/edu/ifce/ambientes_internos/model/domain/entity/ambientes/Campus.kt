package br.edu.ifce.ambientes_internos.model.domain.entity.ambientes

import br.edu.ifce.ambientes_internos.model.domain.NormalizadorNome
import jakarta.persistence.*

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "uk_campus_nome", columnNames = ["nome"])
    ]
)
class Campus(

    nomeInicial: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) {

    @Column(nullable = false, length = 100)
    var nome: String = NormalizadorNome.normalizar(nomeInicial)
        set(value) {
            field = NormalizadorNome.normalizar(value)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Campus) return false
        return nome == other.nome
    }

    override fun hashCode(): Int = nome.hashCode()
}
