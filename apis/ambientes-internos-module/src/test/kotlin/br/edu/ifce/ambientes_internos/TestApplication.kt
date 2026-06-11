package br.edu.ifce.ambientes_internos

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import br.edu.ifce.security.config.BootstrapAdminRunner
import br.edu.ifce.security.config.SecurityConfig
import br.edu.ifce.security.config.JwtProperties
import br.edu.ifce.security.config.RsaKeyProperties
import br.edu.ifce.security.controller.AuthController
import br.edu.ifce.security.controller.UsuarioController
import br.edu.ifce.security.model.application.service.AuthService
import br.edu.ifce.security.model.application.service.CustomOAuth2UserService
import br.edu.ifce.security.model.application.service.JwtService
import br.edu.ifce.security.model.application.service.RefreshTokenService
import br.edu.ifce.security.model.application.service.UsuarioService

@SpringBootApplication
@EntityScan(basePackages = ["br.edu.ifce.ambientes_internos.model.domain", "br.edu.ifce.security.model.domain"])
@EnableJpaRepositories(basePackages = ["br.edu.ifce.ambientes_internos.model.repository", "br.edu.ifce.security.model.repository"])
@ComponentScan(
    basePackages = ["br.edu.ifce.ambientes_internos", "br.edu.ifce.security"],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [
                BootstrapAdminRunner::class,
                SecurityConfig::class,
                AuthController::class,
                UsuarioController::class,
                AuthService::class,
                CustomOAuth2UserService::class,
                JwtService::class,
                RefreshTokenService::class,
                UsuarioService::class,
                JwtProperties::class,
                RsaKeyProperties::class
            ]
        )
    ]
)
class TestApplication
