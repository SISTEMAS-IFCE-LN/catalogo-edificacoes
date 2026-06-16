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
| `OAuth2LoginSuccessHandler` | `br.edu.ifce.security.config` | Gera JWT + refresh token diretamente no callback OAuth2 e retorna JSON na response. |
| `AuthController` | `br.edu.ifce.security.controller` | Adaptador HTTP para refresh e logout. |
| `UsuarioService` | `br.edu.ifce.security.model.application.service` | Gestão de perfis e desativação (com lockout prevention). |
| `UsuarioController` | `br.edu.ifce.security.controller` | Endpoints administrativos de perfis. |
| `SecurityConfig` | `br.edu.ifce.security.config` | `SecurityFilterChain`, `oauth2Login`, `oauth2ResourceServer.jwt()`, CORS. |
| `JwtConfig` | `br.edu.ifce.security.config` | Beans `JwtEncoder` e `JwtDecoder` (RSA). |
| `RsaKeyProperties` | `br.edu.ifce.security.config` | Bind de `rsa.public-key` / `rsa.private-key`. |
| `JwtProperties` | `br.edu.ifce.security.config` | Bind de `jwt.access-token-expiration` / `jwt.refresh-expiration` / `jwt.cookie-secure`. |
| `BootstrapAdminRunner` | `br.edu.ifce.security.config` | Garante a presença de um administrador institucional conhecido no boot. |

---

## 2. Fluxo de autenticação OAuth2 + JWT

```
┌────────┐         ┌──────────┐         ┌────────┐        ┌──────────┐
│  SPA   │         │ Backend  │         │ Google │        │  Banco   │
└───┬────┘         └────┬─────┘         └───┬────┘        └────┬─────┘
    │  GET /oauth2/    │                   │                  │
    │  authorization/  │                   │                  │
    │  google          │                   │                  │
    ├─────────────────►│                   │                  │
    │                  │  302 → accounts   │                  │
    │                  │  .google.com/...  │                  │
    │◄─────────────────┤                   │                  │
    │                                       │                  │
    │  [ usuário autentica no Google ]      │                  │
    │                                       │                  │
    │  302 → /login/oauth2/code/google       │                  │
    │  ?code=...&state=...                 │                  │
    ├──────────────────►│                   │                  │
    │                  │  troca code por   │                  │
    │                  │  access_token     │                  │
    │                  ├──────────────────►│                  │
    │                  │◄──────────────────┤                  │
    │                  │  userinfo (id,    │                  │
    │                  │  email, name)     │                  │
    │                  ├──────────────────►│                  │
    │                  │◄──────────────────┤                  │
    │                  │                                       │
    │                  │  CustomOAuth2UserService:           │
    │                  │  - email termina em @ifce.edu.br?    │
    │                  │  - provisiona Usuario (se novo)      │
    │                  │  - atribui ROLE_COLABORADOR          │
    │                  │  - sincroniza nome (se divergente)   │
    │                  ├──────────────────────────────────────►
    │                  │◄──────────────────────────────────────┤
    │                  │                                       │
    │                  │  OAuth2LoginSuccessHandler:          │
    │                  │  - gera JWT (15 min)                 │
    │                  │  - gera refresh token (12 h)         │
    │                  │  - persiste refresh (revoga antigos) │
    │                  │  - retorna JSON com access token     │
    │                  │  - seta cookie HttpOnly refreshToken │
    │◄─────────────────┤                                       │
    │  { accessToken }  │                                       │
    │  Set-Cookie:      │                                       │
    │   refreshToken=.. │                                       │
```

### Pontos de atenção

- O `oauth2Login` requer `SessionCreationPolicy.IF_REQUIRED` no `SecurityConfig` (chain 1) para armazenar temporariamente o `Authentication` durante o handshake.
- A API em si opera com `SessionCreationPolicy.STATELESS` (chain 2) e valida o JWT a cada requisição.
- O cookie de sessão HTTP é `SameSite=Lax` (necessário para o callback cross-site do OAuth2).
- O cookie `refreshToken` é `HttpOnly`, `Secure` (configurável via `JWT_COOKIE_SECURE`), `SameSite=Strict`, `path=/`.
- O `OAuth2LoginSuccessHandler` gera os tokens diretamente no callback OAuth2 (dentro da chain 1, onde o `Authentication` está disponível) e retorna JSON na response, eliminando a necessidade de um endpoint `POST /auth/login/success` separado.

---

## 3. Fluxo de refresh (com rotação)

```
┌────────┐                  ┌──────────┐                  ┌────────┐
│  SPA   │                  │ Backend  │                  │  Banco │
└───┬────┘                  └────┬─────┘                  └────┬───┘
    │  access token expirou      │                              │
    │  POST /auth/refresh         │                              │
    │  (cookie refreshToken)      │                              │
    ├───────────────────────────►│                              │
    │                            │  RefreshTokenService:        │
    │                            │  - buscarParaRotacao()       │
    │                            ├─────────────────────────────►│
    │                            │◄─────────────────────────────┤
    │                            │  - se válido: gerar novo    │
    │                            │    access token + revogar    │
    │                            │    antigo + criar novo      │
    │                            ├─────────────────────────────►│
    │                            │  200 OK                      │
    │                            │  Set-Cookie: refreshToken=.. │
    │                            │  Body: { accessToken }      │
    │◄───────────────────────────┤                              │
    │  { accessToken }            │                              │
    │  Set-Cookie: refreshToken=.. │                             │
```

**Importante:** a cada refresh bem-sucedido, o refresh token antigo é **revogado** e um novo é emitido (rotação). Tokens revogados não podem ser reutilizados.

---

## 4. Perfis e hierarquia cumulativa

Quatro perfis, armazenados no campo `perfis` da tabela `usuario_perfis` como `Set<Perfil>`:

| Perfil | Permissões representadas |
|---|---|
| `ROLE_COLABORADOR` | Listar e consultar ambientes publicados. **Obrigatório para todos os usuários.** |
| `ROLE_VALIDADOR` | Listar e gerenciar ambientes em validação (publicar, privar). |
| `ROLE_GESTOR_SISTEMA` | CRUD de ambientes não publicados; submeter para validação. |
| `ROLE_ADMINISTRADOR` | Gerir perfis de outros usuários; desativar contas. |

**Regra universal:** o `UsuarioService.atualizarPerfis` sempre **adiciona** `ROLE_COLABORADOR` ao set final, garantindo que nenhum usuário perca o acesso mínimo.

---

## 5. Lockout prevention

`UsuarioService.verificarExclusaoAdm()` é invocado por `atualizarPerfis` e `desativarUsuario` para impedir que o sistema fique sem administrador ativo.

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
- `PATCH /api/utilizadores/{id}/perfis` removendo `ROLE_ADMINISTRADOR` do último admin ativo.
- `PATCH /api/utilizadores/{id}/desativar` quando o usuário é o último admin ativo.

---

## 6. Mapeamento de endpoints HTTP

Definido em `SecurityConfig.apiFilterChain` (chain 2, com `@Order(2)`).

### 6.1. Endpoints públicos (`permitAll`)

| Path | Método | Observação |
|---|---|---|
| `/api/ambientes/publicados/**` | todos | Listagens e detalhes de ambientes publicados. |
| `/auth/**` | todos | Refresh, logout. |
| `/health` | todos | Health check. |
| `/oauth2/**` | todos | Handshake OAuth2 (chain 1, com `IF_REQUIRED`). |
| `/login/**` | todos | Handshake OAuth2. |

### 6.2. Endpoints protegidos por `@PreAuthorize`

| Path base | Classe | Authority exigida |
|---|---|---|
| `/api/ambientes/nao-publicados/**` | `AmbienteNaoPublicadoController` | `ROLE_GESTOR_SISTEMA` |
| `/api/ambientes/validacao/**` | `AmbienteValidacaoController` | `ROLE_VALIDADOR` |
| `/api/ambientes/{qualquer}/{id}` (GET) | `BaseController.obterAmbientePorId` | `ROLE_COLABORADOR` |
| `/api/ambientes/publicados/esquadrias` (GET) | `AmbientePublicadoController.listarEsquadriasAmbientes` | `ROLE_COLABORADOR` |
| `/api/utilizadores/**` | `UsuarioController` | `ROLE_ADMINISTRADOR` |

### 6.3. Resposta a acessos não autorizados

- Sem `Authentication` em endpoint protegido → `401 Unauthorized`.
- `Authentication` presente mas sem `Authority` exigida → `403 Forbidden`.
- Lockout prevention violado → `409 Conflict`.

---

## 7. Configuração externa

### 7.1. `JwtProperties` (`br.edu.ifce.security.config`)

```kotlin
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var accessTokenExpiration: Long = 900L,    // 15 min, em segundos
    var refreshExpiration: Long = 43200L,    // 12 h, em segundos
    var cookieSecure: Boolean = true         // Secure flag do cookie
)
```

| Property | Env var | Default | Unidade | Descrição |
|---|---|---|---|---|
| `jwt.access-token-expiration` | `JWT_ACCESS_TOKEN_EXPIRATION` | `900` | segundos | Vida do access token. |
| `jwt.refresh-expiration` | `JWT_REFRESH_EXPIRATION` | `43200` | segundos | Vida do refresh token **e** do cookie que o contém. |
| `jwt.cookie-secure` | `JWT_COOKIE_SECURE` | `true` | boolean | Se `true`, cookie só é enviado em conexões HTTPS. **Desligar em dev local (HTTP).** |

### 7.2. `RsaKeyProperties` (`br.edu.ifce.security.config`)

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

### 7.3. `bootstrap.*` (gerido por `BootstrapAdminRunner`)

| Property | Env var | Default | Descrição |
|---|---|---|---|
| `bootstrap.admin-email` | `BOOTSTRAP_ADMIN_EMAIL` | (vazio) | Email do admin institucional. **Obrigatório** — aborta o boot se vazio. |
| `bootstrap.allow-reactivate` | `BOOTSTRAP_ALLOW_REACTIVATE` | `true` | Kill switch geral. Quando `false`, o bootstrap é no-op total. |

### 7.4. OAuth2 Google

| Property | Env var | Descrição |
|---|---|---|
| `spring.security.oauth2.client.registration.google.client-id` | `GOOGLE_CLIENT_ID` | Client ID do projeto no Google Cloud Console. |
| `spring.security.oauth2.client.registration.google.client-secret` | `GOOGLE_CLIENT_SECRET` | Client Secret do projeto no Google Cloud Console. |

### 7.5. Cookies de sessão (geridos pelo Spring)

```yaml
server:
  servlet:
    session:
      cookie:
        http-only: true
        secure: true       # override para false em application-dev.yml
        same-site: lax
```

O cookie de sessão HTTP (gerado durante o `oauth2Login`) é `SameSite=Lax` para permitir o envio no redirect cross-site do callback OAuth2. O cookie de refresh token é independente e usa `SameSite=Strict`. Ambos devem respeitar o ambiente (HTTPS em prod).

---

## 8. Geração de chaves RSA

Para gerar o par RSA usado para assinar e validar o JWT, **gere os arquivos `.pem` e aponte a aplicação para eles**
via `JWT_PUBLIC_KEY_PATH` e `JWT_PRIVATE_KEY_PATH`.

### 8.1. Gerar os arquivos

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

### 8.2. Apontar a aplicação para os arquivos

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

## 9. Bootstrap do administrador institucional

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
4. Admins subsequentes podem ser promovidos via `PATCH /api/utilizadores/{id}/perfis` com `ROLE_ADMINISTRADOR`.

---

## 10. Exemplo de payload JWT

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
  "sub": "1",
  "email": "ti@ifce.edu.br",
  "roles": [
    "ROLE_ADMINISTRADOR",
    "ROLE_COLABORADOR"
  ],
  "iat": 1717000000,
  "exp": 1717000900
}
```

`sub` é o `Usuario.id`. `roles` é o claim convertido em `GrantedAuthority` pelo `JwtAuthenticationConverter` (com `authorityPrefix=""` para manter o prefixo `ROLE_`).

---

## 11. Tratamento de erros

| Cenário | HTTP Status | Origem |
|---|---|---|
| Cookie de refresh ausente | `401` | `AuthController.refresh` retorna `ResponseEntity.status(UNAUTHORIZED)`. |
| Cookie de refresh inválido/expirado/revogado | `401` | `RefreshTokenService.buscarParaRotacao` retorna `null`. |
| Login com Google de e-mail `@ifce.edu.br` mas inativo | `403` | `CustomOAuth2UserService` lança `ResponseStatusException`. |
| Login com e-mail externo não pré-cadastrado | `403` | `CustomOAuth2UserService` lança `ResponseStatusException`. |
| Lockout prevention (remover/desativar último admin) | `409` | `UsuarioService.verificarExclusaoAdm` lança `ResponseStatusException`. |
| Atualizar perfis de usuário inexistente | `404` | `UsuarioService.atualizarPerfis` lança `ResponseStatusException`. |
| Endpoint protegido sem `Authorization: Bearer <jwt>` | `401` | `oauth2ResourceServer.jwt()` falha. |
| Endpoint protegido com `Authority` insuficiente | `403` | `@PreAuthorize` falha. |
