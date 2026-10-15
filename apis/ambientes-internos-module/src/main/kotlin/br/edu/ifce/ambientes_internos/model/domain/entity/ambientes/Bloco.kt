package br.edu.ifce.ambientes_internos.model.domain.entity.ambientes

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import jakarta.persistence.*
import java.text.Normalizer
import java.util.*

@Entity
@Table(uniqueConstraints = [UniqueConstraint(name = "uk_bloco_unidade_nome", columnNames = ["unidade", "nome"])])
class Bloco(

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var unidade: Unidade,

    nomeInicial: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) {

    @Column(nullable = false, length = 50)
    var nome: String = normalizar(nomeInicial)
        set(value) {
            field = normalizar(value)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bloco

        if (id != other.id) return false
        if (unidade != other.unidade) return false
        if (nome != other.nome) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + unidade.hashCode()
        result = 31 * result + nome.hashCode()
        return result
    }

    companion object {
        private val MARCAS = Regex("\\p{Mn}+")
        private val SEPARADORES = Regex("[^\\p{L}\\p{Nd}]+")
        private val DIGITOS = Regex("\\d+")
        private val ESPACOS = Regex("\\s+")

        fun normalizar(valor: String): String = valor
            .trim()
            .let { Normalizer.normalize(it, Normalizer.Form.NFD) }
            .replace(MARCAS, "")
            .uppercase(Locale.ROOT)
            .replace(SEPARADORES, " ")
            .replace(DIGITOS) { m -> m.value.trimStart('0').ifEmpty { "0" } }
            .replace(ESPACOS, " ")
            .trim()
    }

}