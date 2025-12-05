package br.edu.ifce.ambientes_internos.model.domain.ambientes

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Unidade
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Localizacao(
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
) {}