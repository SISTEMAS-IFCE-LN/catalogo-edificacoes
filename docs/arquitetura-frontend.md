# Arquitetura Frontend — Catálogo de Edificações

Este documento descreve a arquitetura do frontend do sistema Catálogo de Edificações. É a referência autoritativa para decisões de estrutura, segurança, roteamento, estado e deploy do frontend. Para o fluxo de autenticação, perfis e endpoints, ver [`seguranca.md`](./seguranca.md). Para os casos de uso do frontend, ver [`ambientes-internos/casos-uso-frontend.md`](./ambientes-internos/casos-uso-frontend.md).

---

## 1. Sumário das decisões

| Tema | Decisão | Motivação                                                                                                                                                 |
|---|---|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Access token | **Em memória** (variável de módulo, não persiste em storage) | Mais seguro contra ataque XSS; alinhado ao design do backend, que entrega o refresh token em cookie `HttpOnly` exatamente para suportar bootstrap via refresh |
| Refresh token | Cookie `HttpOnly + Secure + SameSite=Lax` gerido pelo backend | Frontend nunca o manipula; enviado automaticamente em `/auth/refresh`                                                                                     |
| CSRF | Tratado em `/auth/**` via Double Submit Cookie (`XSRF-TOKEN` / `X-XSRF-TOKEN`) | Backend já implementa `CookieCsrfTokenRepository.withHttpOnlyFalse()` em `authFilterChain` (`@Order(2)`)                                                  |
| Dados do `Usuario` | `GET /api/usuarios/me` (implementado no backend) | JWT não expõe `nome`/`ativo`/`criadoEm`; endpoint necessário para popular `User`. Frontend não decodifica o JWT — apenas o transporta em `Authorization` |
| Renderização | **CSR puro (SPA)** | Adequado ao JWT entregue em fragmento de URL (`#token=...`, inacessível ao servidor) e aos UCs interativos (multistep, tabelas, seleção múltipla)                 |
| Framework | **Vite + React Router v8** | Mínimo necessário para CSR; hot reload rápido; build estático simples; guarda de rotas via `<RequireAuth>`/`<RequireRole>`                                |
| Estilização | **Tailwind CSS 4 + shadcn/ui** | Plugin Vite nativo (`@tailwindcss/vite`), tema via `@theme` em CSS; acessível (Base UI), customizável, alinhado ao padrão declarativo de permissões |
| Estado servidor/UI | TanStack Query v5 (server) + Context (auth) | Separação clara; cache inteligente; auth isolado                                                                                                          |
| HTTP Client | **Axios** com interceptores (auth, CSRF, refresh) | Tratamento central de 401, fila de refresh, anexação de `X-XSRF-TOKEN`                                                                                    |
| Formulários | React Hook Form + Zod | Schemas compartilhados com backend; validação runtime + type inference                                                                                    |
| Deploy | **Nginx** servindo `dist/` + reverse proxy para backend Spring | Reaproveita reverse proxy já exigido por `X-Forwarded-*` (ver [`operacao.md`](./operacao.md)); menor nº de peças operacionais                             |
| Responsividade | **Mobile-first**, Tailwind + shadcn `Drawer`/`Sheet`/`Avatar` | App acessível em desktop e celular; tabelas viram cards, modais viram bottom-sheet em mobile (ver §15)                                                     |

---

## 2. Contexto e restrições herdadas do backend

A arquitetura frontend é determinada, em grande parte, pelo contrato estabelecido em [`seguranca.md`](./seguranca.md):

- **OAuth2 Authorization Code com PKCE** com Google; o backend atua como *broker*.
- `OAuth2LoginSuccessHandler` redireciona para `FRONTEND_CALLBACK_SUCCESS_URL` com o access token no **fragmento** da URL: `#token=...`. O fragmento nunca trafega ao servidor (mitiga fugas em logs/`Referer`).
- O refresh token é entregue em cookie `HttpOnly`, `Secure`, `SameSite=Lax`, `path=/`, `maxAge = jwt.refresh-expiration` (default 3600s).
- Os endpoints `POST /auth/refresh` e `POST /auth/logout` são protegidos por CSRF via Double Submit Cookie; o frontend precisa obter o `XSRF-TOKEN` via `GET /auth/csrf-token` e enviá-lo no header `X-XSRF-TOKEN` em POSTs a `/auth/**`.
- A API (chain 3, `STATELESS`) valida JWT via `Authorization: Bearer <jwt>` e não exige CSRF.
- O JWT (RSA, 15 min) contém claims `iss`, `sub` (= `Usuario.id`), `roles` (com prefixo `ROLE_`), `jti`, `iat`, `exp`. O frontend não decodifica o JWT — apenas o transporta no header `Authorization: Bearer`. `User` completo (com `nome`, `email`, `ativo`, `criadoEm`, `perfis`) vem de `GET /api/usuarios/me`.
- Perfis são **cumulativos** e `UsuarioService.atualizarPerfis` sempre adiciona `ROLE_COLABORADOR`. Logo, todo usuário autenticado tem ao menos esse perfil.
- Endpoints públicos: `/api/ambientes/publicados/**` (listagens e detalhes, este último com `permitAll` mas autoridade interna exige `ROLE_COLABORADOR` — ver abaixo), `/auth/**`, `/health`, `/oauth2/**`, `/login/**`.
- Proteção por autoridade (chain 3):

| Path | Authority exigida |
|---|---|
| `/api/ambientes/nao-publicados/**` | `ROLE_GESTOR_SISTEMA` |
| `/api/ambientes/validacao/**` | `ROLE_VALIDADOR` |
| `/api/ambientes/publicados/{id}` (GET) | `ROLE_COLABORADOR` |
| `/api/ambientes/publicados/esquadrias` (GET) | `ROLE_COLABORADOR` |
| `/api/usuarios/**` | `ROLE_ADMINISTRADOR` |
| `/api/usuarios/me` (GET) | qualquer autenticado |

---

## 3. Autenticação: tokens e fluxo

### 3.1. Guarda do access token

A estratégia adotada é **em memória**: o access token é mantido em uma **variável de módulo** (closure em `lib/security/auth.ts`), nunca em `localStorage`/`sessionStorage`. Isso elimina o vetor de exfiltração por XSS (não há onde o script injetado ler o token persistente).

Implicações:

- Ao recarregar a página (F5), o token é perdido; o boot exibe brevemente `isLoading=true` e executa `POST /auth/refresh` (cookie HttpOnly é enviado automaticamente). Em 200, o novo access token é armazenado em memória e `GET /api/usuarios/me` popula o `User`.
- Em 401 do refresh na inicialização, o estado passa a "deslogado" sem flash de conteúdo protegido (a UI não renderiza rotas autenticadas enquanto `isLoading` ou `!isAuthenticated`).
- Custo: uma requisição extra no cold-start. Para um sistema institucional interno, imperceptível.

### 3.2. Não decodificação do JWT

O frontend **não decodifica** o JWT — apenas o recebe em `#token=...` (login OAuth2) ou no `POST /auth/refresh` (boot), guarda em memória, e transporta em `Authorization: Bearer`. As razões:

- `User` completo (`nome`, `email`, `ativo`, `criadoEm`, `perfis`) vem de `GET /api/usuarios/me`, que é a fonte canônica e consistente com o backend.
- Permissões são lidas de `user.perfis`, nunca das claims `roles` do JWT (evita drift entre o token e o banco).
- Refresh é reativo: o interceptor do Axios chama `POST /auth/refresh` em qualquer `401`, sem necessidade de prever expiração via claimed `exp`.
- Validação do JWT (assinatura, expiração real) é exclusiva do backend — o frontend não tem a chave pública RSA configurada nem deve fazer suposições sobre claims internas.

### 3.3. Fluxo de login (OAuth2 → callback)

```
┌─────────┐   1. /login: clique "Entrar com Google"
│ Usuário │      window.location.href = '/oauth2/authorization/google'
└────┬────┘
     │
     ▼
┌──────────┐   2. Handshake Google (PKCE) → callback /login/oauth2/code/google
│ Backend  │      CustomOAuth2UserService provisiona/ativa Usuario
└────┬─────┘      OAuth2LoginSuccessHandler:
     │             - gera JWT (15 min)
     │             - persiste+retorna refresh token (cookie HttpOnly)
     │             - 302 → FRONTEND_CALLBACK_SUCCESS_URL#token=<jwt>
     ▼
┌──────────┐   3. /callback (rota pública do frontend) lê location.hash
│ Frontend │      const token = new URLSearchParams(hash.slice(1)).get('token')
└────┬─────┘      auth.login(token):
     │             - accessToken (memória) = token
     │             - Authorization: Bearer <token> no Axios
     │             - GET /api/usuarios/me → User no Context
     │             - navigate(PAGES_ROUTES.HOME) conforme perfis
     ▼
  App autenticado
```

### 3.4. Refresh transparente

O refresh é orquestrado pelo **interceptor de resposta** do Axios (mais robusto que `setTimeout` — acompanha rotação eventual do refresh token pelo backend; ver `seguranca.md` §3).

- Ao receber `401` em rota que **não** seja `/auth/*`, o interceptor:
  1. Adquire um lock síncrono (Promise compartilhada atribuída antes de qualquer `await`) para evitar refresh concorrente — chamadas concorrentes compartilham a mesma promise e geram um único POST `/auth/refresh`.
  2. `POST /auth/refresh` (cookie HttpOnly enviado automaticamente; header `X-XSRF-TOKEN` anexo por interceptor de request — ver §3.5).
  3. Em 200: atualiza accessToken em memória; refaz a requisição original uma única vez; libera o lock.
  4. Em 401/403/qualquer erro: limpa accessToken em memória e despacha evento `auth:logout` (window.dispatchEvent). O `AuthProvider` escuta esse evento e limpa o estado (User = null, isAuthenticated = false), fazendo com que os guards redirecionem para `/login`.
- Endpoint `/auth/refresh` suporta CSRF: o frontend obtém o token **mascarado** via `GET /auth/csrf-token` (body) e o mantém em memória; o cookie `XSRF-TOKEN` (raw, `HttpOnly`) é enviado automaticamente pelo navegador, antes de qualquer POST `/auth/*`.

### 3.5. CSRF (Double Submit Cookie com mascaramento XOR)

```
1. Após login bem-sucedido, frontend chama GET /auth/csrf-token
   → Set-Cookie: XSRF-TOKEN=<raw> (HttpOnly=true)
   → Body: { token: "<mascarado>" }
   → frontend armazena <mascarado> em memória (não em storage)

2. Em todo POST /auth/* (refresh, logout), interceptor Axios:
   - lê o mascarado de getCsrfToken() (memória)
   - anexa header X-XSRF-TOKEN: <mascarado>
```

O Spring Security 6.1+ (`XorCsrfTokenRequestHandler`, default no Spring Boot 3.5) aplica mascaramento XOR ao token: o cookie `XSRF-TOKEN` guarda o **raw** e o body de `GET /auth/csrf-token` devolve o **mascarado** (máscara nova a cada chamada, mesmo raw subjacente). O header `X-XSRF-TOKEN` deve carregar o mascarado; o servidor desmascara e compara ao raw do cookie.

O cookie é `HttpOnly=true` (construtor padrão de `CookieCsrfTokenRepository`) porque o JS não precisa lê-lo — o mascarado vem do body. `HttpOnly` elimina exfiltração do raw por XSS (defesa em profundidade, mesmo princípio do access token em memória, §3.1). O cookie é enviado automaticamente pelo navegador (`withCredentials`) e lido pelo servidor; `HttpOnly` não bloqueia nenhuma dessas operações. A proteção CSRF aplica-se **apenas** a `/auth/**`; a API (chain 3) continua stateless sem CSRF (JWT vai em `Authorization`, não enviado automaticamente pelo navegador).

### 3.6. Logout

```
auth.logout():
  1. Se !FAKE_AUTH:
     - ensureCsrfToken() — garante o token CSRF mascarado em memória antes do POST
     - POST /auth/logout (com X-XSRF-TOKEN) — invalida refresh token no backend
  2. Mesmo em falha, limpa estado local:
     - accessToken (memória) = null
     - CSRF token (memória) = null
     - User = null, isAuthenticated = false, isLoading = false
  3. Guards (RequireAuth/PublicOnly) redirecionam para /login ou / conforme estado
```

O fail-safe (limpeza local independente do resultado do backend) impede estado inconsistente. O redirecionamento é delegado aos guards (não há `navigate` explícito no `logout`).

### 3.7. Dependência de backend: `GET /api/usuarios/me`

Para popular `User` no `AuthContext`, o **backend implementa** `GET /api/usuarios/me` em `UsuarioController` (faz parte do recurso "usuário", não "auth"). O path é plural (`/api/usuarios/...`) por compartilhar o `@RequestMapping` das rotas administrativas `/api/usuarios/**` de `ROLE_ADMINISTRADOR`; a rota `/me` é aberta a qualquer autenticado via matcher específico no `SecurityConfig` (colocado antes do matcher `ROLE_ADMINISTRADOR` para `/api/usuarios/**`):

- Entrada: nenhuma (lê `Authentication` do SecurityContext, derivado do JWT validado).
- Saída: representação completa de `Usuario` (id, email, nome, ativo, criadoEm, perfis).
- Autoridade: qualquer autenticado (`ROLE_COLABORADOR` é o mínimo universal).

---

## 4. Stack tecnológica

| Camada | Tecnologia | Versão | Justificativa |
|---|---|---|---|
| Build/Dev | Vite | 5+ | Build estático otimizado, hot reload instantâneo, config TS-first |
| Framework UI | React | 18+ | Ecossistema maduro, alinhado a shadcn/ui |
| Roteamento | React Router | 8+ (data routers) | Guards via `<RequireAuth>`/`<RequireRole>`, loaders, safe navigation |
| Linguagem | TypeScript | 5+ | Type safety para permissões, contracts com backend |
| HTTP Client | Axios | 1+ | Interceptores (auth, CSRF, refresh 401), retry transparente |
| Estado servidor | TanStack Query | 5+ | Cache, refetch inteligente, optimistic updates, paginação |
| Auth state | React Context | — | Simples, integrado ao React, suficiente para escopo |
| Estilização | Tailwind CSS | 4+ | Utility-first, plugin Vite nativo (`@tailwindcss/vite`), tema via `@theme` em `globals.css`, tree-shaking automático |
| UI Components | shadcn/ui (inclui `Drawer`, `Sheet`, `Avatar`, `Dialog`, `DropdownMenu`, `Accordion`) | — | Acessível (Base UI embutido), sem lock-in, copiado para o repo. |
| Formulários | React Hook Form | 7+ | Performance, integração com Zod e shadcn |
| Validação | Zod | 3+ | Type inference, validação runtime, schemas próximos do backend |
| Testes | Vitest + Testing Library | — | Coerente com Vite, mesma API do Jest |

### Justificativa da escolha Vite + React Router

A decisão pelo **CSR puro** decorre de:

1. **Token em fragmento** (`#token=...`): o servidor nunca enxerga o fragmento; SSR de rotas autenticadas não teria como enviar o token para o renderizador server-side.
2. **UCs interativos**: multistep `FormAmbiente` (UC06-FE), tabelas com paginação + filtros aplicados via botão (padrão `PesquisaBarAmbientes`) + seleção múltipla (UC01-FE, UC04-FE, UC22-FE) — trabalho essencialmente client-side.
3. **Refresh transparente**: estado de sessão no cliente; SSR exigiria vazar o token para o servidor, quebrando o modelo de segurança.
4. **SEO não é requisito**: a única rota pública indexável é `/ambientes/publicados` (lista). O modelo de negócio não exige indexação de nenhum dado.
5. **Custo operacional**: SSR universal exigiria processo Node em produção (PM2/container), a mais uma peça para operar. Com CSR, o deploy é estático atrás de Nginx.

---

## 5. Estrutura de pastas

```
frontend/
├── src/
│   ├── main.tsx                    # bootstrap: QueryClient, AuthProvider, RouterProvider
│   ├── App.tsx                     # <RouterProvider router={router}/>
│   │
│   ├── router/
│   │   └── index.tsx               # createBrowserRouter(...)
│   │
│   ├── routes/                     # 1 pasta por rota
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── callback/                # lê location.hash #token=...
│   │   │   └── page.tsx
│   │   ├── unauthorized/
│   │   │   └── page.tsx
│   │   ├── ambientes/
│   │   │   ├── publicados/          # público (lista) + autenticado (detalhe/esquadrias)
│   │   │   │   ├── page.tsx         # lista (UC21-FE)
│   │   │   │   ├── [id]/
│   │   │   │   │   └── page.tsx     # detalhe (UC19-FE) — RequireRole COLABORADOR
│   │   │   │   └── esquadrias/      # ação em lote UC20-FE (modal)
│   │   │   │       └── page.tsx
│   │   │   ├── validacao/           # ROLE_VALIDADOR
│   │   │   │   ├── page.tsx         # lista (UC01-FE)
│   │   │   │   └── [id]/
│   │   │   │       └── page.tsx     # detalhe (UC02-FE) com ações Publicar/Privar
│   │   │   └── nao-publicados/      # ROLE_GESTOR_SISTEMA
│   │   │       ├── page.tsx         # lista (UC04-FE)
│   │   │       ├── novo/
│   │   │       │   └── page.tsx     # FormAmbiente multistep (UC06-FE)
│   │   │       └── [id]/
│   │   │           └── page.tsx     # detalhe (UC05-FE) com modais UC07–UC18
│   │   ├── usuarios/                # ROLE_ADMINISTRADOR
│   │   │   └── page.tsx
│   │   └── _layout/
│   │       └── protected-layout.tsx # Outlet com Header/Sidebar
│   │
│   ├── components/
│   │   ├── ui/                      # shadcn/ui (button, dialog, input, table, ...)
│   │   ├── auth/
│   │   │   ├── AuthContext.ts       # createContext(AuthContextValue)
│   │   │   ├── AuthProvider.tsx     # Provider com user, login, logout, refreshUser
│   │   │   ├── RequireAuth.tsx      # <Outlet> guard autenticação
│   │   │   ├── RequireRole.tsx      # <Outlet> guard autorização
│   │   │   ├── PublicOnly.tsx       # <Outlet> guard para rotas públicas (redireciona autenticados)
│   │   │   └── PermissionButton.tsx # botão condicional por role
│   │   ├── layout/
│   │   │   ├── Header.tsx
│   │   │   ├── ProtectedNavigation.tsx  # menu adaptativo por role (desktop + mobile drawer)
│   │   │   ├── UserMenu.tsx           # avatar → Sheet (mobile) / DropdownMenu (desktop)
│   │   │   └── Footer.tsx
│   │   ├── ambientes/                # TabelaPadrao, DetalheAmbiente, FormAmbiente, PesquisaBarAmbientes,
│   │   │                              # ModalConfirmacao, AcoesLote, PaginacaoFooter, ErrorLista, modais UC07–UC18
│   │   ├── usuarios/                 # TabelaUsuarios, ModalEditarPerfis, ModalConfirmacaoStatusUsuario
│   │   └── common/
│   │       └── ResponsiveModal.tsx   # wrapper: Dialog (desktop) | Drawer (mobile)
│   │
│   ├── lib/
│   │   ├── security/
│   │   │   ├── auth.ts               # variável de módulo + accessors (set/get/clear do token)
│   │   │   ├── csrf.ts               # getCsrfToken() — token CSRF mascarado em memória
│   │   │   └── permissions.ts        # ROUTE_PERMISSIONS, ACTION_PERMISSIONS, hasPermission, getRequiredRoles
│   │   ├── api/
│   │   │   ├── api.ts                # instância Axios + interceptores (auth, CSRF, refresh 401)
│   │   │   ├── api-ambientes.ts      # compartilhado: fetchAmbientes, fetchDetalheAmbiente, fetchEsquadriasAmbientes
│   │   │   ├── api-publicados.ts     # wrappers UC21-FE → api-ambientes (fetchPublicados, fetchDetalhePublicados, fetchEsquadriasPublicados)
│   │   │   ├── api-usuarios.ts       # UC22–UC26-FE
│   │   │   ├── api-validacao.ts      # UC01–UC03-FE (fetchValidacao, fetchDetalheValidacao, publicarAmbiente, privarAmbiente)
│   │   │   └── api-naopublicados.ts  # UC05–UC18-FE
│   │   ├── ambientes/
│   │   │   └── esquadrias.ts         # filtro/resumo de esquadrias (lógica pura)
│   │   └── shadcn-helper.ts                  # cn(), formatadores, helpers
│   │
│   ├── hooks/
│   │   ├── useAsyncAction.ts         # executar ação async (loading + fechar no sucesso + toast de erro)
│   │   ├── useAuth.ts                # atalho para useContext(AuthContext)
│   │   ├── useFiltroLocal.ts         # rascunho local sincronizado com a URL (PesquisaBarAmbientes/Usuarios + hooks de search params)
│   │   ├── usePaginationParams.ts    # núcleo genérico: page/size + handlePageChange/handleSizeChange/updateSearchParams
│   │   ├── useAmbientesSearchParams.ts # adaptador fino: usePaginationParams + Zod (filtros) + tipoFiltro
│   │   ├── useSelecaoAmbientes.ts    # seleção múltipla por página das listas de ambientes (publicados/nao-publicados)
│   │   ├── usePermission.ts          # canDo(action), canAccess(route), hasRole(roles)
│   │   └── useUsuariosSearchParams.ts # adaptador fino: usePaginationParams + nome/email + tipoFiltro
│   │
│   ├── types/
│   │   ├── paginacao.ts              # DadosPaginacaoSchema + DadosPaginacao (compartilhado entre domínios)
│   │   ├── usuarios/
│   │   │   ├── user.ts               # Role, User, AuthState, StatusAcao
│   │   │   └── filtros.ts            # TipoFiltroUsuarios, FiltrosUsuarios, FILTROS_USUARIOS_VAZIOS
│   │   └── ambientes/
│   │       ├── enums.ts              # enums espelhados do backend + transforms de resposta de enum
│   │       ├── filtros.ts            # Filtros, FILTROS_VAZIOS, UrlFiltrosSchema
│   │       ├── localizacao.ts        # LocalizacaoSchema (resposta; usado por response/esquadrias)
│   │       ├── query.ts              # AmbientesQuery, EsquadriasQuery
│   │       ├── request.ts            # schemas de request (AmbienteReq…) + keysOf/nomeTecnicoDeRotulo
│   │       ├── response.ts           # schemas/tipos de resposta de ambiente (AmbienteBasico, AmbienteDetalhe…)
│   │       └── esquadrias.ts         # schemas/tipos de resposta de esquadrias (Esquadria, EsquadriasResponse…)
│   │
│   └── constants/
│       ├── roles.ts                  # ROLE_LABELS (rótulos de perfis); Role vive em types/usuarios/user.ts
│       └── routes.ts                 # PAGES_ROUTES, API_ROUTES, BACKEND_URL
│
├── public/
│   └── favicon.svg
├── nginx/
│   └── frontend.conf                 # config de referência (ver §10)
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

### Convenções

- **`routes/`** aloca cada tela em uma pasta; `page.tsx` é o default. Subrotas dinâmicas usam `[id]/` (mantém convenção Next-like para familiaridade).
- **`components/ui/`** são os componentes base do shadcn/ui (gerados por CLI, copiados não dependidos).
- **`lib/`** é infraestrutura (Axios, auth em memória, permissions, csrf), agrupada por domínio (`security/`, `api/`, `ambientes/`).
- **`hooks/`** contém lógica reutilizável enxuta.

---

## 6. Sistema de roles e permissões

### 6.1. Tipos

```typescript
// types/usuarios/user.ts

/**
 * Enum de roles. Sincronizado com br.edu.ifce.security.model.domain.Perfil.
 */
export enum Role {
  COLABORADOR = 'ROLE_COLABORADOR',
  VALIDADOR = 'ROLE_VALIDADOR',
  GESTOR_SISTEMA = 'ROLE_GESTOR_SISTEMA',
  ADMINISTRADOR = 'ROLE_ADMINISTRADOR',
}

/**
 * Usuário autenticado. Retornado por GET /api/usuarios/me.
 *
 * Observação: o frontend NÃO decodifica o JWT (ver §3.2). Dados completos
 * (nome, email, ativo, criadoEm, perfis) vêm deste endpoint; o JWT é apenas
 * transportado em Authorization: Bearer.
 */
export interface User {
  id: number
  email: string
  nome: string
  ativo: boolean
  criadoEm: string  // ISO date string
  perfis: Role[]
}

/**
 * Estado de auth exposto pelo AuthContext.
 */
export interface AuthState {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean      // true durante boot/refresh inicial
}
```

> Obs.: `accessToken` **não** faz parte de `AuthState` exposto a componentes — quem precisa dele é somente `lib/api/api.ts` (interceptor). Isto evita acoplamento e re-renders desnecessários.

### 6.2. Mapeamento central de permissões

```typescript
// lib/security/permissions.ts

import { Role } from '@/types/usuarios/user'

/**
 * Permissões por rota. Espelha exatamente as authorities exigidas
 * pelo SecurityFilterChain do backend (seguranca.md §7.2). Rotas não
 * listadas são consideradas públicas.
 *
 * Importante: cada rota declara a MENOR authority exigida pelo backend,
 * nunca a união de perfis. Como UsuarioService.atualizarPerfis sempre
 * adiciona ROLE_COLABORADOR a todo usuário, exigir [COLABORADOR]
 * cobre automaticamente VALIDADOR, GESTOR_SISTEMA e ADMINISTRADOR.
 */
export const ROUTE_PERMISSIONS: Record<string, Role[]> = {
  // Validador
  '/ambientes/validacao':         [Role.VALIDADOR],
  '/ambientes/validacao/:id':     [Role.VALIDADOR],

  // Gestor
  '/ambientes/nao-publicados':    [Role.GESTOR_SISTEMA],
  '/ambientes/nao-publicados/novo': [Role.GESTOR_SISTEMA],
  '/ambientes/nao-publicados/:id': [Role.GESTOR_SISTEMA],

  // Colaborador (mínimo universal — todo autenticado tem este perfil)
  '/ambientes/publicados/:id':    [Role.COLABORADOR],
  '/ambientes/publicados/esquadrias': [Role.COLABORADOR],

  // Administrador
  '/usuarios':                    [Role.ADMINISTRADOR],
}

/**
 * Permissões granulares por ação. Útil para mostrar/ocultar botões.
 * Formato 'recurso:ação'.
 */
export const ACTION_PERMISSIONS: Record<string, Role[]> = {
  // Validador
  'ambiente:publicar':            [Role.VALIDADOR],
  'ambiente:privar':              [Role.VALIDADOR],

  // Gestor
  'ambiente:criar':               [Role.GESTOR_SISTEMA],
  'ambiente:editar':              [Role.GESTOR_SISTEMA],
  'ambiente:deletar':             [Role.GESTOR_SISTEMA],
  'ambiente:enviar-validacao':    [Role.GESTOR_SISTEMA],
  'ambiente:alterar-tipo':        [Role.GESTOR_SISTEMA],
  'ambiente:duplicar':            [Role.GESTOR_SISTEMA],

  // Colaborador
  'ambiente:ver-esquadrias':      [Role.COLABORADOR],

  // Administrador
  'usuario:editar-perfis':        [Role.ADMINISTRADOR],
  'usuario:desativar':            [Role.ADMINISTRADOR],
  'usuario:ativar':               [Role.ADMINISTRADOR],
}

export function hasPermission(userRoles: Role[], requiredRoles: Role[]): boolean {
  return userRoles.some(role => requiredRoles.includes(role))
}

/**
 * Resolve a menor authority exigida para um pathname, casando por
 * especificidade (rota mais longa vence): rotas dinâmicas (:id) casam via
 * matchRoute e rotas estáticas via startsWith.
 * Ex.: '/ambientes/validacao/123' casa com '/ambientes/validacao/:id'.
 */
export function getRequiredRoles(pathname: string): Role[] | null {
  const matchedRoute = Object.keys(ROUTE_PERMISSIONS)
    .filter(route => matchRoute(route, pathname) || pathname.startsWith(route))
    .sort((a, b) => b.length - a.length)[0]

  return matchedRoute ? ROUTE_PERMISSIONS[matchedRoute] : null
}

/**
 * Substitui parâmetros dinâmicos para comparação.
 * Ex.: pattern '/ambientes/publicados/:id' no pathname real /ambientes/publicados/123.
 */
export function matchRoute(pattern: string, pathname: string): boolean {
  const re = new RegExp('^' + pattern.replace(/:[^/]+/g, '[^/]+') + '$')
  return re.test(pathname)
}
```

> A separação entre `ROUTE_PERMISSIONS` (com `:id`) e `getRequiredRoles` com `matchRoute` permite que rotas dinâmicas sejam matched corretamente tanto no guard quanto no hook `usePermission.canAccess`.

---

## 7. Provider de Autenticação

```typescript
// components/auth/AuthContext.ts — contexto separado para evitar lint react-refresh

import { createContext } from 'react'
import type { AuthState } from '@/types/usuarios/user'

export interface AuthContextValue extends AuthState {
  login: (token: string) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
```

```typescript
// hooks/useAuth.ts — hook de autenticação (atalho para useContext)

import { useContext } from 'react'
import { AuthContext } from '@/components/auth/AuthContext'
import type { AuthContextValue } from '@/components/auth/AuthContext'

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth deve ser usado dentro de <AuthProvider>')
  return ctx
}
```

```typescript
// components/auth/AuthProvider.tsx

import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import type { AuthState, User } from '@/types/usuarios/user'
import { api, refreshAccessToken } from '@/lib/api/api'
import { setAccessToken, clearAccessToken, getAccessToken } from '@/lib/security/auth'
import { ensureCsrfToken, clearCsrfToken } from '@/lib/security/csrf'
import { AuthContext } from './AuthContext'
import type { AuthContextValue } from './AuthContext'

const FAKE_AUTH = import.meta.env.VITE_FAKE_AUTH === 'true'

const FAKE_USER: User = {
  id: 1,
  email: 'dev@ifce.edu.br',
  nome: 'Dev FakeAuth',
  ativo: true,
  criadoEm: new Date().toISOString(),
  perfis: ['ROLE_COLABORADOR', 'ROLE_VALIDADOR', 'ROLE_GESTOR_SISTEMA', 'ROLE_ADMINISTRADOR'] as User['perfis'],
}

// Lazy initializer: evita setState dentro de effect quando FakeAuth ativo
const INITIAL_STATE: AuthState = FAKE_AUTH
  ? { user: FAKE_USER, isAuthenticated: true, isLoading: false }
  : { user: null, isAuthenticated: false, isLoading: true }

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>(INITIAL_STATE)

  const loadUser = useCallback(async () => {
    const { data } = await api.get<User>('/api/usuarios/me')
    setState(s => ({ ...s, user: data, isAuthenticated: true, isLoading: false }))
  }, [])

  // Boot: tenta refresh silencioso (cookie HttpOnly enviado automaticamente)
  useEffect(() => {
    if (FAKE_AUTH) {
      console.warn('[FakeAuth] ATIVADO — não usar em produção!')
      return
    }
    let cancelled = false
    ;(async () => {
      try {
        if (!getAccessToken()) {
          await refreshAccessToken()      // POST /auth/refresh
        }
        await loadUser()
      } catch {
        clearAccessToken()
        if (!cancelled) {
          setState(s => ({ ...s, isLoading: false, isAuthenticated: false }))
        }
      }
    })()
    return () => { cancelled = true }
  }, [loadUser])

  // Escuta evento auth:logout disparado pelo interceptor Axios em falha de refresh
  useEffect(() => {
    const onLogout = () => {
      clearAccessToken()
      clearCsrfToken()
      setState({ user: null, isAuthenticated: false, isLoading: false })
    }
    window.addEventListener('auth:logout', onLogout)
    return () => window.removeEventListener('auth:logout', onLogout)
  }, [])

  // Após OAuth2 callback: recebe token diretamente
  const login = useCallback(async (token: string) => {
    setAccessToken(token)
    await loadUser()
  }, [loadUser])

  const logout = useCallback(async () => {
    if (!FAKE_AUTH) {
      try {
        await ensureCsrfToken()           // garante cookie XSRF-TOKEN antes do POST
        await api.post('/auth/logout')    // X-XSRF-TOKEN anexado pelo interceptor
      } catch (e) {
        console.warn('Logout backend falhou — limpando estado local', e instanceof Error ? e.message : String(e))
      }
    }
    clearAccessToken()
    clearCsrfToken()
    setState({ user: null, isAuthenticated: false, isLoading: false })
  }, [])

  const refreshUser = useCallback(async () => {
    if (getAccessToken()) await loadUser()
  }, [loadUser])

  const value = useMemo<AuthContextValue>(() => ({
    ...state, login, logout, refreshUser,
  }), [state, login, logout, refreshUser])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
```

```typescript
// lib/security/auth.ts — variável de módulo (em memória)
// O frontend NÃO decodifica o JWT (ver §3.2). Token é apenas uma string
// opaca transportada em Authorization: Bearer. Dados do Usuario vêm de
// /api/usuarios/me; refresh é reativo (interceptor Axios trata 401).

let accessToken: string | null = null

export function setAccessToken(token: string | null) { accessToken = token }
export function getAccessToken(): string | null { return accessToken }
export function clearAccessToken() { accessToken = null }
```

---

## 8. HTTP Client (Axios)

```typescript
// lib/api/api.ts

import axios, { type AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { getAccessToken, setAccessToken, clearAccessToken } from '@/lib/security/auth'
import { getCsrfToken, ensureCsrfToken } from '@/lib/security/csrf'
import { API_ROUTES, BACKEND_URL } from '@/constants/routes'

export const api = axios.create({
  baseURL: BACKEND_URL,
  withCredentials: true,   // envia cookies (refreshToken, XSRF-TOKEN)
  headers: { 'Content-Type': 'application/json' },
})

// Lock p/ evitar refresh concorrente em rajada de 401.
// O lock é adquirido SINCRONAMENTE: a IIFE async é invocada e atribuída
// a refreshPromise na mesma instrução, ANTES de qualquer await. Assim,
// chamadas concorrentes que cheguem durante o await ensureCsrfToken()
// encontram refreshPromise não-null e compartilham a mesma promise,
// evitando POSTs duplicados (ver §3.4 e Riscos §19).
let refreshPromise: Promise<string> | null = null

export function refreshAccessToken(): Promise<string> {
  if (refreshPromise) return refreshPromise

  refreshPromise = (async () => {
    // Garante que o cookie XSRF-TOKEN existe antes do POST /auth/refresh
    // (exigido pelo authFilterChain do backend — ver seguranca.md §3)
    await ensureCsrfToken()
    const { data } = await axios.post(`${BACKEND_URL}${API_ROUTES.AUTH}/refresh`, {}, {
      withCredentials: true,
      headers: { 'X-XSRF-TOKEN': getCsrfToken() },
    })
    const newToken: string = data.accessToken
    setAccessToken(newToken)
    return newToken
  })().finally(() => { refreshPromise = null })

  return refreshPromise
}

// Interceptor de request: injeta Authorization e X-XSRF-TOKEN em /auth/*
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken()
  if (token) config.headers.set('Authorization', `Bearer ${token}`)

  // CSRF: apenas em POST/PUT/PATCH/DELETE para /auth/**
  const isAuthMutation =
    config.url?.startsWith(`${API_ROUTES.AUTH}/`) &&
    ['post', 'put', 'patch', 'delete'].includes(config.method ?? '')

  if (isAuthMutation) {
    const xsrf = getCsrfToken()
    if (xsrf) config.headers.set('X-XSRF-TOKEN', xsrf)
  }
  return config
})

// RetryConfig herda de AxiosRequestConfig e adiciona _retry
type RetryConfig = AxiosRequestConfig & { _retry?: boolean }

// Interceptor de response: em 401 (exceto /auth/*), tenta refresh uma vez
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as RetryConfig | undefined
    const isAuthEndpoint = original?.url?.startsWith(`${API_ROUTES.AUTH}/`)

    if (error.response?.status === 401 && !isAuthEndpoint && original && !original._retry) {
      original._retry = true
      try {
        const newToken = await refreshAccessToken()
        original.headers = { ...original.headers, Authorization: `Bearer ${newToken}` }
        return api(original)
      } catch {
        // Refresh falhou → deslogar (o AuthProvider escuta e redireciona)
        clearAccessToken()
        window.dispatchEvent(new CustomEvent('auth:logout'))
        return Promise.reject(error)
      }
    }
    return Promise.reject(error)
  },
)
```

```typescript
// lib/security/csrf.ts

import axios from 'axios'
import { API_ROUTES, BACKEND_URL } from '@/constants/routes'

// Token CSRF mascarado em memória (ver §3.5). O raw fica no cookie HttpOnly
// e NÃO é legível via JS; o mascarado vem do body de GET /auth/csrf-token.
let maskedCsrfToken: string | null = null

export function getCsrfToken(): string | null {
  return maskedCsrfToken
}

// Garante token mascarado em memória. O raw (cookie) persiste entre F5, mas o
// mascarado se perde no reload — por isso ensureCsrfToken() é chamado no boot
// do AuthProvider e antes de cada POST /auth/*.
export async function ensureCsrfToken(): Promise<void> {
  if (maskedCsrfToken) return
  const { data } = await axios.get(`${BACKEND_URL}${API_ROUTES.AUTH}/csrf-token`, { withCredentials: true })
  maskedCsrfToken = data.token
}

// Limpa o token em memória (força nova aquisição após logout).
export function clearCsrfToken(): void {
  maskedCsrfToken = null
}
```

No callback de login (rota `/callback`), chamar `ensureCsrfToken()` antes de qualquer POST `/auth/*`:

```typescript
// routes/callback/page.tsx
import { PAGES_ROUTES } from '@/constants/routes'

useEffect(() => {
  (async () => {
    const hash = window.location.hash.slice(1)
    const params = new URLSearchParams(hash)
    const token = params.get('token')
    if (!token) { navigate('/login'); return }
    await ensureCsrfToken()
    await login(token)
    navigate(PAGES_ROUTES.HOME)
  })()
}, [])
```

---

## 9. Roteamento e guards (React Router v8)

### 9.1. Guards

```typescript
// components/auth/RequireAuth.tsx

import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '@/hooks/useAuth'

function FullScreenLoader() {
  return (
    <div role="status" className="flex items-center justify-center min-h-screen">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary" />
    </div>
  )
}

export function RequireAuth() {
  const { isAuthenticated, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) return <FullScreenLoader />     // boot/refresh inicial
  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  return <Outlet />
}
```

```typescript
// components/auth/RequireRole.tsx

import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '@/hooks/useAuth'
import { hasPermission } from '@/lib/security/permissions'
import type { Role } from '@/types/usuarios/user'

export function RequireRole({ roles }: { roles: Role[] }) {
  const { user } = useAuth()
  const location = useLocation()

  if (!user) return <Navigate to="/login" replace state={{ from: location }} />
  if (!hasPermission(user.perfis, roles)) {
    return <Navigate to="/unauthorized" replace />
  }
  return <Outlet />
}
```

```typescript
// components/auth/PublicOnly.tsx — redireciona autenticados para /
import { Navigate, Outlet } from 'react-router'
import { useAuth } from '@/hooks/useAuth'

export function PublicOnly() {
  const { isAuthenticated, isLoading } = useAuth()
  if (isLoading) return null
  if (isAuthenticated) return <Navigate to="/" replace />
  return <Outlet />
}
```

### 9.2. Definição de rotas

```typescript
// router/index.tsx

import { Suspense } from 'react'
import { createBrowserRouter } from 'react-router'
import { RequireAuth } from '@/components/auth/RequireAuth'
import { RequireRole } from '@/components/auth/RequireRole'
import { PublicOnly } from '@/components/auth/PublicOnly'
import { Role } from '@/types/usuarios/user'
import { Loading } from '@/components/ui/Loading'
import { PAGES_ROUTES } from '@/constants/routes'
import {
  HomePage,
  LoginPage,
  CallbackPage,
  UnauthorizedPage,
  ProtectedLayout,
  PublicadosPage,
  PublicadoDetalhePage,
  EsquadriasPage,
  ValidacaoPage,
  ValidacaoDetalhePage,
  UsuariosPage,
} from '@/router/lazy-pages'

export const router = createBrowserRouter([
  // Públicas (não autenticadas)
  {
    element: <PublicOnly />,
    children: [
      { path: PAGES_ROUTES.LOGIN, element: <Suspense fallback={<Loading />}><LoginPage /></Suspense> },
    ],
  },
  { path: '/callback', element: <Suspense fallback={<Loading />}><CallbackPage /></Suspense> },     // host do #token
  { path: '/unauthorized', element: <Suspense fallback={<Loading />}><UnauthorizedPage /></Suspense> },

  // Layout comum (Header auth-aware + Footer) — público e autenticado
  {
    element: <Suspense fallback={<Loading />}><ProtectedLayout /></Suspense>,
    children: [
      // Pública (lista de publicados) — UC21-FE, sem RequireAuth, mas com Header
      { path: '/ambientes/publicados', element: <Suspense fallback={<Loading />}><PublicadosPage /></Suspense> },

      // Autenticadas
      {
        element: <RequireAuth />,
        children: [
          // Colaborador (mínimo universal — UC19-FE, UC20-FE)
          {
            path: '/ambientes/publicados/:id',
            element: <RequireRole roles={[Role.COLABORADOR]} />,
            children: [{ index: true, element: <Suspense fallback={<Loading />}><PublicadoDetalhePage /></Suspense> }],
          },
          {
            path: '/ambientes/publicados/esquadrias',
            element: <RequireRole roles={[Role.COLABORADOR]} />,
            children: [{ index: true, element: <Suspense fallback={<Loading />}><EsquadriasPage /></Suspense> }],
          },

          // Validador (UC01-UC03-FE)
          {
            path: '/ambientes/validacao',
            element: <RequireRole roles={[Role.VALIDADOR]} />,
            children: [
              { index: true, element: <Suspense fallback={<Loading />}><ValidacaoPage /></Suspense> },
              { path: ':id', element: <Suspense fallback={<Loading />}><ValidacaoDetalhePage /></Suspense> },
            ],
          },

          // Gestor (UC04-UC18-FE) — Parte 11: a implementar.
          // Estado atual: placeholder <HomePage/>. As rotas 'novo' e ':id'
          // serão adicionadas pela parte 11 (plano 11-pagina-naopublicados-formambiente.md).
          {
            path: '/ambientes/nao-publicados',
            element: <RequireRole roles={[Role.GESTOR_SISTEMA]} />,
            children: [{ index: true, element: <Suspense fallback={<Loading />}><HomePage /></Suspense> }],
          },

          // Administrador (UC22-UC26-FE)
          {
            path: '/usuarios',
            element: <RequireRole roles={[Role.ADMINISTRADOR]} />,
            children: [{ index: true, element: <Suspense fallback={<Loading />}><UsuariosPage /></Suspense> }],
          },
        ],
      },
    ],
  },

  { path: '/', element: <Suspense fallback={<Loading />}><HomePage /></Suspense> },
  { path: '*', element: <Suspense fallback={<Loading />}><UnauthorizedPage /></Suspense> },
])
```

### 9.3. Navegação adaptativa

```typescript
// components/layout/ProtectedNavigation.tsx

const menuItems: MenuItem[] = [
  { href: '/ambientes/publicados', label: 'Publicados',
    roles: null },                                  // público — sempre visível quando autenticado
  { href: '/ambientes/validacao', label: 'Aguardando Validação',
    roles: [Role.VALIDADOR] },
  { href: '/ambientes/nao-publicados', label: 'Não Publicados',
    roles: [Role.GESTOR_SISTEMA] },
  { href: '/usuarios', label: 'Usuários',
    roles: [Role.ADMINISTRADOR] },
]
```

> Sempre que `roles === null`, o item é mostrado a todo autenticado. O item **Publicados** aparece para todos. A lista pública (`/ambientes/publicados`) é servida **dentro** do layout comum: o `Header` é **auth-aware** — para anônimos exibe logo + botão "Login"; para autenticados, navegação + nome/email + "Sair". O `ProtectedNavigation` só é renderizado com usuário logado.

---

## 10. Hooks de permissão

```typescript
// hooks/usePermission.ts

import { useAuth } from '@/hooks/useAuth'
import { hasPermission, ROUTE_PERMISSIONS, ACTION_PERMISSIONS } from '@/lib/security/permissions'
import { matchRoute } from '@/lib/security/permissions'
import type { Role } from '@/types/usuarios/user'

export function usePermission() {
  const { user } = useAuth()

  function canDo(action: string): boolean {
    if (!user) return false
    const required = ACTION_PERMISSIONS[action]
    if (!required) {
      return false
    }
    return hasPermission(user.perfis, required)
  }

  function canAccess(pathname: string): boolean {
    if (!user) return false
    const entry = Object.entries(ROUTE_PERMISSIONS)
      .find(([pattern]) => matchRoute(pattern, pathname))
    if (!entry) return true   // rota pública
    const [, required] = entry
    return hasPermission(user.perfis, required)
  }

  function hasRole(roles: Role[]): boolean {
    if (!user) return false
    return hasPermission(user.perfis, roles)
  }

  return { canDo, canAccess, hasRole }
}
```

```typescript
// components/auth/PermissionButton.tsx

import type { ButtonProps } from '@/components/ui/button'
import { Button } from '@/components/ui/button'
import { usePermission } from '@/hooks/usePermission'
import type { Role } from '@/types/usuarios/user'

interface Props extends ButtonProps {
  requiredRoles: Role[]
  children: React.ReactNode
}

export function PermissionButton({ requiredRoles, children, ...rest }: Props) {
  const { hasRole } = usePermission()
  if (!hasRole(requiredRoles)) return null
  return <Button {...rest}>{children}</Button>
}
```

---

## 11. Layout com navegação condicional

```typescript
// routes/_layout/protected-layout.tsx

import { Outlet } from 'react-router'
import { Header } from '@/components/layout/Header'
import { Footer } from '@/components/layout/Footer'

export function ProtectedLayout() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1 container mx-auto px-4 py-8">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}
```

O `Header` é **auth-aware** e renderiza internamente o `ProtectedNavigation` (que filtra itens por `user.perfis.some(r => item.roles?.includes(r) ?? true)` e destaca o ativo via `useLocation()`):

- Anônimo: logo + botão "Login" (`PAGES_ROUTES.LOGIN`), sem `ProtectedNavigation`.
- Autenticado: logo + `ProtectedNavigation` + nome/email + botão "Sair" (`logout()` com navegação para `/login`).

Como o layout envolve também a rota pública `/ambientes/publicados` (§9.2), o `ProtectedLayout` não é exclusivo de rotas autenticadas — o `Header` decide o que exibir conforme o estado de autenticação.

---

## 12. TanStack Query — uso típico

```typescript
// routes/ambientes/validacao/page.tsx

import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/api'

export default function ValidacaoPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['ambientes', 'validacao', { page, size, filtros }],
    queryFn: ({ signal }) =>
      api.get('/api/ambientes/validacao', { params: { page, size, ...filtros }, signal })
         .then(r => r.data),
    staleTime: 30_000,
  })

  // ...
}
```

Convenções:

- `queryKey` sempre começa com o recurso (`ambientes`, `usuarios`) e inclui parâmetros.
- `signal` repassado para cancelamento ao desmontar.
- `staleTime` 30s para listagens — evita refetch excessivo ao navegar entre detalhe/lista.
- Mutações (`useMutation`) com `onSuccess`Invalidando queries relacionadas (`queryClient.invalidateQueries({ queryKey: ['ambientes'] })`).

---

## 13. Formulários (RHF + Zod)

```typescript
// routes/ambientes/nao-publicados/novo/page.tsx (multistep UC06-FE)

import { useForm, FormProvider } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { ambienteSchema } from '@/types/ambientes/request'
import type { AmbienteInput } from '@/types/ambientes/request'

export default function NovoAmbientePage() {
  const form = useForm<AmbienteInput>({
    resolver: zodResolver(ambienteSchema),
    defaultValues: { /* ... */ },
    mode: 'onTouched',
  })

  // ...wizard com <FormProvider>...
}
```

```typescript
// types/ambientes/request.ts

import { z } from 'zod'
import { TipoEsquadria, TipoGeometria } from '@/types/ambientes/enums'

// As chaves dos enums TS espelham os NOMES TÉCNICOS dos enums Kotlin.
const keysOf = <T extends Record<string, string>>(e: T) =>
  Object.keys(e) as [(keyof T & string), ...(keyof T & string)[]]

// Espelha GeometriaAmbienteReq do backend
export const geometriaSchema = z.object({
  tipo: z.enum(keysOf(TipoGeometria)),   // 'RETANGULAR', 'TRIANGULAR'
  base: z.number().positive(),
  altura: z.number().positive(),
  repeticao: z.number().int().positive().default(1),
})

// Espelha GeometriaEsquadriaReq do backend
export const geometriaEsquadriaSchema = z.object({
  base: z.number().positive(),
  altura: z.number().positive(),
  repeticao: z.number().int().positive().default(1),
})

// Espelha EsquadriaReq do backend
export const esquadriaSchema = z.object({
  tipo: z.enum(keysOf(TipoEsquadria)),   // 'PORTA', 'JANELA', 'COBOGO', 'VAO_ABERTO', 'ESQUADRIA_OUTRO_AMBIENTE'
  geometria: geometriaEsquadriaSchema,
  material: z.string().min(1),
  alturaPeitoril: z.number().min(0).default(0),
  informacaoAdicional: z.string().max(255).optional().default(''),
})

// Espelha AmbienteReq do backend
export const ambienteSchema = z.object({
  nome: z.string().min(1).max(50),
  localizacao: z.object({
    bloco: z.string().min(1),
    unidade: z.string().min(1),
    andar: z.number().int(),
  }),
  tipo: z.enum(['SALA_AULA', 'LABORATORIO', /* ... espelha backend */]),
  capacidade: z.number().int().positive(),
  geometrias: z.array(geometriaSchema).min(1, 'Pelo menos uma geometria'),
  pesDireitos: z.array(z.number().positive()).min(1, 'Pelo menos um pé-direito'),
  esquadrias: z.array(esquadriaSchema)
    .refine(arr => arr.some(e => e.tipo === 'PORTA'),
            'Pelo menos uma porta é obrigatória'),
  informacaoAdicional: z.string().max(255).optional().default(''),
})

export type AmbienteInput = z.infer<typeof ambienteSchema>
```

> Schemas espelham os DTOs de request do backend (`AmbienteReq`, `GeometriaAmbienteReq`, `EsquadriaReq`). O campo `pesDireitos` é um array de números (não objetos), conforme o backend.

---

## 14. Deploy — Nginx

A arquitetura prevê **dois serviços** por trás de um único Nginx reverse proxy:

- `frontend` (estático): `dist/` servido por `nginx:alpine`.
- `backend`: Spring (Kotlin) no `:8080` (container dockerizado ou processo dedicado).

O Nginx é o mesmo reverse proxy já exigido pelo OAuth2 (`X-Forwarded-Proto`/`X-Forwarded-Host`; ver `operacao.md` §4.1 e §troubleshooting). A mesma origem entre frontend e `/auth`/`/oauth2` torna `SameSite=Lax` suficiente.

### 14.1. Configuração de referência (`nginx/frontend.conf`)

```nginx
server {
  listen 80;
  server_name _;
  return 301 https://$host$request_uri;
}

server {
  listen 443 ssl http2;
  server_name catalogo.ifce.edu.br;

  ssl_certificate     /etc/nginx/tls/fullchain.pem;
  ssl_certificate_key /etc/nginx/tls/privkey.pem;

  root /var/www/catalogo-frontend/dist;
  index index.html;

  # Cache de assets com hash (Vite emite /assets/[name].[hash].js)
  location /assets/ {
    expires 1y;
    add_header Cache-Control "public, immutable";
    try_files $uri =404;
  }

  # SPA fallback — todo / vai para index.html, exceto rotas de API
  location / {
    try_files $uri $uri/ /index.html;
    add_header Cache-Control "no-cache, must-revalidate";
  }

  # Proxy /auth/** (CSRF + cookie refreshToken — sempre mesma origem)
  location /auth/ {
    proxy_pass http://backend:8080;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-Host  $host;
    proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
    proxy_pass_request_headers on;
  }

  # Proxy /oauth2/** e /login/** (handshake OAuth2)
  location ~ ^/(oauth2|login)(/.*)?$ {
    proxy_pass http://backend:8080;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-Host  $host;
    proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
  }

  # Proxy /api/** (Authorization via header; sem CSRF)
  location /api/ {
    proxy_pass http://backend:8080;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header Authorization $http_authorization;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-Host  $host;
  }

  # Health check interno
  location = /health-frontend {
    access_log off;
    return 200 "ok\n";
    add_header Content-Type text/plain;
  }
}
```

### 14.2. `docker-compose.yml` (trecho frontend)

```yaml
services:
  frontend:
    image: nginx:alpine
    volumes:
      - ./frontend/dist:/var/www/catalogo-frontend/dist:ro
      - ./nginx/frontend.conf:/etc/nginx/conf.d/default.conf:ro
      - ./tls:/etc/nginx/tls:ro
    ports:
      - "443:443"
      - "80:80"
    depends_on:
      - backend
```

### 14.3. Variáveis de ambiente do frontend

| Env var | Default | Descrição |
|---|---|---|
| `VITE_BACKEND_URL` | `''` (mesma origem — proxy Nginx) | Lida apenas em `constants/routes.ts` (`BACKEND_URL`). Usada como `baseURL` do Axios e nas chamadas de auth/CSRF com axios puro. Em dev, `http://localhost:8080` para contornar o proxy |
| `VITE_FAKE_AUTH` | `'false'` | Quando `'true'`, ativa FakeAuth (usuário mockado) para dev sem backend. **Nunca em produção** — o build de produção deve omiti-la ou setá-la `'false'` |

> O entry do OAuth2 (`/oauth2/authorization/google`) é uma **constante** (`PAGES_ROUTES.GOOGLE_OAUTH_ENTRY` em `constants/routes.ts`); não é env var porque nunca varia entre ambientes.

Em dev local, `vite dev` (porta 5173) chama a API diretamente em `localhost:8080`. O `vite.config.ts` pode definir `server.proxy` para reproduzir o Nginx:

```typescript
// vite.config.ts (trecho)
export default defineConfig({
  server: {
    proxy: {
      '/api':          'http://localhost:8080',
      '/auth':         'http://localhost:8080',
      '/oauth2':       'http://localhost:8080',
      '/login/oauth2': 'http://localhost:8080', // entry OAuth2; `/login` puro é rota do SPA
    },
  },
})
```

---

## 15. Responsividade e UX mobile

A aplicação deve ser plenamente utilizável em desktops e celulares. Esta seção consolida as decisões de responsividade.

### 15.1. Princípios

- **Mobile-first**: layouts começam em 375px e são progressivamente melhorados em `sm`/`md`/`lg`.
- **Breakpoints**: Tailwind defaults — `sm 640`, `md 768`, `lg 1024`. Ponto de corte principal `md` (768px) para alternar entre layout mobile e desktop.
- **Touch targets ≥ 44×44px** (WCAG 2.5.5) em todos os elementos interativos.
- **Safe-area**: `env(safe-area-inset-*)` em barras fixas para notch/L-shape do iPhone.
- **`motion-reduce:reduce`** (Tailwind variant) em animações para respeitar `prefers-reduced-motion`.
- **`viewport-fit=cover`** + `width=device-width, initial-scale=1` em `<meta name="viewport">`.

### 15.2. Configurações de base

- `index.html`:

  ```html
  <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
  ```

- `src/styles/globals.css`:

  ```css
  body { position: relative; }   /* exigido pelo Drawer (Base UI) em iOS Safari */
  ```

- Tema Tailwind v4: configurado via `@theme { ... }` em `src/styles/globals.css`. Defaults cobrem os breakpoints (`sm 640`, `md 768`, `lg 1024`); sem `tailwind.config.ts`/`postcss.config.js`.
- `shadcn` CLI: instalar `drawer`, `sheet`, `avatar`, `dropdown-menu`, `accordion` (todos parte do catálogo oficial).

### 15.3. Componentes `Drawer` / `Sheet` do shadcn

O `Drawer` do shadcn é a camada visual para bottom-sheets e side-sheets em mobile. A partir da release atual, ele usa **Base UI**. Para bottom-sheets (eixos verticais), use `swipeDirection="down"`. Para side-sheets (eixos horizontais), use `swipeDirection="left"` ou `"right"`.

### 15.4. Header compacto

- Altura `h-14` em mobile (`md:h-16`).
- Sempre visível.
- Mobile (`<md`):
  - **Esquerda**: hamburger (`lucide-react` `Menu`) → abre `<Sheet side="left">` contendo `<ProtectedNavigation>` em lista vertical (variant mobile). Item fecha o Sheet ao ser selecionado.
  - **Direita**: **avatar do usuário** (`<Avatar>` shadcn com iniciais em `<AvatarFallback>`, pois não há `picture` em `Usuario`) → abre `<Sheet side="right">` com perfil dedicado (nome, email, badges de `perfis`, botão "Sair" full-width).
- Desktop (`≥md`): hamburger e avatar-sheet ocultos; `<ProtectedNavigation>` inline horizontal à esquerda; `<UserMenu>` (avatar pequeno + `DropdownMenu`) à direita.
- Ambos os triggers: `min-h-[44px] min-w-[44px]` e `aria-label` explícito ("Abrir menu" / "Abrir perfil").
- Implementação mostrar/ocultar puramente via CSS (`flex md:hidden` / `hidden md:flex`) — SSR-safe.

### 15.5. `<UserMenu>` (componente único para mobile e desktop)

```tsx
// components/layout/UserMenu.tsx

import {
  Avatar, AvatarFallback,
} from '@/components/ui/avatar'
import {
  Sheet, SheetContent, SheetTrigger, SheetHeader, SheetTitle,
} from '@/components/ui/sheet'
import {
  DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem,
} from '@/components/ui/dropdown-menu'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'
import { RoleBadge } from '@/components/usuarios/RoleBadge'

export function UserMenu() {
  const { user, logout } = useAuth()
  if (!user) return null
  const initials = user.nome.split(' ').map(p => p[0]).slice(0, 2).join('').toUpperCase()

  return (
    <>
      {/* Mobile: avatar → Sheet à direita */}
      <Sheet>
        <SheetTrigger className="md:hidden" aria-label="Abrir perfil">
          <Avatar className="h-10 w-10 min-h-[44px] min-w-[44px]">
            <AvatarFallback>{initials}</AvatarFallback>
          </Avatar>
        </SheetTrigger>
        <SheetContent side="right" className="w-[280px]">
          <SheetHeader>
            <SheetTitle>{user.nome}</SheetTitle>
          </SheetHeader>
          <div className="px-4 space-y-4">
            <p className="text-sm text-muted-foreground">{user.email}</p>
            <div className="flex flex-wrap gap-1">
              {user.perfis.map(p => <RoleBadge key={p} role={p} />)}
            </div>
            <Button variant="outline" className="w-full min-h-[44px]" onClick={() => logout()}>
              Sair
            </Button>
          </div>
        </SheetContent>
      </Sheet>

      {/* Desktop: avatar → DropdownMenu inline */}
      <DropdownMenu>
        <DropdownMenuTrigger className="hidden md:flex" aria-label="Abrir perfil">
          <Avatar className="h-8 w-8">
            <AvatarFallback className="text-xs">{initials}</AvatarFallback>
          </Avatar>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <div className="px-2 py-1.5">
            <p className="text-sm font-medium">{user.nome}</p>
            <p className="text-xs text-muted-foreground">{user.email}</p>
          </div>
          <DropdownMenuItem onClick={() => logout()}>Sair</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </>
  )
}
```

### 15.6. `<ProtectedNavigation>` — duas variantes

A mesma fonte de dados `menuItems` renderiza em duas variantes:

- `<DesktopNav>`: inline horizontal, exibida em `≥md` (`hidden md:flex`).
- `<MobileNavList>`: lista vertical dentro do `<Sheet side="left">`, cada item com ícone `lucide-react` + label, `min-h-[44px]`, fechando o Sheet em seleção.

Itens `roles === null` são considerados visíveis a qualquer autenticado (ex.: "Publicados" — visível para todos, dado que `COLABORADOR` é perfil mínimo universal).

### 15.7. Tabelas — `TabelaPadrao`, `TabelaUsuarios`

- **`≥md`**: `<Table>` shadcn completa (ordenação, checkbox, paginação compacta).
- **`<md`**: **cards empilhados** (`<div className="md:hidden space-y-3">`), cada item virando card com:
  - Nome em destaque (topo do card).
  - Chips (`<Badge>`) para atributos secundários (Tipo, Localização, Capacidade, Perfis…).
  - `<DropdownMenu>` kebab no canto direito com as ações por item (Editar Perfis, Desativar/Ativar, Publicar/Privar, Editar, Duplicar, etc.).
  - Checkbox de seleção integrado ao card (mesma fonte `selectedIds` do estado em lote).
- **Barra de ações em lote (`AcoesLote`)**: em mobile, `fixed bottom-0 inset-x-0` com `padding-bottom: env(safe-area-inset-bottom)`, aparecendo apenas quando há seleção. Em desktop, barra inline acima da tabela.
- Paginação: botões `Anterior`/`Próximo` `w-full` em mobile, compactos em desktop.
- Deep-linking de filtros/paginação na URL é mantido em ambos os viewports.
- Coluna `Perfis` em `TabelaUsuarios`: `flex flex-wrap gap-1` com `<Badge>` por perfil (chips que wrapnam).

### 15.8. `FormAmbiente` (UC06-FE) — multistep responsivo

- `<Stepper>` shadcn; em mobile colapsa para "Etapa X de Y" + label da etapa atual (sem números grandes).
- Botões `Voltar`/`Próximo`/`Salvar`/`Cancelar` em `w-full md:w-auto`.
- Campos numéricos com `inputMode="numeric"` ou `"decimal"` (abre teclado correto no mobile).
- Listas editáveis (geometrias, pés-direitos, esquadrias): cada item é um card empilhado, com botão "remover" (`lucide-react` `Trash2` ícone) à direita; novos itens adicionados ao final com animação ease-in.
- Validação por etapa (Zod schema recortado) para evitar avançar com dados inválidos — UX melhor que validar apenas ao final.

### 15.9. `<ResponsiveModal>` — modais UC07–UC13

Wrapper que renderiza `Dialog` (desktop) ou `Drawer` (mobile) conforme viewport:

```tsx
// src/components/common/ResponsiveModal.tsx

import * as React from 'react'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import {
  Drawer, DrawerContent, DrawerHeader, DrawerTitle, DrawerFooter,
} from '@/components/ui/drawer'
import { useIsMobile } from '@/hooks/use-mobile'
import type { DialogProps } from '@radix-ui/react-dialog'

interface Props extends DialogProps {
  title: string
  description?: string
  children: React.ReactNode
  footer?: React.ReactNode
}

export function ResponsiveModal({ title, description, children, footer, ...rest }: Props) {
  const isMobile = useIsMobile()   // hook fornecido pela CLI do shadcn

  if (isMobile) {
    return (
      <Drawer open={rest.open} onOpenChange={rest.onOpenChange} swipeDirection="down">
        <DrawerContent className="max-h-[85dvh]">
          <DrawerHeader>
            <DrawerTitle>{title}</DrawerTitle>
            {description && <DrawerDescription>{description}</DrawerDescription>}
          </DrawerHeader>
          <div className="flex-1 overflow-y-auto p-4">{children}</div>
          {footer && <DrawerFooter className="pb-[env(safe-area-inset-bottom)]">{footer}</DrawerFooter>}
        </DrawerContent>
      </Drawer>
    )
  }

  return (
    <Dialog {...rest}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          {description && <DialogDescription>{description}</DialogDescription>}
        </DialogHeader>
        {children}
        {footer && <DialogFooter>{footer}</DialogFooter>}
      </DialogContent>
    </Dialog>
  )
}
```

Padrão oficial documentado em https://ui.shadcn.com/docs/components/drawer#responsive.

**Aplicação**:
- `ModalConfirmacao`, `ModalEditarPerfis`, `ModalConfirmacaoStatusUsuario`: mantêm `Dialog` em todos viewports — curtos, sem listas editáveis.
- Modais UC07–UC13 (inclusão/edição de geometrias, pés-direitos, esquadrias, informação adicional): usam `<ResponsiveModal>` — são listas editáveis que em mobile se beneficiam do bottom-sheet tactile.
- Em listas longas dentro de `Drawer`, configurar `snapPoints` para limitar altura e permitir drag-to-expand.

### 15.10. `PesquisaBarAmbientes`

- Layout `grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3` — empilha em mobile, organiza em colunas a partir de `sm`.
- Botão "Limpar filtros": ícone `X` em mobile, ícone + texto em desktop.
- Inputs com `inputMode` apropriado quando aplicável.

### 15.11. `DetalheAmbiente` (UC02/UC05/UC19)

- **`≥lg`**: layout grid com sidebar de ações à direita (sticky).
- **`<lg`**: ações no topo como `<DropdownMenu>` ou como barra sticky no rodapé quando há ações contextuais (Publicar/Privar/Editar/Enviar p/ validação).
- Seções (dados básicos, geometrias, pés-direitos, esquadrias, informação adicional): `<Accordion>` em mobile para escaneabilidade vertical, apresentação linear em desktop.

### 15.12. Acessibilidade mobile

- Touch targets ≥ 44px em todos botões/links (`min-h-[44px]`).
- `focus-visible:ring-2` (shadcn já provê).
- Contraste WCAG AA (tokens shadcn já AA por default).
- `aria-label` em triggers de avatar/hamburger.
- Suporte a `prefers-color-scheme` é opcional (dark mode do shadcn já disponível — fora do scope deste documento).

### 15.13. Sem novas deps npm

Todas as peças usadas pertencem ao catálogo do shadcn: `avatar`, `sheet`, `drawer`, `dropdown-menu`, `accordion`, `dialog`, `badge`, `table`, `button`. O hook `useIsMobile` é fornecido automaticamente pela CLI do shadcn em `src/hooks/use-mobile.ts`.

---

## 16. Testes

| Camada | Ferramenta | Alvo |
|---|---|---|
| Funções puras de permissão | Vitest | `hasPermission`, `getRequiredRoles`, `matchRoute` |
| Hooks | `@testing-library/react` + `renderHook` | `usePermission`, `useAuth` |
| Componentes | `@testing-library/react` | `<RequireRole>` redirect, `<PermissionButton>`, `<UserMenu>` mobile/desktop variants |
| Interceptor Axios | `axios-mock-adapter`, Vitest | 401 → refresh → retry; falha de refresh → logout |
| E2E | Playwright | fluxo login → callback → /; UC01, UC06, UC22, UC25 |
| E2E mobile | Playwright (viewports 375×667, 390×844, 768×1024) | smoke de fluxos críticos em mobile: login → publicados → detalhe; UC22 cards; UC06 multistep |
| Regressão visual | Playwright `toHaveScreenshot` | `TabelaPadrao` cards; `DetalheAmbiente` mobile; `UserMenu` em ambos viewports |

Cobertura mínima recomendada: `lib/security/permissions.ts` 100%, `lib/security/auth.ts` 100% (trivial após remoção de `decodeJwt`), guards 100%.

---

## 17. Benefícios da arquitetura

1. **Segurança em múltiplas camadas**: rota (Nginx) → guard (React Router) → componente (`PermissionButton`) → hook (`usePermission`) → backend (autoridade máxima).
2. **Access token imune a XSS por exfiltração de storage**: vive apenas em memória; refresh em cookie HttpOnly. Frontend não decodifica o JWT — apenas transporta.
3. **CSRF tratado**: alinhado a Double Submit Cookie do backend; sem 403 em refresh/logout.
4. **Single source of truth espelhada**: `ROUTE_PERMISSIONS` reflete exatamente as authorities do `SecurityFilterChain` do backend, evitando drift.
5. **Type safety**: enum `Role` e interface `User` tipados; `PermissionButton` e guards genéricos.
6. **UX adaptativa**: menu, botões e ações filtrados por perfis.
7. **UX responsiva**: aplicação plenamente usável em 375px ou 1920px, com componentes mobile-first (`<ResponsiveModal>`, cards-vs-table, `<UserMenu>` em Sheet/DropdownMenu) sem duplicar lógica de negócio.
8. **Deploy enxuto**: estáticos + Nginx; sem processo Node em produção.
9. **Performance**: Vite build com code-splitting; TanStack Query com cache; Lazy loading por rota (`React.lazy`).
10. **Manutenibilidade**: estrutura Vite/RR familiar; deps mínimas; sem mental model de server components.

---

## 18. Tarefas de implementação (enfileiramento sugerido)

1. **Scaffold Vite + React + TS + Tailwind + shadcn/ui**; configurar `vite-tsconfig-paths` para alias `@/`. Tailwind v4 via plugin `@tailwindcss/vite` em `vite.config.ts` (não PostCSS); `@import "tailwindcss"` em `globals.css`. Instalar via shadcn CLI: `button input dialog dropdown-menu avatar sheet drawer accordion table badge card form sonner`.
2. **Backend**: `GET /api/usuarios/me` em `UsuarioController` já implementado, retornando `UsuarioRes` a partir do `@AuthenticationPrincipal jwt: Jwt` (`jwt.subject` = `Usuario.id`). Authority: qualquer autenticado. O `SecurityConfig.apiFilterChain` abre `GET /api/usuarios/me` para `authenticated()` antes da regra `ROLE_ADMINISTRADOR` para `/api/usuarios/**`.
3. **`lib/security/auth.ts` + `lib/api/api.ts`**: variável de módulo (apenas `set/get/clear` do token string), interceptores de request (auth + CSRF) e response (refresh 401).
4. **`lib/security/csrf.ts`**: token CSRF mascarado em memória + `ensureCsrfToken()` + `clearCsrfToken()`.
5. **`AuthProvider` + `RequireAuth`/`RequireRole`/`PublicOnly`**.
6. **`/login` + `/callback`**: fluxo OAuth2 → leitura de `#token` → `ensureCsrfToken` → `login` → `/`.
7. **`/ambientes/publicados`** (lista pública, UC21-FE) e **`/ambientes/publicados/:id`** (UC19-FE).
8. **`/usuarios`** (UC22–UC26-FE) — Administrador.
9. **`/ambientes/validacao`** e **`/ambientes/nao-publicados`** com `FormAmbiente` multistep e modais UC07–UC18.
10. **Responsividade**: `<UserMenu>` (Sheet + DropdownMenu), `<ProtectedNavigation>` variants Desktop/Mobile, `<ResponsiveModal>` para UC07–UC13, cards-vs-tabela em `TabelaPadrao`/`TabelaUsuarios`, `AcoesLote` barra fixa em mobile, `<Accordion>` em `DetalheAmbiente` mobile. Adicionar `body { position: relative; }` em `globals.css`.
11. **Nginx** config + `docker-compose` (frontend service).
12. **Testes**: `permissions`, `auth`, guards, interceptor Axios mockado; smoke E2E em viewports 375×667 e 1280×800.

---

## 19. Riscos

| Risco | Mitigação |
|---|---|
| Backend não implementa `/api/usuarios/me` em tempo | Degradar: frontend mostra "Carregando..." e campos limitados (sem nome). Não é a arquitetura final — é gap. |
| `SameSite=Lax` em redirect OAuth2 cross-site | Backend já configura `SameSite=Lax` para cookie de sessão (necessário no callback). Refresh cookie idem. Nginx mantém tudo na mesma origem em prod, eliminando cross-site. |
| XSS injeta script | Token em memória → não há onde ler. CSP estrito (apenas `'self'`, `'unsafe-inline'` para styles, scripts none) restringe injeção. Em produção, habilitar Trusted Types se o navegador suportar. |
| Token perdido depois de refresh concorrente | Lock via `refreshPromise` adquirido síncronamente em `lib/api/api.ts` (IIFE async atribuída antes de qualquer `await`); chamadas concorrentes compartilham a mesma promise. Teste unitário cobre rajada de 401 (ver `api.test.ts`). |
| Backend rotaciona refresh token (vida restante < accessExpiration) | Interceptor de response não chama `setRefreshToken` (cookie gerido por `Set-Cookie` do backend, navegador atualiza automaticamente). Comportamento alinhado a `seguranca.md` §3. |
| Adição de novo perfil | Adicionar ao enum `Role`; atualizar `ROUTE_PERMISSIONS`/`ACTION_PERMISSIONS`; sem refactor estrutural |
| Drawer em iOS Safari mais antigo | `body { position: relative; }` em `globals.css` resolve overlay; Base UI cobre iOS 12+. Se necessário, fallback para `Dialog` em viewport mínima. |
| Modais UC07–UC13 com listas longas em mobile | Configurar `snapPoints` no `Drawer` para limitar altura e permitir drag-to-expand; fallback de scroll com `flex-1 overflow-y-auto`. |
| Avatar sem foto real | Backend não provê `picture` do Google hoje; `<AvatarFallback>` com iniciais já cobre UX. Adicionar `picture` ao `Usuario` + endpoint é evolução futura. |

---

## 20. Referências

- [Vite](https://vitejs.dev/)
- [React Router v8](https://reactrouter.com/)
- [TanStack Query](https://tanstack.com/query/latest)
- [shadcn/ui](https://ui.shadcn.com/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Axios](https://axios-http.com/)
- [React Hook Form](https://react-hook-form.com/)
- [Zod](https://zod.dev/)
- [shadcn/ui — Drawer (incl. pattern Responsive com Dialog)](https://ui.shadcn.com/docs/components/drawer)
- [Base UI — Drawer (base do shadcn Drawer)](https://base-ui.com/react/components/drawer)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- Arquitetura do backend: [`arquitetura.md`](./arquitetura.md)
- Segurança do backend: [`seguranca.md`](./seguranca.md)
- Operação: [`operacao.md`](./operacao.md)
- Casos de uso frontend: [`ambientes-internos/casos-uso-frontend.md`](./ambientes-internos/casos-uso-frontend.md)