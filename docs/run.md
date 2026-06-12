# Como executar a aplicação

Este documento contém as instruções práticas para rodar o Catálogo de Edificações em ambiente de desenvolvimento e validar a suíte de testes.

---

## 1. Pré-requisitos

| Ferramenta | Versão | Verificação |
|---|---|---|
| JDK | 21 (Microsoft, Adoptium, Zulu ou OpenJDK) | `java -version` |
| Maven | 3.9+ (ou usar `mvnw` que vem no projeto) | `mvn -v` |
| Git | qualquer | `git --version` |
| OpenSSL | qualquer (apenas para gerar chaves RSA) | `openssl version` |
| curl ou Postman | qualquer | — |

---

## 2. Setup inicial

### 2.1. Clonar o repositório

```bash
git clone <url-do-repositorio>
cd catalogo-edificacoes
```

### 2.2. Gerar chaves RSA (apenas para executar fora de testes)

A aplicação lê as chaves RSA a partir de arquivos `.pem` no boot, via `ResourceUtils`. Suporta `file:` (filesystem), `classpath:` (resources do JAR) ou caminho relativo/absoluto direto.

#### Gerar os arquivos

Linux/macOS:

```bash
mkdir -p ./keys
openssl genrsa -out ./keys/private.pem 2048
openssl rsa -in ./keys/private.pem -pubout -out ./keys/public.pem
openssl pkcs8 -topk8 -in ./keys/private.pem -out ./keys/private_pkcs8.pem -nocrypt
```

Windows PowerShell (assumindo `openssl` disponível via Git Bash ou WSL):

```powershell
mkdir keys
& "C:\Program Files\Git\usr\bin\openssl.exe" genrsa -out keys\private.pem 2048
& "C:\Program Files\Git\usr\bin\openssl.exe" rsa -in keys\private.pem -pubout -out keys\public.pem
& "C:\Program Files\Git\usr\bin\openssl.exe" pkcs8 -topk8 -in keys\private.pem -out keys\private_pkcs8.pem -nocrypt
```

> **Importante:** adicione `./keys/` ao `.gitignore`. Os arquivos `.pem` **nunca** devem ser commitados.

#### Apontar os caminhos

Por padrão, o `application.yml` aponta para:

```yaml
rsa:
  public-key-path: file:./keys/public.pem
  private-key-path: file:./keys/private_pkcs8.pem
```

**Variantes suportadas:**

| Estilo | Exemplo | Quando usar |
|---|---|---|
| `file:` relativo | `file:./keys/public.pem` | Dev local, container sem path fixo. |
| `file:` absoluto | `file:/etc/catalogo/public.pem` | Produção Linux/Kubernetes (Secret montado em volume). |
| Caminho puro | `./keys/public.pem` | Equivalente a `file:./keys/public.pem`. |
| `classpath:` | `classpath:keys/public.pem` | Testes com `src/test/resources/keys/public.pem`. |

Para sobrescrever via env var:

```bash
export JWT_PUBLIC_KEY_PATH="file:/etc/catalogo/public.pem"
export JWT_PRIVATE_KEY_PATH="file:/etc/catalogo/private_pkcs8.pem"
```

Ou em `.env`:

```bash
JWT_PUBLIC_KEY_PATH=file:./keys/public.pem
JWT_PRIVATE_KEY_PATH=file:./keys/private_pkcs8.pem
```

> **Migração:** se você tinha `JWT_PUBLIC_KEY` / `JWT_PRIVATE_KEY` configurados em ambientes anteriores, atualize para `JWT_PUBLIC_KEY_PATH` / `JWT_PRIVATE_KEY_PATH` apontando para os arquivos `.pem`. A quebra é intencional — passar o conteúdo inline não é mais suportado.

### 2.3. Configurar credenciais do Google (opcional em dev puro)

Para testar o fluxo OAuth2 real, é necessário um projeto no Google Cloud Console com OAuth2 Client configurado. Sem isso, a aplicação sobe mas o login redireciona para uma URL do Google que falha.

```bash
export GOOGLE_CLIENT_ID="xxx.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="xxx"
```

Para **smoke test sem OAuth2** (testar a API com `curl` + JWT gerado manualmente), esta etapa é desnecessária.

### 2.4. Definir o email do admin bootstrap

```bash
export BOOTSTRAP_ADMIN_EMAIL="seu-email@gmail.com"
```

Se o email terminar em `@gmail.com` (ou similar), o `CustomOAuth2UserService` vai recusar o login via Google (precisa `@ifce.edu.br`). Para o **bootstrap admin apenas**, qualquer email serve — ele é apenas um registro no banco, sem autenticação até alguém logar com Google.

Para testes locais sem Google, mantenha o default `dev-admin@ifce.edu.br` (configurado em `application-dev.yml`).

---

## 3. Comandos principais

A partir da raiz do projeto:

```bash
cd apis
```

### 3.1. Compilar tudo

```bash
./mvnw clean install
```

(Linux/macOS) ou:

```powershell
.\mvnw.cmd clean install
```

(Windows). O output esperado em sucesso:

```
[INFO] Reactor Summary for catalogo-edificacoes-parent 0.0.1-SNAPSHOT:
[INFO] catalogo-edificacoes-parent ........................ SUCCESS
[INFO] security-module .................................... SUCCESS
[INFO] ambientes-internos-module .......................... SUCCESS
[INFO] main-app ........................................... SUCCESS
```

### 3.2. Rodar a suíte de testes

Para o reator inteiro:

```bash
./mvnw test
```

Para um módulo específico:

```bash
./mvnw -pl security-module test
./mvnw -pl ambientes-internos-module test
```

Total atual (junho/2026): **170 testes, 0 falhas, 0 erros**.

### 3.3. Iniciar a aplicação em modo dev

```bash
./mvnw -pl main-app spring-boot:run
```

Ou, após `mvn install`:

```bash
java -jar apis/main-app/target/main-app-0.0.1-SNAPSHOT.jar
```

A aplicação sobe em `http://localhost:8080` com o profile `dev` ativo.

### 3.4. Endpoints úteis para smoke test

| Endpoint | Método | Auth? | Descrição |
|---|---|---|---|
| `/health` | GET | Não | Health check. |
| `/actuator/health` | GET | Não | Se actuator estiver habilitado. |
| `/h2-console` | GET (navegador) | Não | Console do H2 in-memory, habilitado em dev. |
| `/oauth2/authorization/google` | GET (navegador) | Não | Inicia o handshake OAuth2 com Google. |
| `/auth/login/success` | POST | Sim (sessão OAuth2) | Chamado pelo `defaultSuccessUrl`; emite JWT. |
| `/auth/refresh` | POST | Cookie | Renova o access token. |
| `/auth/logout` | POST | Cookie | Revoga refresh token e limpa cookie. |
| `/api/ambientes/publicados` | GET | Não | Lista ambientes publicados (público). |
| `/api/ambientes/publicados/{id}` | GET | Bearer | Detalhe de um ambiente publicado. |
| `/api/ambientes/publicados/esquadrias?ids=1,2` | GET | Bearer | Esquadrias de um conjunto. |
| `/api/ambientes/nao-publicados` | GET | Bearer (`GESTOR_SISTEMA`) | Lista não publicados. |
| `/api/ambientes/validacao` | GET | Bearer (`VALIDADOR`) | Lista em validação. |
| `/api/utilizadores/{id}/perfis` | PATCH | Bearer (`ADMINISTRADOR`) | Atualiza perfis. |
| `/api/utilizadores/{id}/desativar` | PATCH | Bearer (`ADMINISTRADOR`) | Desativa usuário. |

### 3.5. Smoke test via Postman

A coleção Postman está em `docs/catalogo-edificacoes.postman_collection.json`. Importar no Postman:

1. Abrir Postman → **File → Import**.
2. Selecionar o arquivo `catalogo-edificacoes.postman_collection.json`.
3. A coleção já contém os endpoints públicos e protegidos.

Para testar endpoints protegidos, é necessário primeiro fazer login via Google e copiar o `accessToken` retornado para o header `Authorization: Bearer <token>`.

---

## 4. Executando módulos isoladamente (raro)

Por padrão, o módulo executável é o `main-app`. Em casos excepcionais (debug profundo), é possível executar testes de um módulo específico sem o `main-app`:

```bash
./mvnw -pl security-module spring-boot:run    # ❌ NÃO FUNCIONA — não tem @SpringBootApplication
./mvnw -pl security-module test                # ✅ funciona
./mvnw -pl ambientes-internos-module test      # ✅ funciona (tem TestApplication próprio)
```

A aplicação **só pode ser executada via `main-app`**, que é o composition root do sistema.

---

## 5. H2 Console (dev)

Quando rodando em profile `dev`, o console do H2 está disponível em:

```
http://localhost:8080/h2
```

Configuração de conexão (preenchida automaticamente):

| Campo | Valor |
|---|---|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:testdb` |
| User Name | `sa` |
| Password | (vazio) |

> **Atenção:** o H2 é **in-memory** e some ao reiniciar a aplicação.

---

## 6. Workflows comuns

### 6.1. Adicionar um novo endpoint protegido

1. Adicionar a regra em `SecurityConfig.apiFilterChain` (`permitAll` ou exigir auth).
2. Adicionar `@PreAuthorize` no controller se aplicável.
3. Adicionar `@WithMockUser(authorities = [...])` nos testes do controller.
4. Adicionar caso de teste em `ambientes-internos-module/src/test/kotlin/.../integracao/`.
5. Adicionar entrada na tabela de mapeamento HTTP em `docs/seguranca.md`.

### 6.2. Adicionar uma nova env var

1. Adicionar property em `JwtProperties`, `RsaKeyProperties` ou em `application.yml` (com `${ENV_VAR:default}`).
2. Documentar em `docs/seguranca.md` (seção 7) e `docs/operacao.md` (seção 5).
3. Adicionar `// TODO` no `application-dev.yml` se o dev precisar de valor local.

### 6.3. Limpar caches e reinstalar

```bash
./mvnw clean
./mvnw -pl security-module -am install
```

Útil quando há mudança de pacotes e o `target/classes` antigo causa `ConflictingBeanDefinitionException`.
