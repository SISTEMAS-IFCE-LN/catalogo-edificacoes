# Operação do Catálogo de Edificações

Este documento cobre os aspectos operacionais do sistema: profiles, comportamento de seeds SQL, geração de chaves RSA,
migrations de schema, backup, Docker Compose e procedimento de bootstrap em produção.

---

## 1. Profiles

O sistema suporta múltiplos profiles via `spring.profiles.active`. O profile default é `dev` (definido em
`application.yml`).

| Profile | Quando usar                   | Observações                                                                                                                                                                                              |
|---------|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `dev`   | Desenvolvimento local         | Carrega `data-dev.sql` (120 ambientes fake). `cookie.secure: false`. `BOOTSTRAP_ADMIN_EMAIL` default `dev-admin@ifce.edu.br`. Banco: H2 in-memory.                                                       |
| `test`  | Suíte de testes automatizados | `application-test.yml` em `src/test/resources`. H2 in-memory.                                                                                                                                            |
| `prod`  | Produção                      | Banco: PostgreSQL. Sem seed fake. Exige todas as env vars reais (incluindo `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`). Schema aplicado por migration Flyway antes da API; `ddl-auto: validate`. |

Para ativar um profile específico:

```bash
export PROFILE_ACTIVE=prod   # Linux/macOS
$env:PROFILE_ACTIVE="prod"  # Windows PowerShell
```

---

## 2. Inicialização SQL

A propriedade `spring.sql.init.mode` controla quando scripts SQL são executados:

| Profile                             | `mode`                    | `data-locations`         | Conteúdo                                                               |
|-------------------------------------|---------------------------|--------------------------|------------------------------------------------------------------------|
| base (default em `application.yml`) | `never`                   | —                        | Nenhum SQL é executado.                                                |
| dev                                 | `always`                  | `classpath:data-dev.sql` | 120 ambientes fake para popular o H2 in-memory.                        |
| prod                                | `never` (herdado do base) | —                        | Nenhum SQL. Schema via Flyway; bootstrap é via `BootstrapAdminRunner`. |

**Estrutura física:**

```
apis/main-app/src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml  # PostgreSQL (ddl-auto: validate)
└── data-dev.sql          # 120 ambientes fake (apenas dev)

db/
├── migration/            # migrations Flyway (V1__baseline.sql, V2__...)
├── backup/               # dumps lógicos (não versionados)
└── backup.sh             # script do serviço backup
```

### 2.1. Migrations de schema

O schema de produção é versionado em `db/migration/` e aplicado pelo serviço `migrate` antes da API. A aplicação não contém Flyway e sobe com `ddl-auto: validate`.

- `backup` roda antes de `migrate` e grava o dump em `db/backup/`.
- `migrate` usa a imagem oficial do Flyway e o bind `./db/migration:/flyway/sql:ro`.
- `api` depende da conclusão bem-sucedida de `migrate`.
- Em dev/test, o H2 usa `create-drop`; as migrations são validadas em PostgreSQL 17.

Procedimentos completos, forward-fix, backup, expand/contract e runbook: [`migrations.md`](./migrations.md).

---

## 3. Geração de chaves RSA

A aplicação lê as chaves RSA diretamente de arquivos `.pem` no boot, parseando PEM (PKCS#8 para chave privada,
X.509 para chave pública) e convertendo para `RSAPublicKey` / `RSAPrivateKey`. O conteúdo **não** é mais
injetado via env var inline.

### 3.1. Gerar os arquivos

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

> **Importante:** adicione `./.keys/` (ou o diretório escolhido) ao `.gitignore`. Os arquivos `.pem` **nunca** devem
> ser commitados.

### 3.2. Apontar os caminhos

Por padrão, o `application.yml` lê de `./.keys/`:

```yaml
rsa:
  public-key-path: file:./.keys/public.pem
  private-key-path: file:./.keys/private_pkcs8.pem
```

Variantes suportadas via env var (sobrescreve o default):

| Estilo           | Exemplo                         | Quando usar                                       |
|------------------|---------------------------------|---------------------------------------------------|
| `file:` relativo | `file:../.keys/public.pem`      | Dev local, container sem path fixo.               |
| `file:` absoluto | `file:/etc/catalogo/public.pem` | Produção Linux (Secret montado em volume).        |
| Caminho puro     | `../.keys/public.pem`           | Equivalente a `file:../.keys/public.pem`.         |
| `classpath:`     | `classpath:.keys/public.pem`    | Testes com `src/test/resources/.keys/public.pem`. |

> **Importante sobre caminhos relativos:** o caminho `../.keys/` é **relativo ao working directory do processo Java**.

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

### 4.1. Checklist de deploy

- [ ] `BOOTSTRAP_ADMIN_EMAIL` definido para o email institucional do admin.
- [ ] `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET` configurados.
- [ ] `JWT_PUBLIC_KEY_PATH` e `JWT_PRIVATE_KEY_PATH` apontando para os arquivos `.pem` (ver seção 3).
- [ ] `JWT_COOKIE_SECURE=true` (default).
- [ ] `JWT_COOKIE_SAME_SITE=Lax` (default).
- [ ] `DB_HOST` configurado para o banco PostgreSQL de produção (ex.: `jdbc:postgresql://host:5432`).
- [ ] `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD` definidos.
- [ ] `db/backup/` existe (crie se necessário) e tem permissão de escrita para o serviço `backup`.
- [ ] HTTPS configurado no reverse proxy / load balancer, com headers `X-Forwarded-Proto` e `X-Forwarded-Host` injetados.
      O `application.yml` já define `server.forward-headers-strategy: framework` para que o Spring respeite esses
      headers na construção do `baseUrl` do template `{baseUrl}/login/oauth2/code/{registrationId}`.
- [ ] Redirect URI resolvido registrado no Google Cloud Console para o ambiente (dev: `http://localhost:8080/login/oauth2/code/google`,
      prod: `https://<domínio>/login/oauth2/code/google`).
- [ ] `CORS_ALLOWED_ORIGINS` definido com as origens cross-origin legítimas (ex.: `https://catalogo.ifce.edu.br`).
      Vazio = nega cross-origin; no modo integrado (Nginx) o SPA é a mesma origem e não passa por CORS.
      Nunca usar `*` com `allowCredentials=true`.

**Após executar o deploy, confirme:**

- [ ] O deploy padrão foi executado (`docker compose rm -f && docker compose up -d --build`).
- [ ] `docker compose ps` mostra `backup` e `migrate` com exit 0.
- [ ] `flyway_schema_history` contém a versão esperada com `success=true`.
- [ ] A API está saudável e subiu com `ddl-auto: validate`.

### 4.2. Primeiro boot

1. Executar o deploy (`docker compose rm -f && docker compose up -d --build`). O `backup` e o `migrate` rodam antes da API; em seguida, o `BootstrapAdminRunner` provisiona o usuário.
2. Verificar no log:

   ```
   Bootstrap admin pré-cadastrado: ti@ifce.edu.br (ROLE_ADMINISTRADOR + ROLE_COLABORADOR).
   ```

3. Acessar `/oauth2/authorization/google` com uma conta Google cujo email é o `BOOTSTRAP_ADMIN_EMAIL`.
4. Após o handshake, o `CustomOAuth2UserService` encontra o usuário criado pelo bootstrap, sincroniza o nome e
   prossegue.
5. O `OAuth2LoginSuccessHandler` emite o JWT e o cookie de refresh, e redireciona o navegador para a URL de callback com o access token no fragmento (`#token=...`).

### 4.3. Após o primeiro login

- Promova outros admins institucionais via `PATCH /api/usuarios/{id}/perfis` (apenas o admin pode).
- Crie usuários externos pré-cadastrados diretamente no banco se necessário.
- Monitore logs do `BootstrapAdminRunner` em reativações inesperadas.

---

## 5. Variáveis de ambiente — resumo

| Env var                         | Default                            | Obrigatória em prod? | Origem               |
|---------------------------------|------------------------------------|----------------------|----------------------|
| `PROFILE_ACTIVE`                | `dev`                              | Não                  | application.yml      |
| `GOOGLE_CLIENT_ID`              | —                                  | Sim                  | Google Cloud Console |
| `GOOGLE_CLIENT_SECRET`          | —                                  | Sim                  | Google Cloud Console |
| `JWT_PUBLIC_KEY_PATH`           | `file:./.keys/public.pem`          | Sim                  | `openssl` + path     |
| `JWT_PRIVATE_KEY_PATH`          | `file:./.keys/private_pkcs8.pem`   | Sim                  | `openssl` + path     |
| `JWT_ACCESS_TOKEN_EXPIRATION`   | `900`                              | Não                  | application.yml      |
| `JWT_REFRESH_EXPIRATION`        | `3600`                             | Não                  | application.yml      |
| `JWT_COOKIE_SECURE`             | `true`                             | Não                  | application.yml      |
| `JWT_COOKIE_SAME_SITE`          | `Lax`                              | Não                  | application.yml      |
| `BOOTSTRAP_ADMIN_EMAIL`         | (vazio)                            | **Sim**              | Operador             |
| `BOOTSTRAP_ALLOW_REACTIVATE`    | `true`                             | Não                  | application.yml      |
| `FRONTEND_CALLBACK_SUCCESS_URL` | (vazio)                            | Não                  | FrontendProperties   |
| `FRONTEND_CALLBACK_ERROR_URL`   | (vazio)                            | Não                  | FrontendProperties   |
| `DB_HOST`                       | `jdbc:postgresql://localhost:5432` | **Sim (prod)**       | Operador             |
| `DB_NAME`                       | `catalogo_edificacoes`             | **Sim (prod)**       | Operador             |
| `DB_USERNAME`                   | `catalogo`                         | **Sim (prod)**       | Operador             |
| `DB_PASSWORD`                   | (vazio)                            | **Sim (prod)**       | Operador             |

### 5.1. Variáveis do job de migration e backup

O serviço `migrate` usa as variáveis abaixo. Com o usuário único, `FLYWAY_USER`/`FLYWAY_PASSWORD` espelham `DB_USERNAME`/`DB_PASSWORD`.

| Env var                      | Default no Compose                     | Obrigatória em prod? | Uso                                                           |
|------------------------------|----------------------------------------|----------------------|---------------------------------------------------------------|
| `FLYWAY_URL`                 | `jdbc:postgresql://db:5432/${DB_NAME}` | **Sim**              | Conexão JDBC do job. Pode apontar para um PostgreSQL externo. |
| `FLYWAY_USER`                | `${DB_USERNAME}`                       | **Sim**              | Usuário do Flyway.                                            |
| `FLYWAY_PASSWORD`            | `${DB_PASSWORD}`                       | **Sim**              | Senha do Flyway.                                              |
| `FLYWAY_LOCATIONS`           | `filesystem:/flyway/sql`               | Não                  | Diretório das migrations no container.                        |
| `FLYWAY_CLEAN_DISABLED`      | `true`                                 | Não                  | Impede `clean` acidental.                                     |
| `FLYWAY_BASELINE_ON_MIGRATE` | `false`                                | Não                  | O banco de produção é criado do zero.                         |
| `FLYWAY_OUT_OF_ORDER`        | `false`                                | Não                  | Aplica migrations na ordem de versão.                         |
| `FLYWAY_CONNECT_RETRIES`     | `60`                                   | Não                  | Tentativas de conexão enquanto o banco inicia.                |

O serviço `backup` usa `PGPASSWORD`, `DB_NAME` e `DB_USERNAME` para executar o `pg_dump`.

Para configurações específicas do Spring (datasource, JPA), ver `main-app/src/main/resources/application.yml`.

---

## 7. Docker Compose

O `docker-compose.yml` da raiz sobe o banco, o backup, a migration, a API e o frontend. O fluxo é integrado: o Compose executa `backup` e `migrate` antes de iniciar a API.

**Serviços e ordem:**

| Ordem | Serviço    | Função                                                                                                    |
|-------|------------|-----------------------------------------------------------------------------------------------------------|
| 1     | `db`       | PostgreSQL 17 com healthcheck e volume `catalogo-data`.                                                   |
| 2     | `backup`   | `pg_dump` one-shot com `postgres:17-trixie` para `db/backup/`; `restart: "no"`; depende de `db` saudável. |
| 3     | `migrate`  | Flyway CLI one-shot; `restart: "no"`; depende de `db` saudável e `backup` concluído.                      |
| 4     | `api`      | API Kotlin/Spring; sem Flyway; `ddl-auto: validate`; depende de `migrate` concluído.                      |
| 5     | `frontend` | Nginx + SPA; depende da API saudável.                                                                     |

**Arquivos envolvidos:**

| Arquivo              | Localização | Função                                                               |
|----------------------|-------------|----------------------------------------------------------------------|
| `docker-compose.yml` | raiz        | Define os serviços `db`, `backup`, `migrate`, `api` e `frontend`.    |
| `.env`               | raiz        | Alimenta a interpolação `${...}` do Compose. **Não commitar.**       |
| `.env.example`       | raiz        | Espelho commitado com placeholders.                                  |
| `apis/Dockerfile`    | `apis/`     | Multi-stage build (Maven 3.9 + distroless java25).                   |
| `apis/.keys/`        | `apis/`     | Bind mount no container (`/app/.keys:ro`) para as chaves RSA.        |
| `db/migration/`      | raiz        | Scripts Flyway (`V1__baseline.sql`, `V2__...`).                      |
| `db/backup/`         | raiz        | Dumps lógicos; não versionado.                                       |
| `db/backup.sh`       | raiz        | Script do serviço `backup` (dump, cópias diária/semanal e retenção). |

**Comando padrão de deploy:**

```bash
docker compose rm -f
docker compose up -d --build
```

- `rm -f` remove containers parados de todos os serviços. `db`, `api` e `frontend`, se estiverem rodando, continuam no ar.
- `up -d --build` recria `backup` e `migrate`, que executam antes da API.
- Se a imagem/config da API mudou, ela é recriada e aguarda o `migrate`.
- Se a API não mudou, ela continua no ar durante a migration; as migrations devem ser backward-compatible (expand/contract).
- Para mudanças destrutivas, use uma janela de manutenção (`rm -sf` ou `down`/`up`).

Em PowerShell 5.1, `&&` não funciona; use `docker compose rm -f; if ($?) { docker compose up -d --build }`.

**Acompanhar logs:**

```bash
docker compose logs -f db
docker compose logs -f backup
docker compose logs -f migrate
docker compose logs -f api
```

**Backup e retenção:**

- `backup` roda antes de `migrate`; a API nova não sobe se o backup falhar.
- `db/backup/rollover/` mantém os 5 backups mais recentes.
- `db/backup/daily/` mantém as 7 cópias diárias mais recentes.
- `db/backup/weekly/` mantém as 4 cópias semanais mais recentes.
- A rotação só ocorre após um `pg_dump` bem-sucedido; dumps parciais não são contados.
- Mantenha `db/backup/` no `.gitignore`.

**Parar e re-inicializar:**

```bash
# parar (preserva o volume catalogo-data)
docker compose down

# parar e apagar o banco (destrutivo)
docker compose down -v
```

**Sobre o volume `catalogo-data`:**

A imagem oficial `postgres:17-trixie` só lê `POSTGRES_USER`/`POSTGRES_DB`/`POSTGRES_PASSWORD` na **primeira inicialização** (quando o data dir está vazio). Em boots subsequentes, essas variáveis são ignoradas. Se o volume foi inicializado com credenciais erradas (ex.: variáveis vazias), o `db` sempre sobe com o usuário default `postgres`. Para corrigir, use `docker compose down -v` para remover o volume e re-inicializar.

**Sobre os logs dos containers:**

Os cinco serviços (`db`, `backup`, `migrate`, `api` e `frontend`) usam o anchor `x-logging` definido no topo do `docker-compose.yml`: driver `json-file` com rotação em `max-size: "10m"` e `max-file: "3"` (até ~30 MB por container). Sem essa configuração, o driver default acumula logs indefinidamente e pode esgotar o disco do host. Para ajustar os limites, edite o anchor `x-logging`. A rotação é aplicada na **criação** do container — após alterar o bloco `logging`, rode `docker compose up -d` para recriar os containers. O `docker compose logs` continua funcionando normalmente.

---

## 8. Troubleshooting

| Sintoma                                                                                          | Causa provável                                                                                             | Solução                                                                                                                                                                                                        |
|--------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `FATAL: role "catalogo" does not exist` no healthcheck do `db`                                   | Volume `catalogo-data` foi inicializado com credenciais diferentes (ex.: variáveis vazias na primeira vez) | `docker compose down -v` remove o volume e re-inicializa com as env vars corretas.                                                                                                                             |
| `The server requested SCRAM-based authentication, but no password was provided` no `api`         | `DB_PASSWORD` está vazio e o Postgres usa SCRAM (default)                                                  | Definir `DB_PASSWORD` não-vazio no `.env` raiz e re-inicializar o volume com `docker compose down -v`.                                                                                                         |
| Boot aborta com `IllegalStateException: BOOTSTRAP_ADMIN_EMAIL não configurado`                   | Env var ausente                                                                                            | Definir `BOOTSTRAP_ADMIN_EMAIL` no ambiente.                                                                                                                                                                   |
| Boot aborta com `IllegalStateException: ... allow-reactivate=false`                              | Admin desativado e flag desligada                                                                          | Reativar manualmente no banco ou setar `BOOTSTRAP_ALLOW_REACTIVATE=true` no próximo boot.                                                                                                                      |
| `401 Unauthorized` em todos os endpoints                                                         | Chave RSA inválida ou arquivo inacessível                                                                  | Verificar `JWT_PUBLIC_KEY_PATH` / `JWT_PRIVATE_KEY_PATH` e permissões dos arquivos `.pem`.                                                                                                                     |
| `JwtException: Unable to load RSA key` no boot                                                   | Arquivo `.pem` corrompido ou formato inválido                                                              | Regerar com `openssl`; garantir PKCS#8 para a chave privada.                                                                                                                                                   |
| `FileNotFoundException` no boot                                                                  | Path do `.pem` não existe                                                                                  | Verificar `JWT_PUBLIC_KEY_PATH` / `JWT_PRIVATE_KEY_PATH`; rodar `ls -la ./.keys/` em dev.                                                                                                                      |
| `403 Forbidden` em endpoint aparentemente público                                                | `permitAll` não cobre o path                                                                               | Verificar `SecurityConfig.apiFilterChain`.                                                                                                                                                                     |
| Cookie de refresh não persiste no navegador em dev                                               | `JWT_COOKIE_SECURE=true` em HTTP                                                                           | `application-dev.yml` define `false`. Verificar se o profile está ativo.                                                                                                                                       |
| `UsernameNotFoundException` ao autenticar                                                        | `CustomOAuth2UserService` não provisionou o usuário                                                        | Verificar email termina em `@ifce.edu.br` (auto-provisionamento) ou se foi pré-cadastrado por um admin.                                                                                                        |
| Testes do `ambientes-internos-module` falham com `ApplicationContext failure threshold exceeded` | `target/classes` ou `target/test-classes` desatualizado                                                    | Rodar `mvn clean install` no `security-module` antes de testar o `ambientes-internos-module`.                                                                                                                  |
| Google retorna `redirect_uri_mismatch` no handshake OAuth2                                       | Reverse proxy não injeta `X-Forwarded-Proto` / `X-Forwarded-Host`, ou URI do Google Console está incorreto | Verificar config do proxy (nginx/ALB/etc.); conferir que o `redirect_uri` resolvido bate com o registrado no Google Cloud Console. O `application.yml` já define `server.forward-headers-strategy: framework`. |
| `migrate` não aplicou a nova migration                                                           | Container one-shot já concluído ou `up` sem `rm -f`                                                        | Rodar `docker compose rm -f && docker compose up -d --build`; conferir `flyway_schema_history`.                                                                                                                |
| `FlywayValidateException` / checksum mismatch                                                    | Migration já aplicada foi editada                                                                          | Restaurar o conteúdo original; nunca editar migration aplicada. Avaliar `repair` com cuidado.                                                                                                                  |
| `success=false` na `flyway_schema_history`                                                       | Migration falhou                                                                                           | Ler `docker compose logs migrate`; limpar objetos parciais; rodar `docker compose run --rm --no-deps migrate repair`; reexecutar.                                                                              |
| `backup` falha; `migrate`/`api` não sobem                                                        | Disco cheio, permissão ou conexão                                                                          | Corrigir `db/backup/`/conexão; reexecutar o deploy.                                                                                                                                                            |
| API falha com erro de `Schema-validation`                                                        | Entidades divergem do schema aplicado                                                                      | Corrigir migration ou mapeamento; validar em PostgreSQL 17.                                                                                                                                                    |
| Volume `catalogo-data` apagado                                                                   | `docker compose down -v`                                                                                   | Restaurar o último dump de `db/backup/`; recriar o volume.                                                                                                                                                     |
