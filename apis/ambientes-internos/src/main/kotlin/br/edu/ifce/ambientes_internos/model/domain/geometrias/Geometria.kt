package br.edu.ifce.ambientes_internos.model.domain.geometrias

import br.edu.ifce.ambientes_internos.model.domain.geometrias.enums.TipoGeometria
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import java.math.BigDecimal
import java.math.RoundingMode

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
abstract class Geometria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, insertable = false, updatable = false)
    var tipo: TipoGeometria,

    @Column(nullable = false, columnDefinition = "DECIMAL(9, 2) DEFAULT 0.00")
    var base: BigDecimal,

    @Column(nullable = false, columnDefinition = "DECIMAL(9, 2) DEFAULT 0.00")
    var altura: BigDecimal,

    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    var repeticao: Int
) {

    abstract fun calcularAreaM2(): BigDecimal

    fun calcularAreaTotalM2(): BigDecimal {
        return calcularAreaM2()
            .multiply(BigDecimal(repeticao))
            .setScale(2, RoundingMode.HALF_UP)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Geometria

        if (repeticao != other.repeticao) return false
        if (base != other.base) return false
        if (altura != other.altura) return false

        return true
    }

    override fun hashCode(): Int {
        var result = repeticao
        result = 31 * result + base.hashCode()
        result = 31 * result + altura.hashCode()
        return result
    }

    override fun toString(): String {
        return "Geometria(id=$id, tipo=$tipo, base=$base, altura=$altura, repeticao=$repeticao)"
    }

}