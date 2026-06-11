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

Não há `data.sql` com seed do admin — o provisionamento é responsabilidade do `BootstrapAdminRunner` (
ver [Segurança](./seguranca.md)).

---

## 3. Geração de chaves RSA

Para gerar o par RSA usado para assinar e validar o JWT, ver a
seção [Geração de chaves RSA](./seguranca.md#8-geração-de-chaves-rsa) no documento de segurança.

Resumo:

```bash
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem
openssl pkcs8 -topk8 -in private.pem -out private_pkcs8.pem -nocrypt
```

Em seguida, configure as env vars `JWT_PUBLIC_KEY` e `JWT_PRIVATE_KEY` com o conteúdo dos arquivos (com `\n` literais
preservados).

---

## 4. Procedimento de bootstrap em produção

Antes do primeiro deploy, o operador **deve** definir a env var `BOOTSTRAP_ADMIN_EMAIL`. Sem ela, o
`BootstrapAdminRunner` aborta o boot com `IllegalStateException`.

### 4.1. Checklist pré-deploy

- [ ] `BOOTSTRAP_ADMIN_EMAIL` definido para o email institucional do admin.
- [ ] `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET` configurados.
- [ ] `JWT_PUBLIC_KEY` e `JWT_PRIVATE_KEY` configurados.
- [ ] `JWT_COOKIE_SECURE=true` (default).
- [ ] HTTPS configurado no reverse proxy / load balancer.
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

| Env var                       | Default | Obrigatória em prod? | Origem               |
|-------------------------------|---------|----------------------|----------------------|
| `PROFILE_ACTIVE`              | `dev`   | Não                  | application.yml      |
| `GOOGLE_CLIENT_ID`            | —       | Sim                  | Google Cloud Console |
| `GOOGLE_CLIENT_SECRET`        | —       | Sim                  | Google Cloud Console |
| `JWT_PUBLIC_KEY`              | —       | Sim                  | `openssl`            |
| `JWT_PRIVATE_KEY`             | —       | Sim                  | `openssl`            |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `900`   | Não                  | application.yml      |
| `JWT_REFRESH_EXPIRATION`      | `43200` | Não                  | application.yml      |
| `JWT_COOKIE_SECURE`           | `true`  | Não                  | application.yml      |
| `BOOTSTRAP_ADMIN_EMAIL`       | (vazio) | **Sim**              | Operador             |
| `BOOTSTRAP_ALLOW_REACTIVATE`  | `true`  | Não                  | application.yml      |

Para configurações específicas do Spring (datasource, JPA), ver `main-app/src/main/resources/application.yml`.

---

## 6. Troubleshooting

| Sintoma                                                                                          | Causa provável                                          | Solução                                                                                                 |
|--------------------------------------------------------------------------------------------------|---------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| Boot aborta com `IllegalStateException: BOOTSTRAP_ADMIN_EMAIL não configurado`                   | Env var ausente                                         | Definir `BOOTSTRAP_ADMIN_EMAIL` no ambiente.                                                            |
| Boot aborta com `IllegalStateException: ... allow-reactivate=false`                              | Admin desativado e flag desligada                       | Reativar manualmente no banco ou setar `BOOTSTRAP_ALLOW_REACTIVATE=true` no próximo boot.               |
| `401 Unauthorized` em todos os endpoints                                                         | Chave RSA inválida ou ausente                           | Verificar `JWT_PUBLIC_KEY` / `JWT_PRIVATE_KEY`.                                                         |
| `403 Forbidden` em endpoint aparentemente público                                                | `permitAll` não cobre o path                            | Verificar `SecurityConfig.apiFilterChain`.                                                              |
| Cookie de refresh não persiste no navegador em dev                                               | `JWT_COOKIE_SECURE=true` em HTTP                        | `application-dev.yml` define `false`. Verificar se o profile está ativo.                                |
| `UsernameNotFoundException` ao autenticar                                                        | `CustomOAuth2UserService` não provisionou o usuário     | Verificar email termina em `@ifce.edu.br` (auto-provisionamento) ou se foi pré-cadastrado por um admin. |
| Testes do `ambientes-internos-module` falham com `ApplicationContext failure threshold exceeded` | `target/classes` ou `target/test-classes` desatualizado | Rodar `mvn clean install` no `security-module` antes de testar o `ambientes-internos-module`.           |
