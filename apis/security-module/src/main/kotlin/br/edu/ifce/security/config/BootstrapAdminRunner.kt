package br.edu.ifce.security.config

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Order(1)
class BootstrapAdminRunner(
    private val usuarioRepository: UsuarioRepository,
    @field:Value($$"${app.bootstrap.admin-email:}")
    private val bootstrapAdminEmail: String,
    @field:Value($$"${app.bootstrap.allow-reactivate:true}")
    private val allowReactivate: Boolean
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments?) {
        if (!allowReactivate) {
            log.warn("O administrador padrão não será cadastrado: app.bootstrap.allow-reactivate=false")
            return
        }

        if (bootstrapAdminEmail.isBlank()) {
            throw IllegalStateException(
                "BOOTSTRAP_ADMIN_EMAIL não configurado: " +
                        "Defina a env var app.bootstrap.admin-email antes de iniciar a aplicação " +
                        "para garantir a existência do administrador padrão."
            )
        }

        val usuario = usuarioRepository.findByEmail(bootstrapAdminEmail)

        if (usuario == null) {
            val novo = Usuario(
                email = bootstrapAdminEmail,
                nome = bootstrapAdminEmail
            ).apply {
                perfis = mutableSetOf(Perfil.ROLE_ADMINISTRADOR, Perfil.ROLE_COLABORADOR)
            }
            usuarioRepository.save(novo)
            log.info("Administrador padrão criado: $bootstrapAdminEmail")
            return
        }

        val jaPossuiAdmin = usuario.perfis.contains(Perfil.ROLE_ADMINISTRADOR)
        val precisaReativar = !usuario.ativo

        if (jaPossuiAdmin && !precisaReativar) {
            return
        }

        if (precisaReativar) {
            log.warn("Reativando administrador padrão desativado: $bootstrapAdminEmail")
            usuario.ativo = true
        }

        if (!jaPossuiAdmin) {
            log.warn("Promovendo usuário a administrador padrão: $bootstrapAdminEmail")
            usuario.perfis = mutableSetOf(Perfil.ROLE_ADMINISTRADOR, Perfil.ROLE_COLABORADOR)
        }

        usuarioRepository.save(usuario)
    }
}
