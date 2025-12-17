package br.edu.ifce.ambientes_internos.model.domain.entity.ambientes

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.ElementCollection
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.math.BigDecimal
import kotlin.plus

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
abstract class Ambiente(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column(nullable = false, length = 50)
    var nome: String,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "localizacao_id", nullable = false)
    var localizacao: Localizacao,

    @Column(nullable = false)
    var capacidade: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, insertable = false, updatable = false)
    var tipo: TipoAmbiente?,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "ambiente_id")
    val geometrias: MutableSet<Geometria>,

    @ElementCollection(fetch = FetchType.EAGER)
    val pesDireitos: MutableSet<BigDecimal>,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "ambiente_id")
    val esquadrias: MutableSet<Esquadria>,

    @Column(length = 255)
    var informacaoAdicional: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StatusAmbiente,
) {

    fun calcularAreaAmbienteM2(): BigDecimal {
        return geometrias
            .map { it.calcularAreaTotalM2() }
            .fold(BigDecimal.ZERO) { acc, area -> acc + area }
    }

    fun calcularAreaEsquadriasPorTipoMaterial(): Map<Pair<TipoEsquadria, MaterialEsquadria>, BigDecimal> {
        return esquadrias
            .groupBy { Pair(it.tipo, it.material) }
            .mapValues { entry ->
                entry.value
                    .map { it.geometria.calcularAreaTotalM2() }
                    .fold(BigDecimal.ZERO) { acc, area -> acc + area }
            }
    }

    fun calcularAreaEsquadriasPorTipoMaterial(tipo: TipoEsquadria, material: MaterialEsquadria): BigDecimal {
        return esquadrias
            .filter { it.tipo == tipo && it.material == material }
            .map { it.geometria.calcularAreaTotalM2() }
            .fold(BigDecimal.ZERO) { acc, area -> acc + area }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false

        other as Ambiente

        if (nome != other.nome) return false
        if (localizacao != other.localizacao) return false
        if (capacidade != other.capacidade) return false
        if (tipo != other.tipo) return false
        if (geometrias.size != other.geometrias.size) return false
        if (!geometrias.containsAll(other.geometrias)) return false
        if (pesDireitos.size != other.pesDireitos.size) return false
        if (!pesDireitos.containsAll(other.pesDireitos)) return false
        if (esquadrias.size != other.esquadrias.size) return false
        if (!esquadrias.containsAll(other.esquadrias)) return false
        if (informacaoAdicional != other.informacaoAdicional) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nome.hashCode()
        result = 31 * result + localizacao.hashCode()
        result = 31 * result + capacidade
        result = 31 * result + (tipo?.hashCode() ?: 0)
        result = 31 * result + geometrias.hashCode()
        result = 31 * result + pesDireitos.hashCode()
        result = 31 * result + esquadrias.hashCode()
        result = 31 * result + informacaoAdicional.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }

}