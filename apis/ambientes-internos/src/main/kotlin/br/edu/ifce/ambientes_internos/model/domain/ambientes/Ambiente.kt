package br.edu.ifce.ambientes_internos.model.domain.ambientes

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
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

    @Transient
    val geometrias: MutableSet<Geometria>,

    @ElementCollection(fetch = FetchType.EAGER)
    val pesDireitos: MutableSet<BigDecimal>,

    @Transient
    val esquadrias: MutableSet<Esquadria>,

    @Column(length = 255)
    var informacaoAdicional: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StatusAmbiente
) {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, insertable = false, updatable = false)
    var tipo: TipoAmbiente? = null

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

    override fun toString(): String {
        return "Ambiente(id=$id, nome='$nome', localizacao=$localizacao, capacidade=$capacidade, pesDireitos=$pesDireitos informacaoAdicional='$informacaoAdicional', status=$status, tipo=$tipo)"
    }

}