# Contexto dos Ambientes Internos

## Regras de Negócio

As regras a seguir complementam e detalhem as regras iniciais, incorporando a estrutura definida no modelo de domínio
atual e o contexto do domínio do IFCE.

### 1. Regras Gerais do Ambiente

* **RN-1.1:** Cada ambiente deve possuir um identificador único (`id`) no sistema para garantir a integridade e
  facilitar a gestão.
* **RN-1.2:** Todo ambiente deve ter um `nome` descritivo (ex: "Laboratório de Química") e uma `localizacao` associada a
  uma edificação ou bloco do campus (ex: "Bloco A").
* **RN-1.3:** O sistema deve permitir a geração de um identificador físico (QR Code) para cada ambiente, que, ao ser
  lido, direcionará o usuário para a tela de consulta com as informações daquele local.
* **RN-1.4:** Cada ambiente deve ser classificado em um tipo específico (ex: `SalaAula`, `Auditorio`, `SalaAdm`),
  refletido por sua classe concreta que herda de `Ambiente`.
* **RN-1.5:** Um ambiente deve conter listas de `Piso`, `Parede` (podendo ser `Externa` ou `Interna`), `Teto` e
  `Componente`, a fim de detalhar suas características construtivas e funcionais.
* **RN-1.6:** Para ser cadastrado, um ambiente deve ter, no mínimo, o `nome`, a `localização`, a `capacidade`, o `tipo`,
  pelo menos uma `geometria`, um `pé-direito` e uma `porta`; nenhum dos atributos pode ser nulo, vazio ou em branco.
* **RN-1.7:** Não pode haver dois ambientes com o mesmo `nome` na mesma `localização`.
* **RN-1.8:** Todo ambiente deve possuir um atributo `status` (enum) para controlar sua visibilidade nos diferentes
  casos de uso.
* **RN-1.9:** O sistema deve permitir a associação de múltiplas `geometrias` a um ambiente para representar sua forma
  física com precisão (ex: `retangular`, `triangular`, etc.).
* **RN-1.10:** A `capacidade` de um ambiente deve ser expressa em número inteiro, representando a quantidade máxima de
  pessoas que o ambiente pode acomodar confortavelmente.
* **RN-1.11:** O `pé-direito` de um ambiente refere-se à altura do piso ao teto e deve ser um valor positivo.
* **RN-1.12:** Todas as dimensões que representam `medidas lineares` (ex: base, altura, etc.) devem ser expressas em
  `metros` (m) e as `áreas` em `metros quadrados` (m²), respeitando a precisão de `duas casas decimais`.
* **RN-1.13:** O sistema deve fornecer mecanismos para calcular automaticamente a `área total` de um ou mais
  `Ambientes`, seus `Elementos Construtivos` e `Esquadrias`, com base nas geometrias associadas. No caso dos elementos
  construtivos e esquadrias, deve ser possível calcular a `área por tipo` (ex: área de paredes com pintura, área de
  janelas de vidro, etc.).
* **RN-1.14:** O sistema deve fornecer mecanismos para calcular a `potência total` instalada em um ou mais `Ambientes`,
  considerando os `Componentes` que consomem energia (ex: `Equipamentos` e `Luminárias`).

### 2. Regras sobre Elementos Construtivos e Esquadrias

* **RN-2.1:** Cada `Elemento Construtivo` (Piso, Parede e Teto) é definido por uma `quantidade ou repetição`, um
  `tipo ou material`, uma ou mais `geometrias` e atributos adicionais específicos.
* **RN-2.2:** A área total de um `Elemento Construtivo` (Piso, Parede, Teto) deve ser calculada a partir da soma das
  áreas de uma ou mais `geometrias` (ex: `retangular`, `triangular`, etc.), multiplicada pela `quantidade` do elemento.
* **RN-2.3:** A área de uma `Parede` deve descontar a área total das `Esquadrias` nela contidas.
* **RN-2.4:** Uma `Esquadria` (Porta ou Janela) possui uma `geometria` principal que define sua área total e uma lista
  de `Componentes` (ex: Folha, Bandeirola e Guarnicao).
* **RN-2.5:** Cada componente da esquadria possui sua própria `geometria`, `material` e atributos específicos (ex:
  `abertura` para `Folha` e `Bandeirola` e `espessura` para `Guarnicao`).
* **RN-2.6:** Uma `Janela` deve conter um `Peitoril`, que possuirá `altura do piso`, `material` e `geometria` próprios.
* **RN-2.7:** `Parede` e `Teto` devem obrigatoriamente possuir um `revestimento` associado.

### 3. Regras sobre Componentes

* **RN-3.1:** Um `Componente` representará todos os itens adicionais em um ambiente, possuindo pelo menos um `tipo` e
  uma `quantidade ou repetição`.
* **RN-3.2:** Componentes que consomem energia (ex: `Equipamentos` e `Luminárias`) devem permitir o cálculo de sua
  potência em Watts.
* **RN-3.3:** Um `Equipamento` é definido por seu `tipo` e `potencia em Watts` individual.
* **RN-3.4:** Uma `Luminária` é definida pelo seu `material predominante`, `tipo de fixacao`, `quantidade de lampadas` e
  se possui ou não `aletas`.
* **RN-3.5:** A `Lampada` deve compor uma Luminária e possuir as seguintes informações: `tipo`, `formato` e
  `potencia em Watts`.
* **RN-3.6:** Os componentes de instalação `Eletrica`, `Logica` e `Hidrossanitaria` envolvem itens como tomadas,
  interruptores, pias, sanitários, etc. Cada componente deste possui atributos específicos (ex: `capacidade` para
  elétrica, `pontos` para hidrossanitária).
* **RN-3.7:** Um `Extintor` é um `Componente` definido pelos seguintes atributos: `capacidade`, `unidade` (referente a
  capacidade), `tipo` e a lista de classes de incendio que ele combate.

### 4. Regras de Acesso e Gestão

* **RN-4.1:** Um **Validador** é responsável por gerenciar o estado de publicação dos ambientes. Suas ações incluem:
    * Listar todos os ambientes submetidos a validação, podendo pesquisá-los por nome, localização ou tipo.
    * Obter detalhes de um ambiente submetido a validação por ID.
    * Publicar um ambiente que foi submetido a validação.
    * Tornar um ambiente submetido a validação ou publicado como não público.
    * Deletar um ambiente publicado.

* **RN-4.2:** Usuários com perfil de **Gestor do Sistema** podem gerenciar ambientes que ainda não foram publicados.
  Suas ações incluem:
    * Listar todos os ambientes não publicados, podendo pesquisá-los por nome, localização ou tipo.
    * Obter detalhes de um ambiente não publicado por ID.
    * Cadastrar um novo ambiente como não publicado.
    * Atualizar os dados de um ambiente não publicado.
    * Deletar um ambiente não publicado.
    * Alterar o tipo e os dados de um ambiente não publicado.
    * Enviar um ou mais ambientes não publicados para o fluxo de validação e publicação.

* **RN-4.3:** Usuários com perfil de **Colaborador** podem apenas consultar informações completas dos ambientes que
  estão publicados. Suas ações incluem:
    * Obter detalhes completos de um ambiente publicado por ID.
    * Consultar, de forma paginada, as esquadrias de um conjunto de ambientes publicados.
    * A consulta por ID (`GET /api/ambientes/{path}/{id}`) é exigida para todos os ambientes, inclusive publicados,
      e exige `ROLE_COLABORADOR` (autorização implementada no método `obterAmbientePorId` de `BaseController`).

* **RN-4.4:** Usuários identificados como **Público Externo** podem apenas consultar informações simplificadas dos
  ambientes que estão publicados. Suas ações incluem:
    * Listar informações básicas de todos os ambientes publicados, podendo pesquisá-los por nome, localização ou tipo.
    * O acesso a esta listagem e aos respectivos detalhes ocorre **sem autenticação** (rota pública), não exigindo login
      via Google; o sistema deve garantir que nenhum dado sensível de ambientes não publicados seja exposto nesse
      caminho.

* **RN-4.5:** O login no sistema é feito **exclusivamente** via Google, utilizando o fluxo **OAuth 2.0 — Authorization
  Code com PKCE**, com o backend atuando como *broker*. Não há login por usuário/senha local e nenhuma outra identidade
  externa é aceita nesta fase.

* **RN-4.6:** E-mails com domínio `@ifce.edu.br` configuram o acesso **institucional**: no primeiro login, o usuário
  é provisionado automaticamente com o perfil `ROLE_COLABORADOR`, desde que a conta Google esteja válida.
  Adicionalmente, no primeiro login o `CustomOAuth2UserService` sincroniza o `nome` do usuário a partir do atributo
  `name` retornado pelo Google (substituindo valores placeholder como o email).

* **RN-4.7:** Usuários externos (e-mail fora de `@ifce.edu.br`) só obtêm acesso se estiverem **pré-cadastrados** por
  um Administrador. Caso contrário, o sistema recusa o login retornando `403 Forbidden`, sem criar registro pendente.

* **RN-4.8:** Um mesmo usuário pode acumular **múltiplos perfis** simultâneos, conforme o conjunto:
  `ROLE_COLABORADOR`, `ROLE_VALIDADOR`, `ROLE_GESTOR_SISTEMA`, `ROLE_ADMINISTRADOR`. Todo usuário deve manter, no
  mínimo, `ROLE_COLABORADOR` (regra universal cumulativa). O `UsuarioService.atualizarPerfis` sempre força a
  presença de `ROLE_COLABORADOR` no conjunto final, mesmo que o solicitante tente removê-lo.

* **RN-4.9:** **Lockout prevention:** é vedado remover `ROLE_ADMINISTRADOR` ou desativar o usuário quando existir
  **apenas um** Administrador ativo no sistema. Nessas condições, a operação deve ser rejeitada com `409 Conflict` e
  mensagem de negócio clara, preservando ao menos um Administrador ativo. Implementado em
  `UsuarioService.verificarExclusaoAdm()`.

* **RN-4.10:** O **JWT próprio** emitido pelo backend é o credencial de acesso à API, enquanto o **refresh token** é
  entregue em cookie `HttpOnly`, `Secure` e `SameSite=Strict`, com **rotação** a cada uso. O acesso aos endpoints
  protegidos é feito pelo header `Authorization: Bearer <access_token>`. As durações do access token e do refresh
  token são configuráveis externamente (ver RN-4.13).

* **RN-4.11:** O **administrador institucional** é provisionado no boot pelo `BootstrapAdminRunner`, lendo a env var
  `BOOTSTRAP_ADMIN_EMAIL`. Se a env var não estiver configurada, o sistema aborta a inicialização com
  `IllegalStateException`. O runner é idempotente:
    * Se o usuário não existir no banco, é criado com `ROLE_ADMINISTRADOR` + `ROLE_COLABORADOR` e nome placeholder igual
      ao email.
    * Se existir, estiver inativo e a flag `BOOTSTRAP_ALLOW_REACTIVATE` for `true`, o usuário é reativado e os perfis
      são garantidos.
    * Se existir, estiver ativo e já tiver `ROLE_ADMINISTRADOR`, é no-op silencioso.
    * Se existir, estiver ativo mas sem `ROLE_ADMINISTRADOR`, é promovido e logado em `WARN`.
  Em todos os casos o nome do `Usuario` é mantido o que estiver (placeholder ou nome real vindo de login Google
  anterior).

* **RN-4.12:** A flag `BOOTSTRAP_ALLOW_REACTIVATE` (default `true`) funciona como **kill switch geral** do
  `BootstrapAdminRunner`. Quando `false`, o runner é um no-op total: não cria, não reativa, não promove. Útil
  quando o operador precisa de uma desativação permanente do admin institucional sem ter que remover a env var
  `BOOTSTRAP_ADMIN_EMAIL`.

* **RN-4.13:** As durações do access token e do refresh token são configuráveis externamente, em segundos:

    | Property | Env var | Default | Unidade |
    |---|---|---|---|
    | `jwt.access-token-expiration` | `JWT_ACCESS_TOKEN_EXPIRATION` | `900` (15 min) | segundos |
    | `jwt.refresh-expiration` | `JWT_REFRESH_EXPIRATION` | `43200` (12 h) | segundos |
    | `jwt.cookie-secure` | `JWT_COOKIE_SECURE` | `true` | boolean |

    O `maxAge` do cookie HttpOnly de refresh é derivado de `jwt.refresh-expiration`, garantindo coerência entre a
    vida do token persistido e a vida do cookie. A flag `cookie-secure` deve ser `false` apenas em ambiente de
    desenvolvimento local (HTTP). O profile `dev` no `application-dev.yml` já define `cookie-secure: false`.
