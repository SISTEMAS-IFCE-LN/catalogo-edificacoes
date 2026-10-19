package br.edu.ifce.ambientes_internos.model.domain.entity.ambientes

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(name = "uk_localizacao_bloco_andar", columnNames = ["bloco_id", "andar"]) ]
)
class Localizacao(

    @ManyToOne(optional = false)
    @JoinColumn(name = "bloco_id", nullable = false)
    var bloco: Bloco,

    @Column(nullable = false)
    @ColumnDefault("0")
    var andar: Int = 0,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Localizacao

        if (andar != other.andar) return false
        if (bloco != other.bloco) return false

        return true
    }

    override fun hashCode(): Int {
        var result = andar
        result = 31 * result + bloco.hashCode()
        return result
    }
}