package br.edu.ifce.security

import br.edu.ifce.security.config.properties.JwtProperties
import br.edu.ifce.security.model.domain.Perfil
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@TestConfiguration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties::class)
class TestSecurityConfig {

    private val publicKey: RSAPublicKey
    private val privateKey: RSAPrivateKey

    init {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
        val keyPair = keyPairGenerator.generateKeyPair()
        publicKey = keyPair.public as RSAPublicKey
        privateKey = keyPair.private as RSAPrivateKey
    }

    @Bean
    @Order(1)
    fun authFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/auth/**")
            .csrf { csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
        return http.build()
    }

    @Bean
    @Order(2)
    fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/**")
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.GET, "/api/usuarios/me").authenticated()
                auth.requestMatchers(HttpMethod.GET, "/api/usuarios").hasAuthority(Perfil.ROLE_ADMINISTRADOR.name)
                auth.requestMatchers("/api/usuarios/**").hasAuthority(Perfil.ROLE_ADMINISTRADOR.name)
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
    fun jwtEncoder(): JwtEncoder {
        val rsaKey = RSAKey.Builder(publicKey).privateKey(privateKey).build()
        return NimbusJwtEncoder(ImmutableJWKSet<SecurityContext>(JWKSet(rsaKey)))
    }

    @Bean
    fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build()

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles")
        grantedAuthoritiesConverter.setAuthorityPrefix("")
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
        return jwtAuthenticationConverter
    }
}