package br.edu.ifce.security.config

import br.edu.ifce.common.config.ApiPaths.AMBIENTES_NAO_PUBLICADOS_PATH
import br.edu.ifce.common.config.ApiPaths.AMBIENTES_PUBLICADOS_PATH
import br.edu.ifce.common.config.ApiPaths.AMBIENTES_VALIDACAO_PATH
import br.edu.ifce.common.config.ApiPaths.AUTH_PATH
import br.edu.ifce.common.config.ApiPaths.USUARIOS_PATH
import br.edu.ifce.security.config.properties.BootstrapProperties
import br.edu.ifce.security.config.properties.FrontendProperties
import br.edu.ifce.security.config.properties.JwtProperties
import br.edu.ifce.security.config.properties.RsaKeyProperties
import br.edu.ifce.security.model.application.service.CustomOAuth2UserService
import br.edu.ifce.security.model.domain.Perfil
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(
    RsaKeyProperties::class,
    JwtProperties::class,
    BootstrapProperties::class,
    FrontendProperties::class
)
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val frontendProperties: FrontendProperties
) {

    private val oauth2Endpoints = listOf(
        "/oauth2/**",
        "/login/**",
        "/login/oauth2/**"
    )

    private val oauth2Matcher: RequestMatcher = OrRequestMatcher(
        oauth2Endpoints.map { PathPatternRequestMatcher.withDefaults().matcher(it) }
    )

    @Bean
    @Order(1)
    fun oauth2LoginFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(oauth2Matcher)
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .oauth2Login { oauth2 ->
                oauth2.userInfoEndpoint { userInfo ->
                    userInfo.userService(customOAuth2UserService)
                }
                oauth2.successHandler(oAuth2LoginSuccessHandler)

                val errorTarget = frontendProperties.callbackErrorUrl?.takeIf { it.isNotBlank() } ?: "/failure.html"
                oauth2.failureUrl(errorTarget)
            }
        return http.build()
    }

    @Bean
    @Order(2)
    fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher { request -> !oauth2Matcher.matches(request) }
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.customRequestMatchers(
                    listOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.PATCH, HttpMethod.DELETE),
                    "${AMBIENTES_NAO_PUBLICADOS_PATH}/**",
                    Perfil.ROLE_GESTOR_SISTEMA.name
                )
                auth.customRequestMatchers(
                    listOf(HttpMethod.GET, HttpMethod.PATCH),
                    "${AMBIENTES_VALIDACAO_PATH}/**",
                    Perfil.ROLE_VALIDADOR.name
                )
                auth.customRequestMatchers(
                    listOf(HttpMethod.GET, HttpMethod.PATCH),
                    "${USUARIOS_PATH}/**",
                    Perfil.ROLE_ADMINISTRADOR.name
                )
                auth.requestMatchers(
                    HttpMethod.GET,
                    AMBIENTES_PUBLICADOS_PATH,
                    "${AMBIENTES_PUBLICADOS_PATH}/tipo",
                    "${AMBIENTES_PUBLICADOS_PATH}/nome",
                    "${AMBIENTES_PUBLICADOS_PATH}/localizacao"
                ).permitAll()
                auth.requestMatchers(
                    HttpMethod.GET,
                    "${AMBIENTES_PUBLICADOS_PATH}/**"
                ).hasAuthority(Perfil.ROLE_COLABORADOR.name)
                auth.requestMatchers(
                    HttpMethod.GET,
                    "/callback.html",
                    "/failure.html",
                    "/actuator/health"
                ).permitAll()
                auth.requestMatchers(HttpMethod.POST, "${AUTH_PATH}/**").permitAll()
                auth.anyRequest().denyAll()
            }
            .oauth2ResourceServer { rs ->
                rs.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles")
        grantedAuthoritiesConverter.setAuthorityPrefix("")

        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
        return jwtAuthenticationConverter
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        // TODO: restringir origens antes de ir para produção.
        // Manter "*" apenas durante o desenvolvimento para simplificar testes locais.
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.exposedHeaders = listOf("Authorization")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

fun AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry.customRequestMatchers(
    methods: List<HttpMethod>,
    pattern: String,
    authority: String
) {
    methods.forEach { method ->
        this.requestMatchers(method, pattern).hasAuthority(authority)
    }
}