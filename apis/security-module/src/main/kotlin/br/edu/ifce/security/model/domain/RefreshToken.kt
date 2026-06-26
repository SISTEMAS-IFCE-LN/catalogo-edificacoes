package br.edu.ifce.security.model.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class RefreshToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val token: String,

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    val usuario: Usuario,

    @Column(nullable = false)
    val expiraEm: LocalDateTime,

    @Column(nullable = false)
    var revogado: Boolean = false,

    @Column(nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()
)