# Arquitetura do Sistema - Catálogo de Edificações

Este documento descreve a organização arquitetural do ecossistema do Catálogo de Edificações. O sistema adota uma *
*Arquitetura Modular (Multi-Módulos)** fundamentada nos princípios de separação de conceitos, alta coesão e baixo
acoplamento.

---

## 1. Abordagem Modular e Divisão de Responsabilidades

O ecossistema é segmentado em unidades independentes de compilação (módulos), permitindo que a infraestrutura, a
segurança e as diferentes regras de negócio de domínio coexistam sem gerar dependências cíclicas ou acoplamento rígido.

```
                   ┌─────────────────────────┐
                   │        main-app         │
                   │   (Ponto de Entrada)    │
                   └────────────┬────────────┘
                                │ (Agrega)
              ┌─────────────────┴───────────────────────┐
              ▼                                         ▼
┌─────────────────────────┐         ┌────────────────────────────────────┐
│     security-module     │         │     ambientes-internos-module      │
│  (Identidade e Acesso)  │         │   (Domínio de Ambientes Internos)  │
└─────────────────────────┘         └────────────────────────────────────┘
┌─────────────────────────┐
│     common-module       │
│  (Utilitários shared)   │
└─────────────────────────┘
```

### 1.1. Módulo de Inicialização e Orquestração (`main-app`)

* **Papel:** Atua como a raiz de composição (*Composition Root*) do sistema — o único módulo executável.
* **Responsabilidade:** Este módulo não possui regras de negócio ou de segurança corporativa. Sua função é:
    * Consolidar `security-module`, `common-module` e `ambientes-internos-module` em uma única unidade executável final.
    * Conter a classe `CatalogoEdificacoesApp` anotada com
      `@SpringBootApplication(scanBasePackages = ["br.edu.ifce.ambientes_internos", "br.edu.ifce.security"])`.
    * Centralizar `application.yml`, `application-dev.yml` e `data-dev.sql` (carga de 120 ambientes fake para dev).
    * Definir as propriedades globais de OAuth2 (Google), das chaves RSA para JWT, das configurações de cookie
      (`HttpOnly`, `Secure`, `SameSite`) e das regras de CORS para SPA/mobile.
    * Expor o pacote executável de entrada do sistema (ponto único de inicialização).

#### 1.1.1. Módulo de Utilitários Compartilhados (`common-module`)

* **Papel:** Concentra utilitários e abstrações compartilhadas entre os módulos de domínio.
* **Responsabilidade:** Fornece tipos, validações e helpers comuns que evitam duplicação entre `security-module` e `ambientes-internos-module`. É declarado no `<modules>` do parent e listado no `<dependencyManagement>` (`apis/pom.xml:34-37`).
* **Observação:** Não define regras de negócio nem infraestrutura de segurança/identidade.

### 1.2. Módulo de Segurança (`security-module`)

* **Papel:** Centraliza o controle de acessos, autenticação de identidades e gestão de perfis.
* **Responsabilidade:** É o encarregado de interceptar as requisições HTTP recebidas, verificar as permissões
  declaradas e isolar toda a infraestrutura de identidade. Concentra, em especial:
    * Autenticação **OAuth2 (Google)** via Authorization Code com PKCE, com o backend atuando como *broker*.
    * `OAuth2LoginSuccessHandler`: handler que emite JWT + refresh token diretamente no callback OAuth2 (dentro da
      chain 1, onde o `Authentication` está disponível) e redireciona o navegador para a URL de callback configurada,
      anexando o access token no fragmento (`#token=...`).
    * `JwtConfig`: classe de configuração que define os beans `JwtEncoder` e `JwtDecoder` a partir das chaves RSA,
      isolando-os do `SecurityConfig` para evitar ciclos de dependência.
    * Emissão e validação de **JWT próprio** (assinatura RSA) para acesso à API.
    * Gestão de **refresh tokens** persistidos, entregues em cookie `HttpOnly`, `Secure` e
      `SameSite` configurável (default `Lax`).
    * Entidade `Usuario` e enum `Perfil` (`ROLE_COLABORADOR`, `ROLE_VALIDADOR`, `ROLE_GESTOR_SISTEMA`,
      `ROLE_ADMINISTRADOR`), com suporte a **múltiplos perfis cumulativos** e regra de **lockout prevention**
      (RN-4.9).
    * `BootstrapAdminRunner`: garante a presença de um administrador institucional conhecido no boot, lendo a env
      var `BOOTSTRAP_ADMIN_EMAIL`. Aborta o boot se a env var não estiver configurada.
    * Subpacotes internos: `model.{domain, repository, application.{interfaces, service}}` e `config`/`controller`.
    * Configuração do `SecurityFilterChain` com `oauth2Login` (sessão `IF_REQUIRED` para o handshake) e
      `oauth2ResourceServer.jwt()` (sessão `STATELESS` para a API).
* Os demais módulos de domínio recorrem a este componente apenas para validar as credenciais anexadas às requisições,
  mantendo seus códigos limpos de lógicas de infraestrutura de segurança.

### 1.3. Módulo de Domínio de Ambientes Internos (`ambientes-internos-module`)

* **Papel:** Encapsula as regras *core* e as operações de negócio do subdomínio de ambientes internos.
* **Responsabilidade:** Contém as entidades de domínio, os casos de uso e as interfaces de persistência do
  subdomínio de ambientes. Este módulo:
    * Concentra **apenas** o domínio de ambientes internos — sem conter lógica de autenticação ou autorização.
    * Recebe a segurança de forma transversal, por composição em `main-app` e por regras centralizadas no
      `SecurityConfig` do `security-module`, sem acoplar a este módulo a infraestrutura de identidade.
* A arquitetura multi-módulos permite que novos subdomínios (áreas externas, relatórios avançados, etc.) sejam
  acoplados como novos módulos irmãos, compartilhando o mesmo ecossistema de segurança sem impactar os recursos já
  existentes.

### 1.4. Impacto na suíte de testes com a introdução da segurança

A transição para a arquitetura modular e a introdução do `security-module` produzem efeitos diretos na estratégia
de testes do projeto.

* **Testes unitários e JPA do domínio** permanecem válidos após a separação em módulos, exigindo apenas ajustes
  mínimos de caminho/imports quando a nova estrutura assim o requerer.
* **Testes de integração de controllers** passaram a considerar a segurança adicionada. As classes de teste de
  controller agora usam `@WithMockUser(authorities = [...])` em nível de classe para simular a autenticação, e
  `@Import(TestSecurityConfig::class)` para carregar uma config de segurança minimalista (gera chaves RSA em memória)
  sem depender de todo o `SecurityConfig` real.
* **Testes que validam erros de validação por parâmetros** continuam cobrindo `400 Bad Request`, mas apenas depois
  de satisfazer os requisitos de autenticação quando o endpoint estiver protegido.
* **Endpoints públicos** (notadamente as listagens e detalhes de ambientes publicados — ver RN-4.4) continuam
  sendo testados sem autenticação, para garantir que a regra de acesso público não se torne regressiva.
* O `TestApplication` do `ambientes-internos-module` foi atualizado para escanear tanto `br.edu.ifce.ambientes_internos`
  quanto `br.edu.ifce.security` (entidades, repositórios JPA, services) **mas exclui** os componentes de runtime do
  `security-module` (controllers, services de auth, `BootstrapAdminRunner`, properties) que não fazem sentido em
  testes unitários do domínio.

### 1.5. Bootstrap do administrador institucional

`BootstrapAdminRunner` (em `br.edu.ifce.security.config`) é um `ApplicationRunner` que roda após o JPA criar o
schema. Seu comportamento está documentado em [`docs/seguranca.md`](./seguranca.md#9-bootstrap-do-administrador-institucional) e [`docs/operacao.md`](./operacao.md#4-procedimento-de-bootstrap-em-produção).

Em resumo:

- Lê `BOOTSTRAP_ADMIN_EMAIL` (obrigatória — aborta o boot se vazia).
- Idempotente: cria/atualiza o usuário, atribui `ROLE_ADMINISTRADOR` + `ROLE_COLABORADOR`.
- Suporta reativação (flag `BOOTSTRAP_ALLOW_REACTIVATE`, default `true`).

### 1.6. Mapeamento de endpoints HTTP

Definido em `SecurityConfig.apiFilterChain` (chain 2, com `@Order(2)`). Endpoints públicos (`permitAll`):

| Path                           | Descrição                                      |
|--------------------------------|------------------------------------------------|
| `/api/ambientes/publicados/**` | Listagens e detalhes de ambientes publicados.  |
| `/auth/**`                     | Refresh, logout.                               |
| `/health`                      | Health check.                                  |
| `/oauth2/**` e `/login/**`     | Handshake OAuth2 (chain 1, com `IF_REQUIRED`). |

Endpoints protegidos por `SecurityConfig`:

| Path                                         | Authority                          |
|----------------------------------------------|------------------------------------|
| `/api/ambientes/nao-publicados/**`           | `ROLE_GESTOR_SISTEMA`              |
| `/api/ambientes/validacao/**`                | `ROLE_VALIDADOR`                   |
| `/api/ambientes/publicados/{id}` (GET)       | `ROLE_COLABORADOR`                 |
| `/api/ambientes/publicados/esquadrias` (GET) | `ROLE_COLABORADOR`                 |
| `/api/usuarios/**`                           | `ROLE_ADMINISTRADOR`               |

Detalhamento completo em [`docs/seguranca.md`](./seguranca.md).

### 1.7. Configuração centralizada de tokens

`JwtProperties` (em `br.edu.ifce.security.config.properties`) agrupa tempos e flags de cookie em uma única classe
configurável externamente:

| Property                      | Env var                       | Default        |
|-------------------------------|-------------------------------|----------------|
| `jwt.access-token-expiration` | `JWT_ACCESS_TOKEN_EXPIRATION` | `900` (15 min) |
| `jwt.refresh-expiration`      | `JWT_REFRESH_EXPIRATION`      | `3600` (1 h) |
| `jwt.cookie-secure`           | `JWT_COOKIE_SECURE`           | `true`         |
| `jwt.same-site`               | `JWT_COOKIE_SAME_SITE`        | `Lax`          |

`accessTokenExpiration` é lido pelo `JwtService`/`LoginRes` para a expiração do token e pelo `CookieService` para o cookie de refresh (via `refreshExpiration`). `refreshExpiration` é lido pelo `RefreshTokenService` para a expiração do token persistido. O `maxAge` do cookie de refresh é derivado do mesmo valor, garantindo coerência entre token e cookie. As URLs de callback do frontend são configuradas via `FrontendProperties`.

---

## 2. Padrão Arquitetural Interno das Camadas

Dentro de cada módulo de negócio, adota-se uma divisão interna por camadas funcionais para garantir a testabilidade e a
evolução independente do código. O `security-module` segue o mesmo padrão, com a diferença de que a camada
de serviço lida com autenticação, tokens e gestão de identidade em vez de regras de domínio:

1. **Camada de Exposição (Controladores REST):** Responsável por receber as requisições externas, realizar validações
   sintáticas primárias de entrada e formatar os dados de resposta para o cliente.
2. **Camada de Aplicação (Casos de Uso / Serviços):** Onde reside a lógica de negócio e a coordenação das operações. É
   nesta camada que as regras operacionais são processadas de forma isolada de preocupações de rede ou banco de dados.
   No `security-module`, esta camada lida com orquestração de login, refresh, gestão de perfis e lockout prevention.
3. **Camada de Acesso a Dados (Repositórios):** Interfaces responsáveis pela abstração dos mecanismos de persistência e
   consultas ao banco de dados.
4. **Camada de Domínio (Entidades):** Representação pura dos modelos conceituais do sistema, contendo seus atributos e
   comportamentos fundamentais.
