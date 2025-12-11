package br.edu.ifce.ambientes_internos.model.domain.ambientes

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Unidade
import jakarta.persistence.*

@Entity
class Localizacao(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var bloco: Bloco,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var unidade: Unidade,

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
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
        if (unidade != other.unidade) return false

        return true
    }

    override fun hashCode(): Int {
        var result = andar
        result = 31 * result + bloco.hashCode()
        result = 31 * result + unidade.hashCode()
        return result
    }
}