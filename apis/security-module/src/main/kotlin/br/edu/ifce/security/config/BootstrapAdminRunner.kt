package br.edu.ifce.security.config

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Order(1)
class BootstrapAdminRunner(
    private val usuarioRepository: UsuarioRepository,
    private val bootstrapProperties: BootstrapProperties
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments?) {
        if (!bootstrapProperties.allowReactivate) {
            log.warn("O administrador padrão não será cadastrado: bootstrap.allow-reactivate=false")
            return
        }

        if (bootstrapProperties.adminEmail.isBlank()) {
            throw IllegalStateException(
                "BOOTSTRAP_ADMIN_EMAIL não configurado: " +
                        "Defina a env var BOOTSTRAP_ADMIN_EMAIL (propriedade bootstrap.admin-email) " +
                        "antes de iniciar a aplicação para garantir a existência do administrador padrão."
            )
        }

        val usuario = usuarioRepository.findByEmail(bootstrapProperties.adminEmail)

        if (usuario == null) {
            val novo = Usuario(
                email = bootstrapProperties.adminEmail,
                nome = bootstrapProperties.adminEmail
            ).apply {
                perfis = mutableSetOf(Perfil.ROLE_ADMINISTRADOR, Perfil.ROLE_COLABORADOR)
            }
            usuarioRepository.save(novo)
            log.info("Administrador padrão criado: ${bootstrapProperties.adminEmail}")
            return
        }

        val jaPossuiAdmin = usuario.perfis.contains(Perfil.ROLE_ADMINISTRADOR)
        val precisaReativar = !usuario.ativo

        if (jaPossuiAdmin && !precisaReativar) {
            return
        }

        if (precisaReativar) {
            log.warn("Reativando administrador padrão desativado: ${bootstrapProperties.adminEmail}")
            usuario.ativo = true
        }

        if (!jaPossuiAdmin) {
            log.warn("Promovendo usuário a administrador padrão: ${bootstrapProperties.adminEmail}")
            usuario.perfis = mutableSetOf(Perfil.ROLE_ADMINISTRADOR, Perfil.ROLE_COLABORADOR)
        }

        usuarioRepository.save(usuario)
    }
}
