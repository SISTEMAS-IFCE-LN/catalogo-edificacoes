# Operação do Catálogo de Edificações

Este documento cobre os aspectos operacionais do sistema: profiles, comportamento de seeds SQL, geração de chaves RSA e
procedimento de bootstrap em produção.

---

## 1. Profiles

O sistema suporta múltiplos profiles via `spring.profiles.active`. O profile default é `dev` (definido em
`application.yml`).

| Profile | Quando usar                   | Observações                                                                                                                   |
|---------|-------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `dev`   | Desenvolvimento local         | Carrega `data-dev.sql` (120 ambientes fake). `cookie.secure: false`. `BOOTSTRAP_ADMIN_EMAIL` default `dev-admin@ifce.edu.br`. |
| `test`  | Suíte de testes automatizados | `application-test.yml` em `src/test/resources`. H2 in-memory.                                                                 |
| `prod`  | Produção                      | `data.sql` é mínimo (sem seed fake). Exige todas as env vars reais.                                                           |

Para ativar um profile específico:

```bash
export PROFILE_ACTIVE=prod   # Linux/macOS
$env:PROFILE_ACTIVE="prod"  # Windows PowerShell
```

---

## 2. Inicialização SQL

A propriedade `spring.sql.init.mode` controla quando scripts SQL são executados:

| Profile                             | `mode`                    | `data-locations`         | Conteúdo                                            |
|-------------------------------------|---------------------------|--------------------------|-----------------------------------------------------|
| base (default em `application.yml`) | `never`                   | —                        | Nenhum SQL é executado.                             |
| dev                                 | `always`                  | `classpath:data-dev.sql` | 120 ambientes fake para popular o H2 in-memory.     |
| prod                                | `never` (herdado do base) | —                        | Nenhum SQL. Bootstrap é via `BootstrapAdminRunner`. |

**Estrutura física:**

```
apis/main-app/src/main/resources/
├── application.yml
├── application-dev.yml
└── data-dev.sql           # 120 ambientes fake
```

---

## 3. Geração de chaves RSA

A aplicação lê as chaves RSA diretamente de arquivos `.pem` no boot, parseando PEM (PKCS#8 para chave privada,
X.509 para chave pública) e convertendo para `RSAPublicKey` / `RSAPrivateKey`. O conteúdo **não** é mais
injetado via env var inline.

### 3.1. Gerar os arquivos

Linux/macOS:

```bash
mkdir -p ./keys
openssl genrsa -out ./keys/private.pem 2048
openssl rsa -in ./keys/private.pem -pubout -out ./keys/public.pem
openssl pkcs8 -topk8 -in ./keys/private.pem -out ./keys/private_pkcs8.pem -nocrypt
```

Windows PowerShell (com `openssl` via Git Bash ou WSL):

```powershell
mkdir keys
& "C:\Program Files\Git\usr\bin\openssl.exe" genrsa -out keys\private.pem 2048
& "C:\Program Files\Git\usr\bin\openssl.exe" rsa -in keys\private.pem -pubout -out keys\public.pem
& "C:\Program Files\Git\usr\bin\openssl.exe" pkcs8 -topk8 -in keys\private.pem -out keys\private_pkcs8.pem -nocrypt
```

> **Importante:** adicione `./keys/` (ou o diretório escolhido) ao `.gitignore`. Os arquivos `.pem` **nunca** devem
> ser commitados.

### 3.2. Apontar os caminhos

Por padrão, o `application.yml` lê de `./keys/`:

```yaml
rsa:
  public-key-path: file:./keys/public.pem
  private-key-path: file:./keys/private_pkcs8.pem
```

Variantes suportadas via env var (sobrescreve o default):

| Estilo | Exemplo | Quando usar |
|---|---|---|
| `file:` relativo | `file:./keys/public.pem` | Dev local, container sem path fixo. |
| `file:` absoluto | `file:/etc/catalogo/public.pem` | Produção Linux (Secret montado em volume). |
| Caminho puro | `./keys/public.pem` | Equivalente a `file:./keys/public.pem`. |
| `classpath:` | `classpath:keys/public.pem` | Testes com `src/test/resources/keys/public.pem`. |

Em Docker:

```dockerfile
# Dockerfile
RUN mkdir -p /etc/catalogo
COPY --chown=app:app public.pem /etc/catalogo/
COPY --chown=app:app private_pkcs8.pem /etc/catalogo/

ENV JWT_PUBLIC_KEY_PATH=file:/etc/catalogo/public.pem
ENV JWT_PRIVATE_KEY_PATH=file:/etc/catalogo/private_pkcs8.pem
```

Em Kubernetes (montando Secret como volume):

```yaml
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: catalogo
      env:
        - name: JWT_PUBLIC_KEY_PATH
          value: file:/etc/catalogo-secrets/public.pem
        - name: JWT_PRIVATE_KEY_PATH
          value: file:/etc/catalogo-secrets/private_pkcs8.pem
      volumeMounts:
        - name: rsa-keys
          mountPath: /etc/catalogo-secrets
          readOnly: true
  volumes:
    - name: rsa-keys
      secret:
        secretName: catalogo-rsa-keys
```

### 3.3. Migração de ambientes anteriores

Se você já tinha `JWT_PUBLIC_KEY` / `JWT_PRIVATE_KEY` configurados com o conteúdo inline, atualize para
`JWT_PUBLIC_KEY_PATH` / `JWT_PRIVATE_KEY_PATH` apontando para os arquivos `.pem`. O formato antigo não é mais
suportado.

---

## 4. Procedimento de bootstrap em produção

Antes do primeiro deploy, o operador **deve** definir a env var `BOOTSTRAP_ADMIN_EMAIL`. Sem ela, o
`BootstrapAdminRunner` aborta o boot com `IllegalStateException`.

### 4.1. Checklist pré-deploy

- [ ] `BOOTSTRAP_ADMIN_EMAIL` definido para o email institucional do admin.
- [ ] `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET` configurados.
- [ ] `JWT_PUBLIC_KEY_PATH` e `JWT_PRIVATE_KEY_PATH` apontando para os arquivos `.pem` (ver seção 3).
- [ ] `JWT_COOKIE_SECURE=true` (default).
- [ ] HTTPS configurado no reverse proxy / load balancer, com headers `X-Forwarded-Proto` e `X-Forwarded-Host` injetados.
      O `application.yml` já define `server.forward-headers-strategy: framework` para que o Spring respeite esses
      headers na construção do `baseUrl` do template `{baseUrl}/login/oauth2/code/{registrationId}`.
- [ ] Redirect URI resolvido registrado no Google Cloud Console para o ambiente (dev: `http://localhost:8080/login/oauth2/code/google`,
      prod: `https://<domínio>/login/oauth2/code/google`).
- [ ] CORS configurado com origens permitidas (atualmente `*` em dev; ajustar em prod).

### 4.2. Primeiro boot

1. Iniciar a aplicação. O `BootstrapAdminRunner` provisiona o usuário.
2. Verificar no log:

   ```
   Bootstrap admin pré-cadastrado: ti@ifce.edu.br (ROLE_ADMINISTRADOR + ROLE_COLABORADOR).
   ```

3. Acessar `/oauth2/authorization/google` com uma conta Google cujo email é o `BOOTSTRAP_ADMIN_EMAIL`.
4. Após o handshake, o `CustomOAuth2UserService` encontra o usuário criado pelo bootstrap, sincroniza o nome e
   prossegue.
5. O `AuthController.loginSuccess` emite o JWT e o cookie de refresh.

### 4.3. Após o primeiro login

- Promova outros admins institucionais via `PATCH /api/utilizadores/{id}/perfis` (apenas o admin pode).
- Crie usuários externos pré-cadastrados diretamente no banco se necessário.
- Monitore logs do `BootstrapAdminRunner` em reativações inesperadas.

---

## 5. Variáveis de ambiente — resumo

| Env var                       | Default                                  | Obrigatória em prod? | Origem               |
|-------------------------------|------------------------------------------|----------------------|----------------------|
| `PROFILE_ACTIVE`              | `dev`                                    | Não                  | application.yml      |
| `GOOGLE_CLIENT_ID`            | —                                        | Sim                  | Google Cloud Console |
| `GOOGLE_CLIENT_SECRET`        | —                                        | Sim                  | Google Cloud Console |
| `JWT_PUBLIC_KEY_PATH`         | `file:./keys/public.pem`                 | Sim                  | `openssl` + path     |
| `JWT_PRIVATE_KEY_PATH`        | `file:./keys/private_pkcs8.pem`           | Sim                  | `openssl` + path     |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `900`                                    | Não                  | application.yml      |
| `JWT_REFRESH_EXPIRATION`      | `43200`                                  | Não                  | application.yml      |
| `JWT_COOKIE_SECURE`           | `true`                                   | Não                  | application.yml      |
| `BOOTSTRAP_ADMIN_EMAIL`       | (vazio)                                  | **Sim**              | Operador             |
| `BOOTSTRAP_ALLOW_REACTIVATE`  | `true`                                   | Não                  | application.yml      |

Para configurações específicas do Spring (datasource, JPA), ver `main-app/src/main/resources/application.yml`.

---

## 6. Troubleshooting

| Sintoma                                                                                          | Causa provável                                          | Solução                                                                                                 |
|--------------------------------------------------------------------------------------------------|---------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| Boot aborta com `IllegalStateException: BOOTSTRAP_ADMIN_EMAIL não configurado`                   | Env var ausente                                         | Definir `BOOTSTRAP_ADMIN_EMAIL` no ambiente.                                                            |
| Boot aborta com `IllegalStateException: ... allow-reactivate=false`                              | Admin desativado e flag desligada                       | Reativar manualmente no banco ou setar `BOOTSTRAP_ALLOW_REACTIVATE=true` no próximo boot.               |
| `401 Unauthorized` em todos os endpoints                                                         | Chave RSA inválida ou arquivo inacessível               | Verificar `JWT_PUBLIC_KEY_PATH` / `JWT_PRIVATE_KEY_PATH` e permissões dos arquivos `.pem`.             |
| `JwtException: Unable to load RSA key` no boot                                                  | Arquivo `.pem` corrompido ou formato inválido            | Regerar com `openssl`; garantir PKCS#8 para a chave privada.                                           |
| `FileNotFoundException` no boot                                                                  | Path do `.pem` não existe                               | Verificar `JWT_PUBLIC_KEY_PATH` / `JWT_PRIVATE_KEY_PATH`; rodar `ls -la ./keys/` em dev.               |
| `403 Forbidden` em endpoint aparentemente público                                                | `permitAll` não cobre o path                            | Verificar `SecurityConfig.apiFilterChain`.                                                              |
| Cookie de refresh não persiste no navegador em dev                                               | `JWT_COOKIE_SECURE=true` em HTTP                        | `application-dev.yml` define `false`. Verificar se o profile está ativo.                                |
| `UsernameNotFoundException` ao autenticar                                                        | `CustomOAuth2UserService` não provisionou o usuário     | Verificar email termina em `@ifce.edu.br` (auto-provisionamento) ou se foi pré-cadastrado por um admin. |
| Testes do `ambientes-internos-module` falham com `ApplicationContext failure threshold exceeded` | `target/classes` ou `target/test-classes` desatualizado | Rodar `mvn clean install` no `security-module` antes de testar o `ambientes-internos-module`.           |
| Google retorna `redirect_uri_mismatch` no handshake OAuth2                          | Reverse proxy não injeta `X-Forwarded-Proto` / `X-Forwarded-Host`, ou URI do Google Console está incorreto | Verificar config do proxy (nginx/ALB/etc.); conferir que o `redirect_uri` resolvido bate com o registrado no Google Cloud Console. O `application.yml` já define `server.forward-headers-strategy: framework`. |
