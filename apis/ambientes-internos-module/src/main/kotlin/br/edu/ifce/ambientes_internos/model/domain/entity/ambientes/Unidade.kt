package br.edu.ifce.ambientes_internos.model.domain.entity.ambientes

import br.edu.ifce.ambientes_internos.model.domain.NormalizadorNome
import jakarta.persistence.*

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_unidade_campus_nome",
            columnNames = ["campus_id", "nome"]
        )
    ]
)
class Unidade(

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    var campus: Campus,

    nomeInicial: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) {

    @Column(nullable = false, length = 50)
    var nome: String = NormalizadorNome.normalizar(nomeInicial)
        set(value) {
            field = NormalizadorNome.normalizar(value)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Unidade) return false
        return campus == other.campus && nome == other.nome
    }

    override fun hashCode(): Int = 31 * campus.hashCode() + nome.hashCode()
}
