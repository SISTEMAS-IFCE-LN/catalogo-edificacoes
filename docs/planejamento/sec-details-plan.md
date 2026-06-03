### Passo 1: Atualização da Documentação (Commit 1)

**Objetivo:** Refletir as decisões arquiteturais antes de iniciar o código e alinhar a nomenclatura do domínio.

**Decisões consolidadas para o plano:**
- O `data.sql` será mantido nesta etapa inicial.
- A autenticação seguirá o fluxo **OAuth2 Authorization Code com PKCE + backend como broker**.
- O backend emitirá **JWT próprio** para acesso à API.
- O **refresh token** será guardado em **cookie HttpOnly**.

1. **Atualizar `docs/ambientes-internos/regras-negocio.md`:**
   - Substituir a nomenclatura de **Servidor** por **Colaborador** na Seção 4.
   - Adicionar e ajustar as regras de acesso para refletir:
     - login exclusivo com Google via OAuth 2.0;
     - acesso institucional para `@ifce.edu.br`;
     - usuários externos apenas se previamente cadastrados por um Administrador;
     - múltiplos perfis por utilizador;
     - lockout prevention para evitar perda do último administrador.

2. **Criar/atualizar `docs/arquitetura.md`:**
   - Explicar a divisão modular do projeto em `security-module`, `ambientes-internos-module` e `main-app`.
   - Descrever o papel do `main-app` como composição executável.
   - Explicitar que `security-module` concentra autenticação, emissão/validação de tokens e gestão de perfis.
   - Explicitar que `ambientes-internos-module` concentra apenas o domínio de ambientes.

3. **Registrar o impacto esperado na suíte de testes:**
   - Os testes unitários e JPA do domínio devem permanecer válidos após a separação em módulos, com ajustes mínimos de caminho/imports quando necessário.
   - Os testes de integração de controllers vão precisar considerar a segurança adicionada; chamadas hoje anônimas para endpoints protegidos passarão a exigir usuário autenticado com `@WithMockUser` ou configuração equivalente em MockMvc.
   - Os testes que validam erros de validação por parâmetros devem continuar cobrindo `400 Bad Request`, mas apenas depois de satisfazer os requisitos de autenticação quando o endpoint estiver protegido.
   - Os endpoints públicos devem continuar sendo testados sem autenticação para garantir que a regra de acesso público não seja regressiva.
   - Sempre que um teste atual passar a falhar por `401/403` após a introdução da segurança, o teste correspondente deve ser atualizado no mesmo ciclo do commit para preservar a cobertura.

---

### Passo 2: Criação do POM Pai (Commit 2)

**Objetivo:** Criar o `pom.xml` raiz para centralizar versões e coordenação dos submódulos.

1. Na raiz de `apis/ambientes-internos`, substituir o conteúdo do `pom.xml` por:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.8</version>
        <relativePath/>
    </parent>
    <groupId>br.edu.ifce</groupId>
    <artifactId>catalogo-edificacoes-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>security-module</module>
        <module>ambientes-internos-module</module>
        <module>main-app</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <kotlin.version>2.2.20</kotlin.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>br.edu.ifce</groupId>
                <artifactId>security-module</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>br.edu.ifce</groupId>
                <artifactId>ambientes-internos-module</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.jetbrains.kotlin</groupId>
                    <artifactId>kotlin-maven-plugin</artifactId>
                    <version>${kotlin.version}</version>
                    <configuration>
                        <args>
                            <arg>-Xjsr305=strict</arg>
                            <arg>-Xemit-jvm-type-annotations</arg>
                        </args>
                        <compilerPlugins>
                            <plugin>spring</plugin>
                            <plugin>jpa</plugin>
                            <plugin>all-open</plugin>
                        </compilerPlugins>
                        <pluginOptions>
                            <option>all-open:annotation=jakarta.persistence.Entity</option>
                            <option>all-open:annotation=jakarta.persistence.Embeddable</option>
                            <option>all-open:annotation=jakarta.persistence.MappedSuperclass</option>
                        </pluginOptions>
                    </configuration>
                    <dependencies>
                        <dependency>
                            <groupId>org.jetbrains.kotlin</groupId>
                            <artifactId>kotlin-maven-allopen</artifactId>
                            <version>${kotlin.version}</version>
                        </dependency>
                        <dependency>
                            <groupId>org.jetbrains.kotlin</groupId>
                            <artifactId>kotlin-maven-noarg</artifactId>
                            <version>${kotlin.version}</version>
                        </dependency>
                    </dependencies>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

---

### Passo 3: Criação do Módulo `ambientes-internos-module` (Commit 3)

**Objetivo:** Isolar o código de domínio e aplicação de ambientes já existente.

1. Criar a pasta `ambientes-internos-module`.
2. Mover para esse módulo o código de negócio atual do subdomínio de ambientes:
   - `src/main/kotlin` com controladores, casos de uso, entidades, DTOs e repositórios;
   - `src/test/kotlin` correspondente;
   - manter apenas o bootstrap e a configuração geral fora deste módulo.
3. Criar o ficheiro `ambientes-internos-module/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>br.edu.ifce</groupId>
        <artifactId>catalogo-edificacoes-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>ambientes-internos-module</artifactId>

    <dependencies>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
        <dependency><groupId>com.fasterxml.jackson.module</groupId><artifactId>jackson-module-kotlin</artifactId></dependency>
        <dependency><groupId>org.jetbrains.kotlin</groupId><artifactId>kotlin-reflect</artifactId></dependency>
        <dependency><groupId>org.jetbrains.kotlin</groupId><artifactId>kotlin-stdlib</artifactId></dependency>
    </dependencies>

    <build>
        <sourceDirectory>${project.basedir}/src/main/kotlin</sourceDirectory>
        <plugins><plugin><groupId>org.jetbrains.kotlin</groupId><artifactId>kotlin-maven-plugin</artifactId></plugin></plugins>
    </build>
</project>
```

4. Manter este módulo sem dependência direta de segurança para preservar coesão e baixo acoplamento.
5. Mover também os testes do domínio para este módulo, preservando a cobertura de entidades, repositórios e casos de uso.
6. Revisar os testes de integração de controller para que continuem válidos após a mudança de contexto do módulo, sem alterar o comportamento funcional esperado.

---

### Passo 4: Criação do Módulo `security-module` (Commit 4)

**Objetivo:** Concentrar identidade, autorização, persistência de usuários e emissão/validação de tokens.

1. Criar a pasta `security-module` e sua estrutura `src/main/kotlin/br/edu/ifce/security`.
2. Criar o ficheiro `security-module/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>br.edu.ifce</groupId>
        <artifactId>catalogo-edificacoes-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>security-module</artifactId>

    <dependencies>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-oauth2-client</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-oauth2-resource-server</artifactId></dependency>
        <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-oauth2-jose</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
        <dependency><groupId>org.jetbrains.kotlin</groupId><artifactId>kotlin-reflect</artifactId></dependency>
        <dependency><groupId>org.jetbrains.kotlin</groupId><artifactId>kotlin-stdlib</artifactId></dependency>
    </dependencies>

    <build>
        <sourceDirectory>${project.basedir}/src/main/kotlin</sourceDirectory>
        <plugins><plugin><groupId>org.jetbrains.kotlin</groupId><artifactId>kotlin-maven-plugin</artifactId></plugin></plugins>
    </build>
</project>
```

3. Estruturar o módulo com subpacotes para: `domain`, `repository`, `service`, `config`, `controller`.
4. Planejar neste módulo a lógica de: autenticação OAuth2 com Google, emissão de JWT próprio, refresh token persistido e rotacionado, lockout prevention, controle de perfis cumulativos.

---

### Passo 5: Criação do Módulo `main-app` (Commit 5)

**Objetivo:** Criar o pacote executável que integra segurança e domínio.

1. Criar a pasta `main-app` e sua estrutura `src/main/kotlin/br/edu/ifce/`.
2. Mover a classe `AmbientesInternosApp.kt` para `main-app`:

```kotlin
package br.edu.ifce

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["br.edu.ifce.ambientes_internos", "br.edu.ifce.security"])
class AmbientesInternosApp

fun main(args: Array<String>) {
    runApplication<AmbientesInternosApp>(*args)
}
```

3. Mover `src/main/resources/application.yml` para o `main-app`.
4. Mover `data.sql` para o `main-app`, mantendo o seed inicial e a criação do administrador padrão de forma idempotente.
5. Criar o ficheiro `main-app/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>br.edu.ifce</groupId>
        <artifactId>catalogo-edificacoes-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>main-app</artifactId>

    <dependencies>
        <dependency><groupId>br.edu.ifce</groupId><artifactId>security-module</artifactId></dependency>
        <dependency><groupId>br.edu.ifce</groupId><artifactId>ambientes-internos-module</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-devtools</artifactId><scope>runtime</scope><optional>true</optional></dependency>
        <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>
    </dependencies>

    <build>
        <sourceDirectory>${project.basedir}/src/main/kotlin</sourceDirectory>
        <plugins>
            <plugin><groupId>org.jetbrains.kotlin</groupId><artifactId>kotlin-maven-plugin</artifactId></plugin>
            <plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId>
                <configuration><mainClass>br.edu.ifce.AmbientesInternosAppKt</mainClass></configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

6. Neste módulo devem ficar as configurações de composição da aplicação, CORS, propriedades de OAuth2, JWT e cookies.

---

### Passo 6: Entidades de Utilizador e Refresh Token (`security-module`) (Commit 6)

**Objetivo:** Criar o modelo de dados para utilizadores, perfis múltiplos e controle de sessão/token.

1. Em `security-module/src/main/kotlin/br/edu/ifce/security/domain`, criar o Enum:

```kotlin
package br.edu.ifce.security.domain

enum class Perfil {
    ROLE_COLABORADOR, ROLE_VALIDADOR, ROLE_GESTOR_SISTEMA, ROLE_ADMINISTRADOR
}
```

2. Crie a entidade `Utilizador`:

```kotlin
package br.edu.ifce.security.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "utilizadores")
class Utilizador(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @Column(nullable = false)
    var nome: String,
    
    @Column(nullable = false)
    var ativo: Boolean = true,
    
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "utilizador_perfis", joinColumns = [JoinColumn(name = "utilizador_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil")
    var perfis: MutableSet<Perfil> = mutableSetOf()
)
```

3. Crie a entidade `RefreshToken`:

```kotlin
package br.edu.ifce.security.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val token: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id", nullable = false)
    val utilizador: Utilizador,
    
    @Column(name = "expira_em", nullable = false)
    val expiraEm: LocalDateTime,
    
    @Column(name = "revogado", nullable = false)
    var revogado: Boolean = false,
    
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()
)
```

4. Crie os repositórios em `br.edu.ifce.security.repository`:

```kotlin
package br.edu.ifce.security.repository

import br.edu.ifce.security.domain.Utilizador
import br.edu.ifce.security.domain.Perfil
import org.springframework.data.jpa.repository.JpaRepository

interface UtilizadorRepository : JpaRepository<Utilizador, Long> {
    fun findByEmail(email: String): Utilizador?
    fun countByAtivoTrueAndPerfisContains(perfil: Perfil): Long
}
```

```kotlin
package br.edu.ifce.security.repository

import br.edu.ifce.security.domain.RefreshToken
import br.edu.ifce.security.domain.Utilizador
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?
    fun findByUtilizadorAndRevogadoFalse(utilizador: Utilizador): RefreshToken?
    fun deleteByUtilizadorAndExpiraEmBefore(utilizador: Utilizador, dataExpiracao: LocalDateTime)
}
```

5. Garantir consultas essenciais: localizar usuário por e-mail, contar administradores ativos, localizar refresh token válido por identificador/usuário.

---

### Passo 7: Fluxo OAuth2 + Provisionamento + Emissão de JWT (Commit 7)

**Objetivo:** Implementar o fluxo de autenticação com Google e a lógica de autorização do domínio.

1. Crie `CustomOAuth2UserService` em `br.edu.ifce.security.service`:

```kotlin
package br.edu.ifce.security.service

import br.edu.ifce.security.domain.Perfil
import br.edu.ifce.security.domain.Utilizador
import br.edu.ifce.security.repository.UtilizadorRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class CustomOAuth2UserService(
    private val utilizadorRepository: UtilizadorRepository
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val email = oAuth2User.attributes["email"] as String? 
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Email não fornecido pelo provedor OAuth2")
        val nome = oAuth2User.attributes["name"] as String? ?: "Sem Nome"

        var utilizador = utilizadorRepository.findByEmail(email)

        if (utilizador == null) {
            if (email.endsWith("@ifce.edu.br")) {
                utilizador = Utilizador(email = email, nome = nome)
                utilizador.perfis.add(Perfil.ROLE_COLABORADOR)
                utilizadorRepository.save(utilizador)
            } else {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado: utilizador externo não registado.")
            }
        }

        if (!utilizador.ativo) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Utilizador inativo.")

        val authorities = utilizador.perfis.map { SimpleGrantedAuthority(it.name) }
        return DefaultOAuth2User(authorities, oAuth2User.attributes, "email")
    }
}
```

2. Crie o serviço `JwtService` para emissão e validação de tokens:

```kotlin
package br.edu.ifce.security.service

import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JwtService(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder
) {

    fun gerarAccessToken(userId: Long, email: String, roles: List<String>): String {
        val agora = Instant.now()
        val expiracao = agora.plusSeconds(900L) // 15 minutos

        val claims = JwtClaimsSet.builder()
            .issuedAt(agora)
            .expiresAt(expiracao)
            .subject(userId.toString())
            .claim("email", email)
            .claim("roles", roles)
            .build()

        return jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(claims)).tokenValue
    }

    fun ehValido(token: String): Boolean {
        return try {
            jwtDecoder.decode(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun extrairUserId(token: String): Long? {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.subject.toLong()
        } catch (e: Exception) {
            null
        }
    }

    fun extrairEmail(token: String): String? {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.getClaimAsString("email")
        } catch (e: Exception) {
            null
        }
    }

    fun extrairRoles(token: String): List<String>? {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.getClaimAsStringList("roles")
        } catch (e: Exception) {
            null
        }
    }
}
```

3. Crie o serviço `RefreshTokenService` para gerenciar tokens de renovação:

```kotlin
package br.edu.ifce.security.service

import br.edu.ifce.security.domain.RefreshToken
import br.edu.ifce.security.domain.Utilizador
import br.edu.ifce.security.repository.RefreshTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class RefreshTokenService(
    private val repository: RefreshTokenRepository,
    @Value("\${jwt.refresh-expiration:2592000000}")
    private val refreshTokenExpirationMs: Long
) {
    @Transactional
    fun gerarRefreshToken(utilizador: Utilizador): RefreshToken {
        // Revogar tokens antigos
        val tokenAntigo = repository.findByUtilizadorAndRevogadoFalse(utilizador)
        if (tokenAntigo != null) {
            tokenAntigo.revogado = true
            repository.save(tokenAntigo)
        }

        val novoToken = RefreshToken(
            token = UUID.randomUUID().toString(),
            utilizador = utilizador,
            expiraEm = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000)
        )
        return repository.save(novoToken)
    }

    fun validarRefreshToken(token: String): RefreshToken? {
        val refreshToken = repository.findByToken(token) ?: return null
        return if (!refreshToken.revogado && refreshToken.expiraEm.isAfter(LocalDateTime.now())) {
            refreshToken
        } else {
            null
        }
    }

    @Transactional
    fun revogarRefreshToken(token: String) {
        val refreshToken = repository.findByToken(token)
        if (refreshToken != null) {
            refreshToken.revogado = true
            repository.save(refreshToken)
        }
    }

    @Transactional
    fun limparTokensExpirados(utilizador: Utilizador) {
        repository.deleteByUtilizadorAndExpiraEmBefore(utilizador, LocalDateTime.now())
    }
}
```

4. Planejar o retorno do login para a SPA/mobile: access token no corpo da resposta, refresh token em cookie `HttpOnly`, `Secure` e com `SameSite` apropriado.
5. Tratar falhas de autenticação com exceções de negócio mapeadas para HTTP adequado, evitando `IllegalArgumentException` genérico.

---

### Passo 8: Configuração do `SecurityFilterChain` e Regras HTTP (Commit 8)

**Objetivo:** Configurar Spring Security para o fluxo híbrido e proteger a API com JWT próprio.

1. Crie `RsaKeyProperties` em `br.edu.ifce.security.config`:

```kotlin
package br.edu.ifce.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@ConfigurationProperties(prefix = "rsa")
data class RsaKeyProperties(
    var publicKey: RSAPublicKey? = null,
    var privateKey: RSAPrivateKey? = null
)
```

2. Crie `SecurityConfig` em `br.edu.ifce.security.config`:

```kotlin
package br.edu.ifce.security.config

import br.edu.ifce.security.service.CustomOAuth2UserService
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val rsaKeyProperties: RsaKeyProperties
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                // Endpoints públicos para ambientes publicados
                auth.requestMatchers(HttpMethod.GET, "/api/ambientes/publicados/**").permitAll()
                auth.requestMatchers(HttpMethod.GET, "/api/ambientes/tipo", "/api/ambientes/nome", "/api/ambientes/localizacao").permitAll()
                
                // Endpoints de autenticação
                auth.requestMatchers("/auth/**").permitAll()
                auth.requestMatchers("/health").permitAll()
                
                // Qualquer outro request exige autenticação
                auth.anyRequest().authenticated()
            }
            .oauth2Login { login ->
                login.userInfoEndpoint { userInfo ->
                    userInfo.userService(customOAuth2UserService)
                }
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
        val rsaKey = RSAKey.Builder(rsaKeyProperties.publicKey)
            .privateKey(rsaKeyProperties.privateKey)
            .build()
        val jwkSet = JWKSet(rsaKey)
        val jwkSource = ImmutableJWKSet<SecurityContext>(jwkSet)
        return NimbusJwtEncoder(jwkSource)
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withPublicKey(rsaKeyProperties.publicKey).build()
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
        configuration.allowedOrigins = listOf("http://localhost:3000", "http://localhost:4200", "https://seu-frontend-domain.com")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.exposedHeaders = listOf("Authorization")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
```

3. Configurar também em `main-app/src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: catalogo-edificacoes
  profiles:
    active: ${PROFILE_ACTIVE:dev}
  jpa:
    open-in-view: false
  sql:
    init:
      mode: always
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          google:
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo

# Configuração das chaves RSA para JWT
rsa:
  public-key: ${JWT_PUBLIC_KEY}
  private-key: ${JWT_PRIVATE_KEY}
```

4. Configurar propriedades de cookie em `main-app/src/main/resources/application.yml`:

```yaml
server:
  servlet:
    http-only: true
    session:
      cookie:
        http-only: true
        secure: true
        same-site: strict
        max-age: 2592000
```

5. Regras HTTP iniciais: `permitAll` para listagens públicas, autenticação obrigatória para o restante, suporte a `@PreAuthorize` nos endpoints de negócio.
6. Prever configuração de CORS para SPA e futura mobile, sem abrir origem desnecessária.
7. Adicionar suporte de testes para a camada de segurança, incluindo dependência de teste apropriada para MockMvc com autenticação simulada.
8. Para gerar as chaves RSA em desenvolvimento:

```bash
# Gerar chave privada
openssl genrsa -out private.pem 2048

# Gerar chave pública
openssl rsa -in private.pem -pubout -out public.pem

# Converter para PKCS#8 (se necessário)
openssl pkcs8 -topk8 -in private.pem -out private_pkcs8.pem -nocrypt
```

Depois, configure as variáveis de ambiente `JWT_PUBLIC_KEY` e `JWT_PRIVATE_KEY` com o conteúdo das chaves em formato PEM.

---

### Passo 9: Proteção dos Endpoints de Ambientes (Commit 9)

**Objetivo:** Aplicar autorização explícita e horizontal no módulo de domínio.

1. No `AmbienteNaoPublicadoController.kt`, adicione `@PreAuthorize` no nível da classe:

```kotlin
import org.springframework.security.access.prepost.PreAuthorize

@Validated
@RestController
@RequestMapping(AMBIENTE_NAO_PUBLICADO_PATH)
@PreAuthorize("hasAuthority('ROLE_GESTOR_SISTEMA')")
class AmbienteNaoPublicadoController(
    private val useCasesNaoPublicado: IAmbienteNaoPublicadoUseCases
) : BaseController<AmbienteRes>(useCasesNaoPublicado) {
    // ... resto do código
}
```

2. No `AmbienteValidacaoController.kt`, adicione `@PreAuthorize` no nível da classe:

```kotlin
@Validated
@RestController
@RequestMapping(AMBIENTE_VALIDACAO_PATH)
@PreAuthorize("hasAuthority('ROLE_VALIDADOR')")
class AmbienteValidacaoController(
    private val useCasesValidacao: IAmbienteValidacaoUseCases
) : BaseController<AmbienteRes>(useCasesValidacao) {
    // ... resto do código
}
```

3. No `BaseController.kt`, proteja o endpoint de consulta por ID:

```kotlin
@GetMapping("/{id}")
@PreAuthorize("hasAuthority('ROLE_COLABORADOR')")
fun obterAmbientePorId(@PathVariable @Positive(message = MSG_VAL_ID) id: Long): ResponseEntity<RES> {
    return ResponseEntity.ok(useCases.obterAmbientePorId(id))
}
```

4. No `AmbientePublicadoController.kt`, proteja o endpoint de esquadrias:

```kotlin
@Validated
@RestController
@RequestMapping(AMBIENTE_PUBLICADO_PATH)
class AmbientePublicadoController(
    private val useCasesPublicado: IAmbientePublicadoUseCases
) : BaseController<AmbienteRes>(useCasesPublicado) {

    @GetMapping("/esquadrias")
    @PreAuthorize("hasAuthority('ROLE_COLABORADOR')")
    fun listarEsquadriasAmbientes(
        @RequestParam @NotEmpty(message = MSG_LISTA_VAZIA + "um ID.") ids: Set<@Positive(message = MSG_VAL_ID) Long>,
        pageable: Pageable
    ): ResponseEntity<EsquadriasAmbientesPaginadosRes> {
        return ResponseEntity.ok(useCasesPublicado.listarEsquadriasAmbientes(ids, pageable))
    }
}
```

5. Manter as listagens públicas sem autenticação apenas quando a regra de negócio realmente permitir. Os endpoints GET padrão herdados de `BaseController` (listar, por tipo, por nome, por localização) devem permanecer sem `@PreAuthorize` para permitir acesso público.
6. Atualizar a suíte de integração dos controllers para refletir o novo comportamento:
   - testes de endpoints protegidos devem usar autenticação simulada com as authorities corretas;
   - testes de endpoints públicos devem continuar sem autenticação;
   - testes que antes esperavam apenas erro de validação podem precisar primeiro satisfazer autenticação e depois validar o `400`.

---

### Passo 10: Gestão de Utilizadores e Lockout Prevention (Commit 10)

**Objetivo:** Permitir que o Administrador gerencie perfis sem risco de deixar o sistema sem admin ativo.

1. Crie `UtilizadorService` em `security-module`:

```kotlin
package br.edu.ifce.security.service

import br.edu.ifce.security.domain.Perfil
import br.edu.ifce.security.repository.UtilizadorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class UtilizadorService(private val repository: UtilizadorRepository) {

    @Transactional
    fun atualizarPerfis(id: Long, novosPerfis: Set<Perfil>) {
        val utilizador = repository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado") }
        
        // Verifica se está a tentar remover a role Administrador deste utilizador
        if (utilizador.perfis.contains(Perfil.ROLE_ADMINISTRADOR) && !novosPerfis.contains(Perfil.ROLE_ADMINISTRADOR)) {
            val totalAdmins = repository.countByAtivoTrueAndPerfisContains(Perfil.ROLE_ADMINISTRADOR)
            if (totalAdmins <= 1) {
                throw ResponseStatusException(HttpStatus.CONFLICT, 
                    "Ação negada: Não é possível remover o último Administrador do sistema.")
            }
        }
        
        // Regra Universal: Todos devem manter ROLE_COLABORADOR
        val perfisFinais = novosPerfis.toMutableSet()
        perfisFinais.add(Perfil.ROLE_COLABORADOR)
        
        utilizador.perfis = perfisFinais
        repository.save(utilizador)
    }

    @Transactional
    fun desativarUtilizador(id: Long) {
        val utilizador = repository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado") }
        
        // Previne desativar o último administrador
        if (utilizador.perfis.contains(Perfil.ROLE_ADMINISTRADOR)) {
            val totalAdmins = repository.countByAtivoTrueAndPerfisContains(Perfil.ROLE_ADMINISTRADOR)
            if (totalAdmins <= 1) {
                throw ResponseStatusException(HttpStatus.CONFLICT, 
                    "Ação negada: Não é possível desativar o último Administrador do sistema.")
            }
        }
        
        utilizador.ativo = false
        repository.save(utilizador)
    }
}
```

2. Crie `UtilizadorController` em `br.edu.ifce.security.controller`:

```kotlin
package br.edu.ifce.security.controller

import br.edu.ifce.security.domain.Perfil
import br.edu.ifce.security.service.UtilizadorService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/utilizadores")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
class UtilizadorController(private val service: UtilizadorService) {

    @PatchMapping("/{id}/perfis")
    fun atualizarPerfis(
        @PathVariable id: Long, 
        @RequestBody perfis: Set<Perfil>
    ): ResponseEntity<Void> {
        service.atualizarPerfis(id, perfis)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/desativar")
    fun desativarUtilizador(@PathVariable id: Long): ResponseEntity<Void> {
        service.desativarUtilizador(id)
        return ResponseEntity.noContent().build()
    }
}
```

3. Expor endpoint para atualização de perfis por usuário, mantendo o modelo simples com substituição da lista de perfis.
4. Tratar as falhas de lockout como exceção de negócio clara, retornando `409 Conflict` via `ResponseStatusException`.
5. Preparar a base para futuramente incluir listagem de usuários e ações administrativas complementares.

---

### Passo 11: Seed Seguro do Administrador Padrão (Commit 11)

**Objetivo:** Garantir a existência do Administrador inicial sem depender de intervenção manual.

1. Manter `data.sql` no `main-app` com inserção idempotente do usuário padrão `admin@ifce.edu.br`:

```sql
-- Criar tabelas (geradas automaticamente pelo JPA, mas incluir aqui para clareza do seed)
-- As tabelas serão criadas pelo DDL do Hibernate

-- Inserir utilizador administrador padrão (idempotente)
INSERT INTO utilizadores (email, nome, ativo, criado_em) 
SELECT 'admin@ifce.edu.br', 'Administrador Padrão', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM utilizadores WHERE email = 'admin@ifce.edu.br');

-- Atribuir ROLE_ADMINISTRADOR ao admin padrão
INSERT INTO utilizador_perfis (utilizador_id, perfil)
SELECT u.id, 'ROLE_ADMINISTRADOR'
FROM utilizadores u
WHERE u.email = 'admin@ifce.edu.br'
  AND NOT EXISTS (
      SELECT 1 FROM utilizador_perfis up 
      WHERE up.utilizador_id = u.id AND up.perfil = 'ROLE_ADMINISTRADOR'
  );

-- Atribuir ROLE_COLABORADOR ao admin padrão
INSERT INTO utilizador_perfis (utilizador_id, perfil)
SELECT u.id, 'ROLE_COLABORADOR'
FROM utilizadores u
WHERE u.email = 'admin@ifce.edu.br'
  AND NOT EXISTS (
      SELECT 1 FROM utilizador_perfis up 
      WHERE up.utilizador_id = u.id AND up.perfil = 'ROLE_COLABORADOR'
  );
```

2. Garantir que o seed inicial atribua, no mínimo: `ROLE_ADMINISTRADOR` e `ROLE_COLABORADOR`.
3. Evitar duplicidade com lógica baseada em `WHERE NOT EXISTS`.
4. Documentar que o seed é voltado ao ambiente inicial e de desenvolvimento, mantendo o plano de evolução para migrações versionadas em etapa futura, se necessário.

---

### Passo 12: Endpoints de Autenticação (Login, Refresh, Logout) (Commit 12)

**Objetivo:** Implementar os endpoints que o SPA/mobile chamarão para gerenciar a sessão de autenticação.

1. Crie `AuthController` em `br.edu.ifce.security.controller`:

```kotlin
package br.edu.ifce.security.controller

import br.edu.ifce.security.service.JwtService
import br.edu.ifce.security.service.RefreshTokenService
import br.edu.ifce.security.repository.UtilizadorRepository
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType

@RestController
@RequestMapping("/auth")
class AuthController(
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val utilizadorRepository: UtilizadorRepository
) {

    data class LoginResponse(
        val accessToken: String,
        val tokenType: String = "Bearer",
        val expiresIn: Long = 900
    )

    data class RefreshRequest(
        val refreshToken: String
    )

    /**
     * POST /auth/login/callback
     * Endpoint que recebe o resultado do login OAuth2 do Google.
     * O SPA enviará o código de autorização ou o backend receberá via servidor.
     * Após validação, retorna JWT de acesso e refresh token em cookie.
     */
    @PostMapping("/login/callback", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun loginCallback(
        authentication: Authentication,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val email = authentication.name
        val utilizador = utilizadorRepository.findByEmail(email)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        // Gerar JWT de acesso
        val roles = utilizador.perfis.map { it.name }
        val accessToken = jwtService.gerarAccessToken(utilizador.id!!, utilizador.email, roles)

        // Gerar refresh token e armazenar em cookie
        val refreshToken = refreshTokenService.gerarRefreshToken(utilizador)
        response.addCookie(criarCookieRefreshToken(refreshToken.token))

        return ResponseEntity.ok(LoginResponse(accessToken))
    }

    /**
     * POST /auth/refresh
     * Endpoint para renovar o access token usando o refresh token.
     */
    @PostMapping("/refresh")
    fun refresh(
        @RequestBody request: RefreshRequest,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val refreshToken = refreshTokenService.validarRefreshToken(request.refreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val utilizador = refreshToken.utilizador
        val roles = utilizador.perfis.map { it.name }
        val novoAccessToken = jwtService.gerarAccessToken(utilizador.id!!, utilizador.email, roles)

        // Rotacionar refresh token
        val novoRefreshToken = refreshTokenService.gerarRefreshToken(utilizador)
        response.addCookie(criarCookieRefreshToken(novoRefreshToken.token))

        return ResponseEntity.ok(LoginResponse(novoAccessToken))
    }

    /**
     * POST /auth/logout
     * Endpoint para revogar o refresh token e fazer logout.
     */
    @PostMapping("/logout")
    fun logout(
        @CookieValue(value = "refreshToken", required = false) refreshToken: String?,
        response: HttpServletResponse
    ): ResponseEntity<Void> {
        if (refreshToken != null) {
            refreshTokenService.revogarRefreshToken(refreshToken)
        }
        // Limpar cookie
        response.addCookie(criarCookieRefreshTokenVazio())
        return ResponseEntity.noContent().build()
    }

    private fun criarCookieRefreshToken(token: String): jakarta.servlet.http.Cookie {
        return jakarta.servlet.http.Cookie("refreshToken", token).apply {
            isHttpOnly = true
            secure = true // Apenas via HTTPS
            path = "/"
            maxAge = 30 * 24 * 60 * 60 // 30 dias
            setAttribute("SameSite", "Strict")
        }
    }

    private fun criarCookieRefreshTokenVazio(): jakarta.servlet.http.Cookie {
        return jakarta.servlet.http.Cookie("refreshToken", "").apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 0 // Apaga o cookie
            setAttribute("SameSite", "Strict")
        }
    }
}
```

2. Os endpoints de login/refresh devem ser protegidos de rate limiting.

---

### Passo 13: Estratégia de Testes para o Novo Modelo Modular (Commit 13)

**Objetivo:** Garantir cobertura mínima para o `security-module` e ajustar a suíte atual para o novo fluxo de autenticação/autorização.

1. Criar testes unitários do `security-module` para as regras críticas:
   - `JwtService`: geração, extração e validação de `access token`.
   - `RefreshTokenService`: geração, validação, rotação, revogação e expiração.
   - `CustomOAuth2UserService`: provisionamento de usuário institucional, bloqueio de usuário externo não cadastrado e tratamento de usuário inativo.
   - `UtilizadorService`: atualização de perfis e prevenção de lockout do último administrador.

2. Criar testes de integração para o fluxo de autenticação:
   - `AuthController` para `login`, `refresh` e `logout`.
   - validação do cookie `refreshToken` com `HttpOnly`, `Secure` e `SameSite`.
   - cenários de renovação com rotação do refresh token.

3. Criar testes de configuração e autorização para `SecurityConfig`:
   - endpoints públicos acessíveis sem autenticação;
   - endpoints protegidos retornando `401/403` quando apropriado;
   - conversão correta das authorities a partir do JWT.

4. Ajustar os testes existentes do módulo de ambientes quando a segurança for introduzida:
   - autenticar cenários que agora exigem `ROLE_COLABORADOR`, `ROLE_VALIDADOR` ou `ROLE_GESTOR_SISTEMA`;
   - preservar testes de endpoints públicos sem autenticação;
   - validar que os testes de parâmetros continuam cobrindo `400 Bad Request` após a autenticação ser satisfeita.

5. Manter os testes JPA e unitários do domínio praticamente intactos, alterando apenas caminhos/imports se a separação em módulos exigir.

6. Incluir a dependência de teste apropriada para suporte a Spring Security no novo módulo e nos testes de controller do módulo executável.