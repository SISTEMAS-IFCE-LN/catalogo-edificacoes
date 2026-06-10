package br.edu.ifce.security.model.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "usuarios")
class Usuario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val nome: String,

    @Column(nullable = false)
    var ativo: Boolean = true,

    @Column(nullable = false, unique = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_perfis", joinColumns = [JoinColumn(name = "usuario_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil")
    var perfis: MutableSet<Perfil> = mutableSetOf()
)
