# Casos de Uso — Frontend (Ambientes Internos)

Este documento traduz os casos de uso do backend (ver `docs/ambientes-internos/casos-uso.md`) em fluxos, telas e comportamentos esperados do frontend. O foco é a experiência do usuário, interações de UI, validações no cliente, estados e critérios de aceitação.

## Visão Geral

- Público-alvo: Validador, Gestor do Sistema, Colaborador, Público Externo, Administrador.
- Objetivo: mapear cada caso de uso do backend para telas, componentes e interações no frontend.
- Regras gerais de UI:
  - Paginação padrão: 100 itens por página (conforme backend), com opções de navegação e busca por texto.
  - Filtros e buscas aplicam-se client-side quando possível; senão, por chamadas à API acionadas por botão (padrão `PesquisaBarAmbientes`) — sem debounce.
  - Todos os formulários mostram erros inline e mensagens de sucesso via snackbar/toast.
  - Acessibilidade: formulários navegáveis por teclado, rótulos (`label`) para campos e contrastes adequados.

---

## Componentes

- TabelaPadrao — exibe Nome, Localização, Tipo, Capacidade, Área; suporte a paginação, ordenação e seleção múltipla.
- AguadandoValidacao - tela específica para listar ambientes aguardando validação.
- NaoPublicados - tela específica para listar ambientes não publicados.
- Publicados - tela específica para listar ambientes publicados.
- PesquisaBarAmbientes — inputs para filtrar ambientes por `nome`, `localizacao`, `tipo` e botão para limpar filtros.
- DetalheAmbiente — visão detalhada com geometrias, pés-direitos, lista de esquadrias (portas/janelas) e áreas.
- FormAmbiente (multistep) — criação/atualização de ambiente: dados básicos, geometrias, pés-direitos, esquadrias, informação adicional.
- ModalConfirmacao — confirmações para deletar, publicar/privar, duplicar.
- ModalFormulario — shell padrão dos modais de edição (UC07–UC17): Dialog + validação Zod + tratamento de erro via useAsyncAction; montagem condicional na página de detalhe.
- ModalGeometrias / ModalPesDireitos / ModalEsquadrias — modais genéricos de lista editável (inclusão/edição, UC08–UC13), configurados por UC na página de detalhe (`modo`, `titulo`, `inicial`, `onSubmit`).
- AcoesLote — ações em lote (ex: deletar, enviar para validação e publicar/privar) com checagem de permissões.
- Toast/Snackbar — mensagens de sucesso/erro.
- PaginaLogin — página de login com logo/descrição do sistema, botão "Entrar com Google" e mensagens de erro.
- TabelaUsuarios — exibe ID, Email, Nome, Ativo, CriadoEm, Perfis; suporte a paginação e ações individuais.
- PesquisaBarUsuarios — input para filtrar usuários por `nome`.
- ModalEditarPerfis — modal com checkboxes para selecionar roles (COLABORADOR, VALIDADOR, GESTOR_SISTEMA, ADMINISTRADOR).
- ModalConfirmacaoStatusUsuario — confirmação para desativar/ativar usuário.

---

## Rotas

- `/login` — página de login com botão Google OAuth2 (UC28-FE).
- `/ambientes/validacao` — lista de ambientes aguardando validação (UC01-FE).
- `/ambientes/validacao/{id}` — detalhes de um ambiente aguardando validação (UC02-FE).
- `/ambientes/nao-publicados` — lista de ambientes não publicados (UC04-FE).
- `/ambientes/nao-publicados/novo` — criação de novo ambiente não publicado (UC06-FE).
- `/ambientes/nao-publicados/{id}` — detalhes de um ambiente não publicado (UC05-FE).
- `/ambientes/publicados` — lista de ambientes publicados (UC21-FE).
- `/ambientes/publicados/{id}` — detalhes de um ambiente publicado (UC19-FE).
- `/usuarios` — lista de usuários (UC22-FE).

---

## Ator: Validador (UI)

### UC01-FE: Listar Ambientes Aguardando Validação

- Tela: `Ambientes > AguardandoValidação` (rota `/ambientes/validacao`).
- Pré-condições: Usuário logado com role `validador`.
- Fluxo principal (UI):
  1. O usuário acessa a rota `/ambientes/validacao`, é exibido o componente `TabelaPadrao` com os ambientes cujo `status = AGUARDANDO_VALIDACAO` (chamada GET `/api/ambientes/validacao`).
  2. Uma barra de pesquisa (`PesquisaBarAmbientes`) também é exibida para filtrar os ambientes por `nome`, `localizacao` e `tipo`. Cada filtro utiliza endpoints específicos do backend: `/api/ambientes/validacao/nome?nome={nome}`, `/api/ambientes/validacao/localizacao?bloco={bloco}&unidade={unidade}&andar={andar}` e `/api/ambientes/validacao/tipo?tipo={tipo}` — filtro aplicado via botão "Aplicar", sem debounce, estado na URL.
  3. A Tabela exibida possui paginação e o usuário pode definir quantos registros serão exibidos até o máximo de 100. 
  4. O usuário também pode ordenar os resultados por qualquer uma das colunas da tabela.
  5. Cada item tem um botão para visualizar seus detalhes (`DetalheAmbiente`).
- Estados e erros:
  - Se não houver itens, mostrar callout informativo.
  - Em erro de rede, show toast com opção `Tentar novamente`.
- Critérios de aceitação:
  - Filtros retornam apenas itens com `status = AGUARDANDO_VALIDACAO`.
  - Os filtros são aplicados via botão "Aplicar" (sem debounce) e o estado (filtro/página) fica na URL.
  - Paginação funciona e mantém filtros no estado da URL (query params).

### UC02-FE: Detalhes de um Ambiente Aguardando Validação

- Tela: `DetalheAmbiente` (rota `/ambientes/validacao/{id}`).
- Pré-condições: selecionou um ambiente da lista e está logado com role `validador`.
- Fluxo principal (UI):
  1. O usuário clica no botão para ver os detalhes de um ambiente na lista, navegando para a rota `/ambientes/validacao/{id}`. 
  1. Uma requisição GET é realizada para `/api/ambientes/validacao/{id}` e a tela `DetalheAmbiente` é exibida com todos os campos disponíveis para ambiente (geometrias, áreas, pés-direitos, esquadrias e áreas).
  2. As seguintes ações são exibidas na pagina: `Publicar`, `Privar` (quando aplicável), `Voltar` e icones para outras ações referentes aos atributos do ambiente (quando aplicavel).
- Estados e erros:
  - Se ambiente não encontrado, exibir mensagem e botão `Voltar à lista`.
- Critérios de aceitação:
  - Todos os atributos do ambiente exibidos correspondem aos retornos do backend.

### UC03-FE: Gerenciar Publicação de Ambientes

- Componente: Ações `Publicar` e `Privar` disponíveis em `DetalheAmbiente`.
- Pré-condições: Usuário logado com role `validador`.
- Fluxo principal (UI):
  1. Publicar: O usuário clica no botão `Publicar` em `DetalheAmbiente` (ambiente com `status = AGUARDANDO_VALIDACAO`), um `ModalConfirmacao` é aberto para confirmação, em seguida, é realizada uma requisição PATCH para `/api/ambientes/validacao/{id}/publicar` e um toast é exibido com a mensagem de sucesso ou erro.
  2. Privar: O usuário clica no botão `Privar` em `DetalheAmbiente` (ambiente com `status = PUBLICADO` ou `AGUARDANDO_VALIDACAO`), um `ModalConfirmacao` é aberto para confirmação, em seguida, é realizada uma requisição PATCH para `/api/ambientes/validacao/{id}/privar` e um toast é exibido com a mensagem de sucesso ou erro.
  3. O usuário é encaminhado a lista de ambientes atualizada após a ação.
- Estados e erros:
  - Sucesso: toast com texto apropriado e atualização da lista.
  - Erro: toast com detalhes retornados pelo backend.
- Critérios de aceitação:
  - Botão `Publicar` desabilitado se o ambiente não estiver com `status = AGUARDANDO_VALIDACAO`.
  - Botão `Privar` desabilitado se o ambiente não estiver com `status = PUBLICADO` ou `AGUARDANDO_VALIDACAO`.
  - Lista atualizada corretamente após a ação.

---

## Ator: Gestor do Sistema (UI)

### UC04-FE: Listar Ambientes Não Publicados

- Tela: `Ambientes > NãoPublicados` (rota `/ambientes/nao-publicados`) (semelhante ao UC01-FE). Filtragem, paginação e seleção múltipla.
- Pré-condições: Usuário logado com role `gestor`.
- Fluxo principal (UI): Similar ao UC01-FE, mas chamando GET `/api/ambientes/nao-publicados` e possuindo um botão `Criar Novo` para criação de novos ambientes. Os filtros utilizam endpoints específicos: `/api/ambientes/nao-publicados/nome?nome={nome}`, `/api/ambientes/nao-publicados/localizacao?bloco={bloco}&unidade={unidade}&andar={andar}` e `/api/ambientes/nao-publicados/tipo?tipo={tipo}`.
  1. Cada item tem um checkbox para seleção múltipla, além do botão para visualizar seus detalhes (`DetalheAmbiente`).
  2. Um botão de seleção também está disponível para execução de ações em lote (`AcoesLote`): `Deletar` (UC15-FE) e `Enviar p/ Validação` (UC18-FE).
- Estados e erros: Os mesmos do UC01-FE.
- Critérios de aceitação: Os mesmos do UC01-FE, mas para `status = NAO_PUBLICADO`.

### UC05-FE: Detalhes de um Ambiente Não Publicado

- Tela: `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`).
- Pré-condições: Selecionar um ambiente da lista e estar logado com a role `gestor`.
- Fluxo principal (UI): Similar ao UC02-FE, mas com ações adicionais para `Editar`, `Duplicar`, `Alterar Tipo`, `Enviar para validação`.
- Estados e erros: Os mesmos do UC02-FE.
- Critérios de aceitação: Os mesmos do UC02-FE.

### UC06-FE: Cadastrar um Novo Ambiente Não Publicado

- Tela: `FormAmbiente` acionado pelo botão `Criar Novo` em `Ambientes > NãoPublicados` (rota `/ambientes/nao-publicados/novo`).
- Pré-condições: Usuário logado com role `gestor`.
- Fluxo principal (UI):
  - Multistep (formulário dividido em etapas):
    1. Dados básicos: `nome`, `localizacao`, `tipo`, `capacidade`.
    2. Geometrias: lista editável com `tipo`, `repetição`, `comprimento` e `largura` ou `base` e `altura`.
    3. Pés-direitos: lista editável com alturas.
    4. Esquadrias: lista com `tipo`, `repetição`, `largura`, `altura`, `material`, `peitoril`, `info adicional`.
    5. Informação Adicional: campo de texto opcional (`informacaoAdicional` do ambiente), máx. 255 caracteres. Pode ser deixado vazio e preenchido depois via UC14-FE (`ModalInfoAdicional` no detalhe).
    6. Exibir erros inline para cada etapa conforme validações.
    7. Botão `Salvar` para submeter o formulário completo. (requisição POST para `/api/ambientes/nao-publicados`).
    8. Desabilitar botão `Salvar` e mostrar estado de loading durante a submissão.
- Validações client-side:
  - Campos obrigatórios (dados básicos, pelo menos uma geometria, pelo menos uma porta nas esquadrias).
  - Informação adicional do ambiente: opcional; quando preenchida, máx. 255 caracteres (espelha `@Size(max = 255)` de `AmbienteReq.kt`).
  - Regras RN-1.6 / RN-1.7 aplicadas no cliente quando possível.
- Estados e erros:
  - Durante submissão, mostrar loading e desabilitar botões.
  - Ao sucesso, exibir toast de sucesso e redirecionar para `DetalheAmbiente` do novo ambiente.
  - Em erro, exibir toast com mensagem do backend.
- Critérios de aceitação:
  - Novo ambiente criado com `status = NAO_PUBLICADO` e redireciona para `DetalheAmbiente` do novo registro.

### UC07-FE: Atualizar dados básicos de um Ambiente Não Publicado

- Modal: Modal acionado pelo botão `Editar Dados Básicos` em `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`).
- Pré-condições: Usuário logado com role `gestor` e ambiente com `status = NAO_PUBLICADO`.
- Fluxo principal (UI): 
  1. O usuário abre um modal com formulário simples contendo os campos `nome`, `localizacao` e `capacidade`.
  2. O usuário atualiza os campos, cada um é validado no cliente e exibem erros inline conforme validações.
  3. O usuário submete as alterações clicando em `Salvar` (requisição PATCH para `/api/ambientes/nao-publicados/{id}/dados-basicos`).
- Validações client-side:
  - Regras RN-1.6 / RN-1.7 aplicadas no cliente quando possível.
- Estados e erros:
  - Durante submissão, mostrar loading e desabilitar botões.
  - Em erro, exibir toast com mensagem do backend.
  - Em sucesso, atualizar dados na tela e mostrar toast de sucesso.
- Critério de aceitação: dados atualizados corretamente no backend e refletidos na UI.

### UC08-FE: Incluir Geometrias

- Modal: Modal acionado por meio de um botão com ícone referente a inclusão de Geometrias em `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`).
- Pré-condições: Usuário logado com role `gestor` e ambiente com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. O usuário clica no ícone de inclusão de geometrias.
  2. Em um Formulário simples, preenche `tipo`, `comprimento`, `largura` (ou `base` e `altura`) e repetição.
  3. Novos campos podem ser adicionados para inclusão múltipla antes de submeter.
  4. Submete a inclusão e uma requisição PATCH é enviada para `/api/ambientes/nao-publicados/{id}/geometrias/incluir`.
  5. Ao sucesso, a lista de geometrias e suas áreas são atualizadas e exibido um toast de sucesso.
- Estados e erros:
  - Se faltar campos obrigatórios, mostrar erros inline e impedir submissão.
  - Em erro de rede ou validação do backend, exibir toast com detalhes e manter o formulário aberto para correção.
- Critérios de aceitação:
  - A requisição enviada contém todos os campos obrigatórios e a área exibida localmente corresponde ao cálculo do backend.
  - Após inclusão bem-sucedida, a UI mostra a geometria na lista e atualiza a área total do ambiente.
  - Pelo menos uma geometria deve estar presente antes de permitir submissão (cliente valida).

### UC09-FE: Atualizar / Remover Geometrias

- Modal: Modal acionado por meio de um botão com ícone referente a edição de Geometrias em `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`).
- Pré-condições: Usuário logado com role `gestor` e ambiente com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. O usuário clica no ícone de edição de geometrias.
  2. Um formulario pré-preenchido com as informações atuais é exibido.
  3. O usuário pode optar por editar as geometrias ou removê-las.
  4. Para finalizar a edição, o usuário clica em `Salvar` e uma requisição PATCH é enviada para `/api/ambientes/nao-publicados/{id}/geometrias/atualizar`.
  5. Um `ModalConfirmacao` é exibido para confirmar a edição/ remoção.
  6. Ao sucesso, a lista de geometrias e suas áreas são atualizadas e um toast de sucesso é exibido.
- Estados e erros:
  - Não permitir remoção se resultaria em zero geometrias (cliente valida e bloqueia ação).
  - Em caso de erro do backend, exibir mensagem com indicação do item que falhou.
- Critérios de aceitação:
  - Atualizações e remoções persistem no backend e são refletidas imediatamente na UI com recalculo das áreas.
  - Não é possível remover todas as geometrias, mantendo pelo menos uma.

### UC10-FE: Incluir Pés-direitos

- Modal: Modal acionado por meio de um botão com ícone referente a inclusão de Pés-direitos em `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`).
- Pré-condições: Usuário logado com role `gestor` e ambiente com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. Usuário clica no ícone de inclusão de pés-direitos.
  2. Em um Formulário simples, preenche a altura do pé-direito.
  3. Novos campos podem ser adicionados para inclusão múltipla antes de submeter.
  4. Submete a inclusão e uma requisição PATCH é enviada para `/api/ambientes/nao-publicados/{id}/pes-direitos/incluir`.
  5. Ao sucesso, lista de pés-direitos é atualizada e toast de sucesso é exibido.
- Estados e erros:
  - O valor do pé-direito deve ser > 0; mostrar erro inline se inválido (cliente valida).
  - Em erro do backend, exibir toast com mensagem e manter dados para correção.
- Critérios de aceitação:
  - Após inclusão bem-sucedida, a UI mostra os novos pés-direitos na lista.
  - Pelo menos um pé-direito deve estar presente antes de permitir submissão (cliente valida).

### UC11-FE: Atualizar / Remover Pés-direitos

- Modal: Modal acionado por meio de um botão com ícone referente a edição de Pés-direitos em `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`).
- Pré-condições: Usuário logado com role `gestor` e ambiente com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. O usuário clica no ícone de edição de pés-direitos.
  2. Um formulario pré-preenchido com as informações atuais é exibido.
  3. O usuário pode optar por editar os pés-direitos ou removê-los.
  4. Para finalizar a edição, o usuário clica em `Salvar` e uma requisição PATCH é enviada para `/api/ambientes/nao-publicados/{id}/pes-direitos/atualizar`.
  5. Um `ModalConfirmacao` é exibido para confirmar a edição/ remoção.
  6. Ao sucesso, lista de pés-direitos é atualizada e toast de sucesso é exibido.
- Estados e erros:
  - Não permitir submissão com valores inválidos; mostrar erros inline (cliente valida).
  - Em erro de backend, mostrar mensagem especificando o problema.
- Critérios de aceitação:
  - Alterações e remoções persistem no backend e são refletidas na UI.
  - Não é possível remover todos os pés-direitos, mantendo pelo menos um.

### UC12-FE: Incluir Esquadrias

- Modal: Modal acionado por meio de um botão com ícone referente a inclusão de Esquadrias em `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`).
- Pré-condições: Usuário logado com role `gestor` e ambiente com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. O usuário clica no ícone de inclusão de esquadrias.
  2. Em um Formulário simples, preenche `tipo`, `largura`, `altura`, `material`, `peitoril` (opcional), `info adicional` e `repetição`.
  3. Novos campos podem ser adicionados para inclusão múltipla antes de submeter.
  4. Submete a inclusão e uma requisição PATCH é enviada para `/api/ambientes/nao-publicados/{id}/esquadrias/incluir`.
  5. Ao sucesso, a lista de esquadrias e suas áreas são atualizadas e exibido um toast de confirmação.
- Estados e erros:
  - Se faltar campos obrigatórios, mostrar erros inline e impedir submissão (cliente valida).
  - Em erro de rede ou validação do backend, exibir toast com detalhes e manter o formulário aberto para correção.
- Critérios de aceitação:
  - A requisição enviada contém todos os campos obrigatórios e a área da esquadria exibida localmente corresponde ao cálculo do backend.
  - Após inclusão bem-sucedida, a UI mostra as esquadrias na lista e atualiza suas áreas.
  - Pelo menos uma esquadria deve estar presente antes de permitir submissão (cliente valida).

### UC13-FE: Atualizar / Remover Esquadrias

- Modal: Modal acionado por meio de um botão com ícone referente a edição de Esquadrias em `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`).
- Pré-condições: Usuário logado com role `gestor` e ambiente com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. O usuário clica no ícone de edição de esquadrias.
  2. Um formulario pré-preenchido com as informações atuais é exibido.
  3. O usuário pode optar por editar as esquadrias ou removê-las.
  4. Para finalizar a edição, o usuário clica em `Salvar` e uma requisição PATCH é enviada para `/api/ambientes/nao-publicados/{id}/esquadrias/atualizar`.
  5. Um `ModalConfirmacao` é exibido para confirmar a edição/ remoção.
  6. Ao sucesso, a lista das esquadrias e suas áreas são atualizadas e um toast de sucesso é exibido.
- Estados e erros:
  - Não permitir remoção se resultaria em zero esquadrias (cliente valida e bloqueia ação).
  - Em caso de erro do backend, exibir mensagem com indicação do item que falhou.
- Critérios de aceitação:
  - Atualizações e remoções persistem no backend e são refletidas imediatamente na UI com recalculo das áreas.
  - Não é possível remover todas as esquadrias, mantendo pelo menos uma.

### UC14-FE: Atualizar Informação Adicional

- Componente: Input acionado por meio de um botão com ícone referente a edição de Informação Adicional em `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`).
- Pré-condições: Usuário logado com role `gestor` e ambiente com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. Usuário clica no ícone de edição das informações adicionais e clica em ícone de salvar.
  2. Submete PATCH `/api/ambientes/nao-publicados/{id}/informacao-adicional`.
  3. Ao sucesso, exibir toast e atualizar o campo na UI.
- Estados e erros:
  - Validar tamanho máximo do texto no cliente; mostrar aviso se exceder.
  - Em erro do backend, exibir mensagem e manter o conteúdo editável.
- Critérios de aceitação:
  - Informação adicional atualizada no backend e visível na UI após persistência.

### UC15-FE: Deletar Ambientes Não Publicados

- Componente: Ação acionada por botão `Deletar` disponível em `DetalheAmbiente` e em `AcoesLote` na tela `NaoPublicados`.
- Pré-condições: Usuário logado com role `gestor` e ambiente(s) selecionado(s) com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. Usuário clica em botão ou ícone de deleção e confirma a solicitação em `ModalConfirmacao` com aviso sobre remoção permanente.
  2. Após confirmação uma requisição DELETE é enviada para `/api/ambientes/nao-publicados` com lista de IDs.
  3. Ao sucesso, itens removidos somem da lista e toast de sucesso é exibido.
- Estados e erros:
  1. Se algum ID não puder ser deletado, mostrar erro indicando quais IDs falharam e motivo.
  2. Em caso de erro parcial, permitir retry por meio de um modal para os itens que falharam.
- Critérios de aceitação:
  - Ambientes removidos do backend e atualizados na UI; mensagens claras em falhas parciais.

### UC16-FE: Alterar Tipo e Dados de Ambientes Não Publicados

- Modal: Ação acionada por botão `Alterar tipo` em `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`).
- Pré-condições: Usuário logado com role `gestor` e ambiente com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. Usuário fornece novo `tipo` e dados complementares no formulário exibido.
  2. O usuário clica em salvar e os dados são submetidos via requisição POST para `/api/ambientes/nao-publicados/{id}` para criar novo ambiente do tipo especificado.
  3. Caso a operação seja bem-sucedida o usuário é redirecionado para `DetalheAmbiente` do novo registro.
- Estados e erros:
  1. Alertar que operação criará um novo registro e removerá o antigo; exigir confirmação explícita.
  2. Em erro, manter o usuário na tela com mensagens do backend.
- Critérios de aceitação:
  - Novo ambiente criado com os dados esperados; ambiente antigo removido conforme contrato do backend.

### UC17-FE: Duplicar Ambiente Não Publicados

- Modal: Ação acionada por botão `Duplicar` em `DetalheAmbiente` (rota `/ambientes/nao-publicados/{id}`) que abre modal para `nome` e `localizacao` do novo ambiente.
- Pré-condições: Usuário logado com role `gestor` e ambiente com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. Usuário fornece `nome`/`localizacao` no modal e confirma.
  2. Uma requisição POST é enviada para `/api/ambientes/nao-publicados/{id}/duplicar`.
  3. Ao sucesso, redirecionar para `DetalheAmbiente` do novo registro e exibir toast.
- Estados e erros:
  1. Validar nome/localizacao obrigatórios no cliente.
  2. Em erro de persistência, exibir mensagem com opção de tentar novamente.
- Critérios de aceitação:
  - Novo registro criado e exibido; dados copiados conforme esperado do original.
  - Não permitir duplicação se o nome ou localização estiverem vazios (cliente valida) ou se um deles já existe (backend valida).

### UC18-FE: Enviar Ambientes para Publicação

- Componente: Ação acionada por botão `Enviar para publicação` em `DetalheAmbiente` e na tela `NãoPublicados` por meio de `AcoesLote`.
- Pré-condições: Usuário logado com role `gestor` e itens selecionados com `status = NAO_PUBLICADO`.
- Fluxo principal (UI):
  1. Usuário seleciona os ambientes e clica em `Enviar para publicação`.
  2. Confirmação abre `ModalConfirmacao` e, ao confirmar, envia PATCH `/api/ambientes/nao-publicados/validar` com lista de IDs.
  3. Ao sucesso, itens atualizam para `AGUARDANDO_VALIDACAO` e toast é exibido.
- Estados e erros:
  1. Bloquear ação se algum item selecionado tiver status diferente; mostrar aviso explicativo.
  2. Em erro parcial, informar quais IDs falharam e o motivo.
- Critérios de aceitação:
  - Todos os IDs válidos passam para `AGUARDANDO_VALIDACAO`; mensagens claras em caso de falhas.

---

## Ator: Colaborador (UI)

### UC19-FE: Detalhes de um Ambiente Publicado

- Tela: `DetalheAmbiente` (rota `/ambientes/publicados/{id}`).
- Pré-condições: selecionou um ambiente da lista e está logado com role `colaborador` (autenticado, RN-4.5).
- Fluxo principal (UI): Similar ao UC02-FE, mas sem ações adicionais.
- Estados e erros: Os mesmos do UC02-FE.
- Critérios de aceitação: Os mesmos do UC02-FE.

### UC20-FE: Obter Detalhes de Esquadrias de uma lista de Ambientes Publicados

- Tela: Tela `Publicados` por meio de `AcoesLote`, ação `Detalhes Esquadrias`.
- Pré-condições: Usuário logado com role `colaborador` (autenticado, RN-4.5) e ambientes com `status = PUBLICADO` selecionados.
- Fluxo principal (UI):
  1. O usuário seleciona um ou mais ambientes na lista e seleciona `Detalhes Esquadrias`.
  2. O frontend envia `GET /api/ambientes/publicados/esquadrias?ids=1,2,3` passando a lista de ids por parâmetro.
  3. O backend retorna os detalhes das esquadrias de forma paginada (100 registros por página), incluindo `dadosPaginacao`; o frontend exibe:
     - Nome e Localização de cada ambiente consultado;
     - Lista de esquadrias de cada ambiente com: tipo, largura, altura, material, peitoril (se houver), info adicional (se houver), área da esquadria;
     - Resumo por Tipo e Material para cada ambiente;
     - Resumo global de Esquadrias por Tipo e Material (`totalTipoMaterial`): tipo, material e área total agregada de todos os ambientes da página.
  4. A interface possui paginação para navegar entre os resultados.

- Estados e erros:
  - Se algum ID não corresponder a um ambiente publicado, exibir aviso listando os IDs inválidos e permitir excluir/re-tentar apenas os válidos.
  - Se nenhum resultado for encontrado para os filtros aplicados, exibir callout informando "Nenhuma esquadria encontrada" e sugerir remoção/ajuste dos filtros.
  - Em erro de rede ou erro do backend, exibir toast com a mensagem e opção de tentar novamente.

- Critérios de aceitação:
  - A interface permite ambientes e opcionalmente adicionar filtros por `tipo` e `material`.
  - Os dados exibidos incluem nome/localização por ambiente, lista de esquadrias com os campos esperados, resumo por tipo e material para cada ambiente e resumo global consolidado (`totalTipoMaterial`).
  - Paginação funciona corretamente com `dadosPaginacao`.

---

## Ator: Público Externo (UI)

### UC21-FE: Listar Ambientes Publicados

- Tela: `Ambientes > Publicados` (semelhante ao UC01-FE). Filtragem e paginação.
- Pré-condições: Acesso público (não autenticado).
- Fluxo principal (UI): Similar ao UC01-FE, mas chamando GET `/api/ambientes/publicados` e não possuindo botões de ação ou seleção. Os filtros utilizam endpoints específicos: `/api/ambientes/publicados/nome?nome={nome}`, `/api/ambientes/publicados/localizacao?bloco={bloco}&unidade={unidade}&andar={andar}` e `/api/ambientes/publicados/tipo?tipo={tipo}`.
- Estados e erros: Os mesmos do UC01-FE.
- Critérios de aceitação: Os mesmos do UC01-FE, mas para `status = PUBLICADO`.

---

## Ator: Administrador (UI)

### UC22-FE: Listar Usuários

- Tela: `Administracao > Usuarios` (rota `/usuarios`).
- Pré-condições: Usuário logado com role `administrador`.
- Fluxo principal (UI):
  1. O usuário acessa a rota `/usuarios`, é exibido o componente `TabelaUsuarios` com a lista de usuários do sistema (chamada GET `/api/usuarios`).
  2. Uma barra de pesquisa (`PesquisaBarUsuarios`) é exibida para filtrar usuários por `nome` (endpoint `/api/usuarios/nomes/{nome}`; filtro aplicado via botão "Buscar", sem debounce; filtro e página compartilháveis pela URL).
  3. A tabela possui paginação com seletor de tamanho de página (10/20/50/100 registros, default 20) e exibe as colunas: ID, Email, Nome, Ativo, CriadoEm, Perfis.
  4. Cada linha possui botões de ação individual: `Editar Perfis`, `Desativar`/`Ativar`.
- Estados e erros:
  - Se não houver usuários, mostrar callout informativo.
  - Em erro de rede, mostrar toast com opção `Tentar novamente`.
- Critérios de aceitação:
  - A tabela exibe todos os usuários com paginação funcional.
  - Filtro por nome funciona corretamente.
  - Colunas exibem os dados corretos: ID, Email, Nome, Ativo (status visual), CriadoEm, Perfis (badges/tags).

### UC23-FE: Obter Usuário por Email

- Componente: Ação de busca na `TabelaUsuarios` ou navegação direta.
- Pré-condições: Usuário logado com role `administrador`.
- Fluxo principal (UI):
  1. O usuário pode buscar um usuário específico por email via campo de busca ou navegação.
  2. O frontend envia GET `/api/usuarios/email/{email}`.
  3. Os detalhes do usuário são exibidos ou o usuário é redirecionado para a linha correspondente na tabela.
- Estados e erros:
  - Se email não encontrado, exibir mensagem de erro.
- Critérios de aceitação:
  - Busca retorna os dados completos do usuário (ID, Email, Nome, Ativo, CriadoEm, Perfis).

### UC24-FE: Listar Usuários por Nome

- Componente: `PesquisaBarUsuarios` na tela `Usuarios`.
- Pré-condições: Usuário logado com role `administrador`.
- Fluxo principal (UI):
  1. O usuário digita o nome no campo de busca.
  2. O frontend envia GET `/api/usuarios/nomes/{nome}` com paginação.
  3. A tabela é atualizada com os resultados filtrados.
- Estados e erros:
  - Se nenhum usuário encontrado, mostrar callout informativo.
- Critérios de aceitação:
  - Busca por nome é aplicada via botão "Buscar" (Enter também) — sem debounce; filtro e página ficam na URL.
  - Resultados são paginados corretamente.

### UC25-FE: Atualizar Perfis de um Usuário

- Modal: `ModalEditarPerfis` acionado pelo botão `Editar Perfis` na `TabelaUsuarios`.
- Pré-condições: Usuário logado com role `administrador`.
- Fluxo principal (UI):
  1. O usuário clica em `Editar Perfis` na linha do usuário desejado.
  2. Um `ModalEditarPerfis` é aberto com checkboxes para cada role: COLABORADOR, VALIDADOR, GESTOR_SISTEMA, ADMINISTRADOR.
  3. O ROLE_COLABORADOR é sempre marcado automaticamente (conforme backend).
  4. O usuário seleciona/desseleciona os perfis desejados e clica em `Salvar`.
  5. Uma requisição PATCH é enviada para `/api/usuarios/{id}/perfis` com o conjunto de perfis.
  6. Ao sucesso, a tabela é atualizada e um toast de sucesso é exibido.
- Estados e erros:
  - Durante submissão, mostrar loading e desabilitar botões.
  - Em erro de validação (ex: conjunto vazio), exibir toast com mensagem do backend.
  - Em erro 409 (último administrador), exibir mensagem específica.
- Critérios de aceitação:
  - Perfis atualizados corretamente no backend e refletidos na UI.
  - ROLE_COLABORADOR é sempre incluído automaticamente.
  - Não é possível remover o último administrador do sistema.

### UC26-FE: Desativar/Ativar Usuário

- Componente: Botões `Desativar`/`Ativar` individuais na `TabelaUsuarios`.
- Pré-condições: Usuário logado com role `administrador`.
- Fluxo principal (UI):
  1. O usuário clica em `Desativar` (para usuário ativo) ou `Ativar` (para usuário inativo) na linha do usuário desejado.
  2. Um `ModalConfirmacaoStatusUsuario` é aberto para confirmação.
  3. Ao confirmar:
     - Para desativar: requisição PATCH para `/api/usuarios/{id}/desativar`.
     - Para ativar: requisição PATCH para `/api/usuarios/{id}/ativar`.
  4. Ao sucesso, a coluna `Ativo` é atualizada na tabela e um toast de sucesso é exibido.
- Estados e erros:
  - Durante submissão, mostrar loading e desabilitar botões.
  - Em erro 409 (tentativa de desativar último administrador), exibir mensagem específica.
  - Em erro de usuário não encontrado, exibir mensagem de erro.
- Critérios de aceitação:
  - Status `ativo` atualizado corretamente no backend e refletido na UI.
  - A ação é individual (não em lote).
  - Não é possível desativar o último administrador do sistema.

---

## Ator: Qualquer Usuário (Autenticação - UI)

### UC28-FE: Login via Google OAuth2

- Tela: `PaginaLogin` (rota `/login`).
- Pré-condições: Usuário não autenticado.
- Fluxo principal (UI):
  1. O usuário acessa a rota `/login` (ou é redirecionado ao tentar acessar rota protegida sem autenticação).
  2. A página exibe: logo/descrição do sistema e botão `Entrar com Google`.
  3. O usuário clica no botão e é redirecionado para `/oauth2/authorization/google`.
  4. Após autenticação bem-sucedida no Google, o backend redireciona o usuário para a URL de sucesso do frontend com o access token na URL (fragmento `#token=...`).
  5. O frontend armazena o token e redireciona o usuário para a página inicial ou URL de origem.
- Estados e erros:
  - Se o usuário for inativo, exibir mensagem: "Usuário inativo. Contate o administrador."
  - Se o email não pertencer a domínio autorizado, exibir mensagem: "Acesso negado. Domínio não autorizado."
  - Em falha na autenticação, exibir mensagem genérica e botão `Tentar novamente`.
- Critérios de aceitação:
  - Fluxo de login funciona corretamente com Google OAuth2.
  - Token é armazenado e utilizado em requisições subsequentes.
  - Mensagens de erro são exibidas para fluxos alternativos (FA01-FA04 do backend).

### UC29-FE: Renovar Token de Acesso

- Componente: Hook/serviço de autenticação (não visual).
- Pré-condições: Usuário autenticado com refresh token válido em cookie.
- Fluxo principal (UI):
  1. O frontend monitora a expiração do access token (JWT).
  2. Antes da expiração, o frontend envia POST `/auth/refresh` automaticamente.
  3. O backend retorna novo access token (`LoginRes`: accessToken, tokenType, expiresIn).
  4. O frontend armazena o novo token e continua as requisições normalmente.
  5. O refresh token permanece o mesmo (não é renovado no refresh, apenas no login).
- Estados e erros:
  - Se o cookie `refreshToken` não estiver presente ou for inválido, redirecionar para `/login`.
  - Em erro de rede, tentar novamente ou redirecionar para `/login`.
- Critérios de aceitação:
  - Renovação ocorre automaticamente sem interrupção do usuário.
  - Novo access token é utilizado em requisições subsequentes.

### UC30-FE: Encerrar Sessão (Logout)

- Componente: Botão `Sair` no Header (quando usuário autenticado).
- Pré-condições: Usuário autenticado.
- Fluxo principal (UI):
  1. O usuário clica no botão `Sair` no Header.
  2. O frontend envia POST `/auth/logout`.
  3. O backend invalida o refresh token e limpa o cookie.
  4. O frontend limpa o access token local e redireciona para `/login`.
- Estados e erros:
  - Mesmo em caso de erro na requisição, o frontend deve limpar o estado local e redirecionar para `/login`.
- Critérios de aceitação:
  - Sessão encerrada corretamente no backend.
  - Tokens locais removidos.
  - Usuário redirecionado para `/login`.

---

## Considerações transversais

- Autenticação e autorização: rotas e botões devem respeitar roles. O frontend deve esconder/desabilitar ações não permitidas (ex: usuário público não vê botões de deletar/publicar). Autenticação via Google OAuth2 com JWT e refresh token em cookie.
- Gestão de tokens: implementar renovação automática do access token (UC29-FE) e limpeza adequada no logout (UC30-FE).
- Tratamento de erros: padrão único para erros HTTP, exibir mensagens amigáveis e mapear códigos de erro do backend para mensagens do usuário.
- Estado offline/timeout: exibir estado de offline e permitir re-tentar operações. Para operações críticas (deletar/publicar), usar confirmação adicional.
- Testes: cobrir principais fluxos (criar ambiente, editar geometrias, enviar para validação, publicar, deletar, login, logout, gestão de usuários).
- Performance: otimizar chamadas de API usando debounce, cache e queries paginadas.
- Acessibilidade: garantir contraste, foco visível, labels.

---

## Critérios de aceitação gerais

- Cada caso do backend tem uma tela ou ação correspondente no frontend.
- Todas as ações que mudam estado exibem confirmação e feedback (success/error).
- Formularios validam client-side as regras básicas (obrigatoriedade, números positivos, pelo menos uma entidade) e exibem erros do backend quando existirem.
- URLs refletem estado (filtros/paginação) para permitir deep-linking e compartilhamento.