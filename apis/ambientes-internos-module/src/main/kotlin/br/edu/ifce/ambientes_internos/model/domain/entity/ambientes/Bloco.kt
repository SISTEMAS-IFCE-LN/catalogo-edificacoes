package br.edu.ifce.ambientes_internos.model.domain.entity.ambientes

import br.edu.ifce.ambientes_internos.model.domain.NormalizadorNome
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import jakarta.persistence.*

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
        if (other !is Bloco) return false

        return unidade == other.unidade && nome == other.nome
    }

    override fun hashCode(): Int = 31 * unidade.hashCode() + nome.hashCode()

    companion object {
        fun normalizar(valor: String): String = NormalizadorNome.normalizar(valor)
    }

}