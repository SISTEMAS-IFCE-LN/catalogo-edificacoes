package br.edu.ifce.security

import br.edu.ifce.security.config.*
import br.edu.ifce.security.config.properties.RsaKeyProperties
import br.edu.ifce.security.controller.UsuarioController
import br.edu.ifce.security.model.application.service.CustomOAuth2UserService
import br.edu.ifce.security.model.application.service.UsuarioService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan(basePackages = ["br.edu.ifce.security.model.domain"])
@EnableJpaRepositories(basePackages = ["br.edu.ifce.security.model.repository"])
@ComponentScan(
    basePackages = ["br.edu.ifce.security", "br.edu.ifce.common"],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [
                BootstrapAdminRunner::class,
                SecurityConfig::class,
                JwtConfig::class,
                RsaKeyProperties::class,
                OAuth2LoginSuccessHandler::class,
                CustomOAuth2UserService::class,
                TokenCleanupScheduler::class,
                UsuarioController::class,
                UsuarioService::class
            ]
        )
    ]
)
class TestApplication
