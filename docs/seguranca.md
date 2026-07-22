# Segurança do Catálogo de Edificações

Este documento descreve em detalhe o modelo de autenticação, autorização, gestão de tokens e configuração externa de segurança do sistema.

---

## 1. Visão geral

O sistema adota o fluxo **OAuth2 Authorization Code com PKCE** com o Google como provedor de identidade, e o backend Kotlin/Spring atuando como **broker** que emite e valida credenciais próprias (JWT) para acesso à API.

Componentes centrais:

| Componente | Caminho | Responsabilidade |
|---|---|---|
| `CustomOAuth2UserService` | `br.edu.ifce.security.model.application.service` | Provisiona/ativa o `Usuario` a partir dos atributos do Google. |
| `JwtService` | `br.edu.ifce.security.model.application.service` | Emite e valida o JWT próprio. |
| `RefreshTokenService` | `br.edu.ifce.security.model.application.service` | Persiste e rotaciona refresh tokens. |
| `AuthService` | `br.edu.ifce.security.model.application.service` | Orquestra login/refresh/logout. |
| `OAuth2LoginSuccessHandler` | `br.edu.ifce.security.config` | Gera JWT + refresh token no callback OAuth2 e redireciona para o frontend com `#token=` no fragmento da URL. |
| `AuthController` | `br.edu.ifce.security.controller` | Adaptador HTTP para refresh e logout. |
| `UsuarioService` | `br.edu.ifce.security.model.application.service` | Gestão de perfis e desativação (com lockout prevention). |
| `UsuarioController` | `br.edu.ifce.security.controller` | Endpoints administrativos de perfis. |
| `SecurityConfig` | `br.edu.ifce.security.config` | `SecurityFilterChain`, `oauth2Login`, `oauth2ResourceServer.jwt()`, CORS. |
| `JwtConfig` | `br.edu.ifce.security.config` | Beans `JwtEncoder` e `JwtDecoder` (RSA). |
| `RsaKeyProperties` | `br.edu.ifce.security.config.properties` | Bind de `rsa.public-key` / `rsa.private-key`. |
| `JwtProperties` | `br.edu.ifce.security.config.properties` | Bind de `jwt.access-token-expiration` / `jwt.refresh-expiration` / `jwt.cookie-secure` / `jwt.same-site`. |
| `FrontendProperties` | `br.edu.ifce.security.config.properties` | Bind de `frontend.callback-success-url` / `frontend.callback-error-url`. |
| `BootstrapAdminRunner` | `br.edu.ifce.security.config` | Garante a presença de um administrador institucional conhecido no boot. |

---

## 2. Fluxo de autenticação OAuth2 + JWT

```
┌────────┐                               ┌──────────┐         ┌────────┐        ┌──────────┐
│  SPA   │                               │ Backend  │         │ Google │        │  Banco   │
└───┬────┘                               └────┬─────┘         └───┬────┘        └────┬─────┘
    │  GET /oauth2/                           │                   │                  │
    │  authorization/                         │                   │                  │
    │  google                                 │                   │                  │
    ├────────────────────────────────────────►│                   │                  │
    │                                         │  302 → accounts   │                  │
    │                                         │  .google.com/...  │                  │
    │◄────────────────────────────────────────┤                   │                  │
    │                                         │                   │                  │
    │  [ usuário autentica no Google ]        │                   │                  │
    │                                         │                   │                  │
    │  302 → /login/oauth2/code/google        │                   │                  │
    │  ?code=...&state=...                    │                   │                  │
    ├────────────────────────────────────────►│                   │                  │
    │                                         │  troca code por   │                  │
    │                                         │  access_token     │                  │
    │                                         ├──────────────────►│                  │
    │                                         │◄──────────────────┤                  │
    │                                         │  userinfo (id,    │                  │
    │                                         │  email, name)     │                  │
    │                                         ├──────────────────►│                  │
    │                                         │◄──────────────────┤                  │
    │                                         │                                      │
    │                                         │  CustomOAuth2UserService:            │
    │                                         │  - email termina em @ifce.edu.br?    │
    │                                         │  - provisiona Usuario (se novo)      │
    │                                         │  - atribui ROLE_COLABORADOR          │
    │                                         │  - sincroniza nome (se divergente)   │
    │                                         ├─────────────────────────────────────►│
    │                                         │◄─────────────────────────────────────┤
    │                                         │                                      │
    │                                         │  OAuth2LoginSuccessHandler:          │
    │                                         │  - gera JWT (15 min)                 │
    │                                         │  - gera refresh token (1 h)          │
    │  302 /callback.html#token=..            │  - persiste refresh (revoga antigos) │
    │  Set-Cookie:                            │  - seta cookie HttpOnly refreshToken │
    │  refreshToken=..                        │  - redireciona (302) com #token=     │
    │◄────────────────────────────────────────┤                                      │
    
```

### Pontos de atenção

- O `oauth2Login` requer `SessionCreationPolicy.IF_REQUIRED` no `SecurityConfig` (chain 1) para armazenar temporariamente o `Authentication` durante o handshake.
- A API em si opera com `SessionCreationPolicy.STATELESS` (chain 3) e valida o JWT a cada requisição.
- O cookie de sessão HTTP é `SameSite=Lax` (necessário para o callback cross-site do OAuth2).
- O cookie `refreshToken` é `HttpOnly`, `Secure` (configurável via `JWT_COOKIE_SECURE`), `SameSite=Lax` (configurável via `JWT_COOKIE_SAME_SITE`), `path=/`, com `maxAge` igual ao valor de `jwt.refresh-expiration` (default 3600s / 1 h).
- O `OAuth2LoginSuccessHandler` gera os tokens diretamente no callback OAuth2 (dentro da chain 1, onde o `Authentication` está disponível) e redireciona o navegador para a URL de callback configurada, anexando o access token no fragmento (`#token=...`). O fragmento nunca é enviado ao servidor, mitigando fugas em logs ou headers `Referer`.

---

## 3. Fluxo de refresh

```
┌────────┐                  ┌──────────┐                  ┌────────┐
│  SPA   │                  │ Backend  │                  │  Banco │
└───┬────┘                  └────┬─────┘                  └────┬───┘
    │  access token expirou      │                             │
    │  POST /auth/refresh        │                             │
    │  (cookie refreshToken)     │                             │
    ├───────────────────────────►│                             │
    │                            │  RefreshTokenService:       │
    │                            │  - validarRefreshToken()    │
    │                            ├────────────────────────────►│
    │                            │◄────────────────────────────┤
    │                            │  - se válido: gerar novo    │
    │                            │    access token             │
    │                            ├────────────────────────────►│
    │                            │  200 OK                     │
    │                            │  Body: { accessToken }      │
    │◄───────────────────────────┤                             │
    │  { accessToken }           │                             │
```

**Importante:** a cada refresh bem-sucedido, o access token é sempre renovado. O refresh token é renovado apenas quando o tempo restante de vida do refresh token atual é menor que a expiração do access token (`jwt.access-token-expiration`); caso contrário, o mesmo refresh token é reutilizado. A rotação completa do refresh token ocorre no login, quando um novo token é gerado e o antigo é revogado. Quando um novo refresh token é gerado no refresh, o `AuthController` emite um novo cookie `Set-Cookie` com o token atualizado.

---

## 4. Proteção CSRF em `/auth/**`

Os endpoints `POST /auth/refresh` e `POST /auth/logout` dependem do cookie `HttpOnly` `refreshToken`, o que os torna vulneráveis a ataques CSRF. A proteção é implementada via **Double Submit Cookie** com `CookieCsrfTokenRepository`.

### 4.1. Arquitetura

Uma `SecurityFilterChain` dedicada (`authFilterChain`, `@Order(2)`) intercepta `/auth/**` com CSRF habilitado:

| Order | Chain | Matcher | CSRF | Session |
|---|---|---|---|---|
| 1 | `oauth2LoginFilterChain` | `/oauth2/**`, `/login/**` | habilitado (default) | `IF_REQUIRED` |
| 2 | `authFilterChain` | `/auth/**` | habilitado (`CookieCsrfTokenRepository`) | `IF_REQUIRED` |
| 3 | `apiFilterChain` | demais | desabilitado | `STATELESS` |

### 4.2. Fluxo CSRF

```
┌────────┐                          ┌──────────┐
│  SPA   │                          │ Backend  │
└───┬────┘                          └────┬─────┘
    │  1. GET /auth/csrf-token           │
    │  (após OAuth2 redirect)            │
    ├───────────────────────────────────►│
    │  Set-Cookie: XSRF-TOKEN=abc123     │  CookieCsrfTokenRepository
    │  Body: { token: "abc123" }         │  (HttpOnly=false)
    │◄───────────────────────────────────┤
    │                                    │
    │  2. POST /auth/refresh             │
    │  Cookie: refreshToken=xyz          │
    │  Header: X-XSRF-TOKEN=abc123       │
    ├───────────────────────────────────►│
    │                                    │  CsrfFilter valida:
    │                                    │  cookie XSRF-TOKEN == header X-XSRF-TOKEN?
    │  200 OK { accessToken }            │
    │◄───────────────────────────────────┤
```

### 4.3. Pontos de atenção

- O `CookieCsrfTokenRepository.withHttpOnlyFalse()` emite o cookie `XSRF-TOKEN` com `HttpOnly=false` para que o JavaScript possa lê-lo.
- O frontend (`callback.html`) busca o token via `GET /auth/csrf-token` e o envia no header `X-XSRF-TOKEN` em cada POST.
- A proteção CSRF aplica-se **apenas** a `/auth/**`. A API (chain 3) continua stateless e sem CSRF, pois usa JWT via `Authorization: Bearer` (não enviado automaticamente pelo navegador).
- O `failure.html` não é afetado — não faz chamadas a `/auth/**`.

---

## 5. Perfis e hierarquia cumulativa

Quatro perfis, armazenados no campo `perfis` da tabela `usuario_perfis` como `Set<Perfil>`:

| Perfil | Permissões representadas |
|---|---|
| `ROLE_COLABORADOR` | Listar e consultar ambientes publicados. **Obrigatório para todos os usuários.** |
| `ROLE_VALIDADOR` | Listar e gerenciar ambientes em validação (publicar, privar). |
| `ROLE_GESTOR_SISTEMA` | CRUD de ambientes não publicados; submeter para validação. |
| `ROLE_ADMINISTRADOR` | Gerir perfis de outros usuários; desativar contas. |

**Regra universal:** o `UsuarioService.atualizarPerfis` sempre **adiciona** `ROLE_COLABORADOR` ao set final, garantindo que nenhum usuário perca o acesso mínimo.

---

## 6. Lockout prevention

`UsuarioService.verificarExclusaoAdm()` é invocado por `atualizarPerfis` e `desativar` para impedir que o sistema fique sem administrador ativo.

```kotlin
private fun verificarExclusaoAdm() {
    val totalAdmins = repository.countByAtivoTrueAndPerfisContains(Perfil.ROLE_ADMINISTRADOR)
    if (totalAdmins <= 1) {
        throw ResponseStatusException(
            HttpStatus.CONFLICT,
            "Ação negada: Não é possível remover/desativar o último Administrador do sistema."
        )
    }
}
```

**Cenários bloqueados com `409 Conflict`:**
- `PATCH /api/usuarios/{id}/perfis` removendo `ROLE_ADMINISTRADOR` do último admin ativo.
- `PATCH /api/usuarios/{id}/desativar` quando o usuário é o último admin ativo.

---

## 7. Mapeamento de endpoints HTTP

Definido em `SecurityConfig.apiFilterChain` (chain 3, com `@Order(3)`).

### 7.1. Endpoints públicos (`permitAll`)

| Path | Método | Observação |
|---|---|---|
| `/api/ambientes/publicados/**` | todos | Listagens e detalhes de ambientes publicados. |
| `/auth/**` | todos | Refresh, logout. |
| `/health` | todos | Health check. |
| `/oauth2/**` | todos | Handshake OAuth2 (chain 1, com `IF_REQUIRED`). |
| `/login/**` | todos | Handshake OAuth2. |

### 7.2. Endpoints protegidos por `SecurityConfig`

| Path | Authority exigida |
|---|---|
| `/api/ambientes/nao-publicados/**` (GET, POST, PATCH, DELETE) | `ROLE_GESTOR_SISTEMA` |
| `/api/ambientes/validacao/**` (GET, PATCH) | `ROLE_VALIDADOR` |
| `/api/ambientes/publicados/{id}` (GET) | `ROLE_COLABORADOR` |
| `/api/ambientes/publicados/esquadrias` (GET) | `ROLE_COLABORADOR` |
| `/api/usuarios/**` (GET, PATCH) | `ROLE_ADMINISTRADOR` |

### 7.3. Resposta a acessos não autorizados

- Sem `Authentication` em endpoint protegido → `401 Unauthorized`.
- `Authentication` presente mas sem `Authority` exigida → `403 Forbidden`.
- Lockout prevention violado → `409 Conflict`.

---

## 8. Configuração externa

### 8.1. `JwtProperties` (`br.edu.ifce.security.config.properties`)

```kotlin
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var accessTokenExpiration: Long = 900L,    // 15 min, em segundos
    var refreshExpiration: Long = 3600L,    // 1 h, em segundos
    var cookieSecure: Boolean = true,         // Secure flag do cookie
    var sameSite: String = "Lax"              // SameSite flag do cookie
)
```

| Property | Env var | Default | Unidade | Descrição |
|---|---|---|---|---|
| `jwt.access-token-expiration` | `JWT_ACCESS_TOKEN_EXPIRATION` | `900` | segundos | Vida do access token. |
| `jwt.refresh-expiration` | `JWT_REFRESH_EXPIRATION` | `3600` | segundos | Vida do refresh token **e** do cookie que o contém. |
| `jwt.cookie-secure` | `JWT_COOKIE_SECURE` | `true` | boolean | Se `true`, cookie só é enviado em conexões HTTPS. **Desligar em dev local (HTTP).** |
| `jwt.same-site` | `JWT_COOKIE_SAME_SITE` | `Lax` | string | Política `SameSite` do cookie de refresh token. Valores válidos: `Strict`, `Lax`, `None`. |

### 8.2. `RsaKeyProperties` (`br.edu.ifce.security.config.properties`)

A aplicação lê as chaves RSA diretamente de arquivos `.pem` no boot, parseando PEM (PKCS#8 para chave privada,
X.509 para chave pública) e convertendo para `RSAPublicKey` / `RSAPrivateKey`. O conteúdo **não** é mais injetado
via env var inline.

```kotlin
@ConfigurationProperties(prefix = "rsa")
data class RsaKeyProperties(
    var publicKeyPath: String? = null,
    var privateKeyPath: String? = null,
) {
    val publicKey: RSAPublicKey? by lazy { /* parseia publicKeyPath */ }
    val privateKey: RSAPrivateKey? by lazy { /* parseia privateKeyPath */ }
}
```

| Property | Env var | Default | Descrição |
|---|---|---|---|
| `rsa.public-key-path` | `JWT_PUBLIC_KEY_PATH` | `file:../.keys/public.pem` | Caminho do `.pem` da chave pública. Suporta `file:`, `classpath:` ou caminho relativo/absoluto. |
| `rsa.private-key-path` | `JWT_PRIVATE_KEY_PATH` | `file:../.keys/private_pkcs8.pem` | Caminho do `.pem` da chave privada (formato PKCS#8). |

> **Importante sobre caminhos relativos:** o caminho `../.keys/` é **relativo ao working directory do processo Java**.

A leitura é **lazy** — as chaves só são parseadas na primeira vez que o `JwtEncoder` ou `JwtDecoder` é invocado,
evitando custo desnecessário se a aplicação for usada apenas para endpoints públicos no momento do boot.

### 8.3. `bootstrap.*` (gerido por `BootstrapAdminRunner`)

| Property | Env var | Default | Descrição |
|---|---|---|---|
| `bootstrap.admin-email` | `BOOTSTRAP_ADMIN_EMAIL` | (vazio) | Email do admin institucional. **Obrigatório** — aborta o boot se vazio. |
| `bootstrap.allow-reactivate` | `BOOTSTRAP_ALLOW_REACTIVATE` | `true` | Kill switch geral. Quando `false`, o bootstrap é no-op total. |

### 8.4. `FrontendProperties`

| Property | Env var | Default | Descrição |
|---|---|---|---|
| `frontend.callback-success-url` | `FRONTEND_CALLBACK_SUCCESS_URL` | (vazio — fallback `/callback.html`) | URL de sucesso do OAuth2. |
| `frontend.callback-error-url` | `FRONTEND_CALLBACK_ERROR_URL` | (vazio — fallback `/failure.html`) | URL de erro do OAuth2. |

### 8.5. OAuth2 Google

| Property | Env var | Descrição |
|---|---|---|
| `spring.security.oauth2.client.registration.google.client-id` | `GOOGLE_CLIENT_ID` | Client ID do projeto no Google Cloud Console. |
| `spring.security.oauth2.client.registration.google.client-secret` | `GOOGLE_CLIENT_SECRET` | Client Secret do projeto no Google Cloud Console. |

### 8.6. Cookies de sessão (geridos pelo Spring)

```yaml
server:
  servlet:
    session:
      cookie:
        http-only: true
        secure: true       # override para false em application-dev.yml
        same-site: Lax
```

O cookie de sessão HTTP (gerado durante o `oauth2Login`) é `SameSite=Lax` para permitir o envio no redirect cross-site do callback OAuth2. O cookie de refresh token é independente e usa `SameSite=Lax` (configurável via `JWT_COOKIE_SAME_SITE`). Ambos devem respeitar o ambiente (HTTPS em prod).

---

## 9. Geração de chaves RSA

Para gerar o par RSA usado para assinar e validar o JWT, **gere os arquivos `.pem` e aponte a aplicação para eles**
via `JWT_PUBLIC_KEY_PATH` e `JWT_PRIVATE_KEY_PATH`.

### 9.1. Gerar os arquivos

Linux/macOS:

```bash
mkdir -p ./keys
openssl genrsa -out ./.keys/private.pem 2048
openssl rsa -in ./.keys/private.pem -pubout -out ./.keys/public.pem
openssl pkcs8 -topk8 -in ./.keys/private.pem -out ./.keys/private_pkcs8.pem -nocrypt
```

Windows PowerShell (com `openssl` via Git Bash ou WSL):

```powershell
mkdir keys
& "C:\Program Files\Git\usr\bin\openssl.exe" genrsa -out keys\private.pem 2048
& "C:\Program Files\Git\usr\bin\openssl.exe" rsa -in keys\private.pem -pubout -out keys\public.pem
& "C:\Program Files\Git\usr\bin\openssl.exe" pkcs8 -topk8 -in keys\private.pem -out keys\private_pkcs8.pem -nocrypt
```

### 9.2. Apontar a aplicação para os arquivos

Por padrão, o `application.yml` lê de `./.keys/`:

```yaml
rsa:
  public-key-path: file:./.keys/public.pem
  private-key-path: file:./.keys/private_pkcs8.pem
```

Variantes suportadas via env var (sobrescreve o default):

| Estilo | Exemplo | Quando usar |
|---|---|---|
| `file:` relativo | `file:../.keys/public.pem` | Dev local, container sem path fixo. |
| `file:` absoluto | `file:/etc/catalogo/public.pem` | Produção Linux (Secret montado em volume). |
| Caminho puro | `../.keys/public.pem` | Equivalente a `file:../.keys/public.pem`. |
| `classpath:` | `classpath:.keys/public.pem` | Testes com `src/test/resources/.keys/public.pem`. |

> **Importante sobre caminhos relativos:** o caminho `../.keys/` é **relativo ao working directory do processo Java**.

> **Importante:** os arquivos `*.pem` **nunca** devem ser commitados. Adicione `./.keys/` (ou o diretório
> escolhido) ao `.gitignore`. Em Docker/Kubernetes, monte o diretório como Secret em volume (ver
> [`docs/operacao.md`](./operacao.md#32-apontar-os-caminhos)).

---

## 10. Bootstrap do administrador institucional

`BootstrapAdminRunner` é um `ApplicationRunner` que roda após o `ApplicationContext` estar pronto. Comportamento:

| Cenário | Resultado |
|---|---|
| `BOOTSTRAP_ADMIN_EMAIL` vazia | `IllegalStateException` → boot abortado. |
| Email não existe no banco | Cria `Usuario` com `ROLE_ADMINISTRADOR` + `ROLE_COLABORADOR`. |
| Email existe, ativo, já admin | No-op silencioso. |
| Email existe, ativo, sem admin role | Promove (log `WARN`). |
| Email existe, inativo, `allow-reactivate=true` | Reativa + garante perfis (log `WARN`). |
| Email existe, inativo, `allow-reactivate=false` | `IllegalStateException` → boot abortado. |

**Procedimento recomendado para produção:**

1. Antes do primeiro deploy, defina `BOOTSTRAP_ADMIN_EMAIL=ti@ifce.edu.br` (email do setor de TI).
2. Faça o deploy. O runner cria o usuário e o sistema sobe.
3. O primeiro admin loga via Google usando esse email e usa o sistema normalmente.
4. Admins subsequentes podem ser promovidos via `PATCH /api/usuarios/{id}/perfis` com `ROLE_ADMINISTRADOR`.

---

## 11. Exemplo de payload JWT

Header:

```json
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "..."
}
```

Payload (claims):

```json
{
  "iss": "catalogo-edificacoes-backend",
  "sub": "1",
  "roles": [
    "ROLE_ADMINISTRADOR",
    "ROLE_COLABORADOR"
  ],
  "jti": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "iat": 1717000000,
  "exp": 1717000900
}
```

`sub` é o `Usuario.id`. `roles` é o claim convertido em `GrantedAuthority` pelo `JwtAuthenticationConverter` (com `authorityPrefix=""` para manter o prefixo `ROLE_`).

---

## 12. Tratamento de erros

A API adota duas camadas complementares de tratamento de erros:

1. **Handler global** (`GlobalExceptionHandler` no `common-module`): captura exceções lançadas pelos controllers e use cases da API e devolve um body padronizado `ErroRes` (campos `dataHora`, `status`, `mensagem`).
2. **Tratamento local/framework**: casos tratados diretamente nos controllers (`AuthController.refresh`), pelo Spring Security (401/403 de auth) ou pelo handshake OAuth2 (`CustomOAuth2UserService`, chain 1).

### 12.1. Body padronizado de erro

Todas as respostas de erro tratadas pelo `GlobalExceptionHandler` seguem o formato:

```json
{
  "dataHora": "2026-07-01 14:30:00",
  "status": 400,
  "mensagem": "O nome é obrigatório."
}
```

- `dataHora`: timestamp formatado (`yyyy-MM-dd HH:mm:ss`).
- `status`: código HTTP numérico.
- `mensagem`: mensagem legível da falha (primeira violação de validação, mensagem de negócio ou mensagem genérica para 500).

### 12.2. Cenários tratados pelo `GlobalExceptionHandler` (chain 3 — API)

| Cenário | HTTP Status | Origem |
|---|---|---|
| Body JSON inválido / mal formatado | `400` | `HttpMessageNotReadableException`. |
| Validação de `@RequestBody @Valid` (ex.: `MethodArgumentNotValidException`) | `400` | Mensagem do primeiro campo com erro. |
| Validação de `@PathVariable`/`@RequestParam` com `@Validated` | `400` | `ConstraintViolationException`. |
| Validação de negócio (ex.: "Já existe ambiente com esse nome") | `400` | `IllegalArgumentException` (factories, use cases). |
| Recurso inexistente (ex.: "Ambiente não encontrado") | `404` | `NoSuchElementException`. |
| Usuário inexistente / lockout prevention | `404` / `409` | `ResponseStatusException` (`UsuarioService`). |
| Parâmetro obrigatório ausente | `400` | `MissingServletRequestParameterException`. |
| Violação de constraint do banco | `400` | `DataIntegrityViolationException` (mensagem da causa raiz). |
| Método HTTP não suportado | `405` | `HttpRequestMethodNotSupportedException`. |
| `Content-Type` não suportado | `415` | `HttpMediaTypeNotSupportedException`. |
| Erro inesperado do servidor | `500` | `Exception` (fallback, mensagem genérica). |

### 12.3. Cenários tratados localmente ou pelo framework

| Cenário | HTTP Status | Origem |
|---|---|---|
| Cookie de refresh ausente | `401` | `AuthController.refresh` retorna `ResponseEntity.status(UNAUTHORIZED)` direto (não passa pelo handler). |
| Cookie de refresh inválido/expirado/revogado | `401` | `RefreshTokenService` retorna `null` → `AuthController.refresh` converte. |
| Login com Google de e-mail `@ifce.edu.br` mas inativo | redireciona para `/failure.html` | `CustomOAuth2UserService` lança `OAuth2AuthenticationException` → `.failureUrl()` na chain 1. |
| Login com e-mail externo não pré-cadastrado | redireciona para `/failure.html` | `CustomOAuth2UserService` lança `OAuth2AuthenticationException` → `.failureUrl()` na chain 1. |
| Endpoint protegido sem `Authorization: Bearer <jwt>` | `401` | `oauth2ResourceServer.jwt()` falha (Spring Security, não passa pelo handler). |
| Endpoint protegido com `Authority` insuficiente | `403` | `SecurityConfig` — regra de autoridade (Spring Security). |

> **Nota:** o `GlobalExceptionHandler` é um `@RestControllerAdvice` que atua apenas no `DispatcherServlet` da API (chain 3). As exceções lançadas no handshake OAuth2 (chain 1) e na camada de filtros do Spring Security são tratadas pelo próprio Spring Security, fora do escopo do handler global.
>
> A partir do refactor de segurança, as falhas de negócio no `CustomOAuth2UserService` (domínio não autorizado, usuário inativo, email não fornecido) disparam `OAuth2AuthenticationException`, que o Spring Security captura e redireciona o navegador para a URL configurada em `oauth2.failureUrl()` (default local: `/failure.html`).
