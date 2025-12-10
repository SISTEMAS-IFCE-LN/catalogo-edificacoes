package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
abstract class Esquadria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, insertable = false, updatable = false)
    var tipo: TipoEsquadria,

    @Transient
    var geometria: Geometria,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var material: MaterialEsquadria,

    @Column(length = 255)
    var informacaoAdicional: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Esquadria

        if (tipo != other.tipo) return false
        if (geometria != other.geometria) return false
        if (material != other.material) return false
        if (informacaoAdicional != other.informacaoAdicional) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tipo.hashCode()
//        result = 31 * result + geometria.hashCode()
        result = 31 * result + material.hashCode()
        result = 31 * result + informacaoAdicional.hashCode()
        return result
    }

    override fun toString(): String {
        return "Esquadria(id=$id, tipo=$tipo, material=$material, informacaoAdicional='$informacaoAdicional')"
    }

}