# Contexto dos Ambientes Internos

## Casos de Uso

A seguir estão descritos os principais casos de uso relacionados ao gerenciamento e consulta dos ambientes internos das edificações do IFCE.

### Ator: Validador

#### **UC01: Listar Ambientes Aguardando Validação**

* **Descrição:** Permite ao Validador visualizar a lista de todos os ambientes que aguardam validação, filtrando por Nome ou Localização.
* **Ator Primário:** Validador.
* **Pré-condições:** O Validador está autenticado e possui permissão para visualizar ambientes aguardando validação. O ambiente a ser consultado existe e tem atributo `status = AGUARDANDO_VALIDACAO`.
* **Fluxo Principal:**
    * **FP1 - Todos os Ambientes Aguardando Validação:**
        1. O Validador realiza uma requisição `GET` ao endpoint `/api/ambientes/validacao`.
        2. O Sistema recupera a lista de ambientes do banco de dados.
        3. O Sistema retorna a lista ao Validador de forma paginada, limitada a 100 registros por página.
        4. A lista retornada conterá os seguintes dados: ID, Nome, Localização, Tipo, Capacidade e Área.
    * **FP2 - Filtrar por Nome:**
        1. O Validador realiza uma requisição `GET` ao endpoint `/api/ambientes/validacao?nome={nome}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao nome fornecido.
        3. Repete os passos 3 e 4 do FP1.
    * **FP3 - Filtrar por Localização:**
        1. O Validador realiza uma requisição `GET` ao endpoint `/api/ambientes/validacao?localizacao={localizacao}`.
        2. O Sistema recupera a lista de ambientes que correspondem à localização fornecida.
        3. Repete os passos 3 e 4 do FP1.
    * **FP4 - Filtrar por Tipo:**
        1. O Validador realiza uma requisição `GET` ao endpoint `/api/ambientes/validacao?tipo={tipo}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao tipo fornecido.
        3. Repete os passos 3 e 4 do FP1.
* **Fluxos Alternativos:**
    * **FA01 - Nenhum ambiente encontrado:** Se não houver ambientes aguardando validação, o Sistema exibe uma mensagem indicando que a lista está vazia.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

#### **UC02: Obter Detalhes de um Ambiente Aguardando Validação**

* **Descrição:** Permite ao Validador visualizar todas as informações detalhadas de um ambiente específico que aguarda validação, buscando por ID.
* **Ator Primário:** Validador.
* **Pré-condições:** O Validador está autenticado e possui permissão para visualizar ambientes aguardando validação. O ambiente a ser consultado existe e tem atributo `status = AGUARDANDO_VALIDACAO`.
* **Fluxo Principal:**
    * **FP1 - Por ID:**
        1. O Validador realiza uma requisição `GET` ao endpoint `/api/ambientes/validacao/{id}`.
        2. O Sistema recupera e exibe os detalhes do ambiente solicitado.
        3. Os detalhes incluem as seguintes informações do ambiente:
            * ID;
            * Nome;
            * Localização;
            * Tipo;
            * Capacidade;
            * Geometrias do ambiente e suas áreas;
            * Área total do ambiente;
            * Pés-direitos;
            * Lista de Portas por tipo e suas áreas;
            * Área total das Portas;
            * Lista de Janelas por tipo e suas áreas;
            * Área total das Janelas.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente não encontrado:** Se o ID não corresponder a nenhum ambiente aguardando validação, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

#### **UC03: Gerenciar Publicação de Ambientes**

* **Descrição:** O Validador aprova ou reprova a publicação de ambientes submetidos a publicação e gerencia o status dos ambientes no geral.
* **Ator Primário:** Validador.
* **Pré-condições:** O Validador está autenticado e possui as permissões necessárias.
* **Fluxo Principal:**
    * **FP1 - Publicar:**
        1.  Obtém o ambiente não publicado (UC06) ou aguardando validação (UC02) que deseja tornar público.
        2.  O Validador realiza uma requisição `PATCH` ao endpoint `/api/ambientes/validacao/{id}/publicar`.
        3.  O Sistema atualiza o status do ambiente selecionado para `status = PUBLICADO`.
        4.  O Sistema exibe uma mensagem de sucesso e os detalhes do ambiente publicado.
    * **FP2 - Tornar Não Público:**
        1.  Obtém o ambiente publicado (UC20) ou aguardando validação (UC02) que deseja tornar não público.
        2.  O Validador realiza uma requisição `PATCH` ao endpoint `/api/ambientes/validacao/{id}/remover-publicacao`.
        3.  O Sistema atualiza o status do ambiente selecionado para `status = NAO_PUBLICADO`.
        4.  O Sistema exibe uma mensagem de sucesso e os detalhes do ambiente não publicado.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente não encontrado ou status incorreto:** Se o ambiente não existir ou não estiver no estado esperado (ex: tentar publicar um ambiente já publicado), o Sistema exibe erro.
    * **FA02 - Erro ao publicar ambiente:** Se ocorrer um erro durante o processo de publicação, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** O status do ambiente é alterado no banco de dados para `status = PUBLICADO` ou `status = NAO_PUBLICADO`.

#### **UC04: Deletar Ambientes Aguardando Validação ou Publicados**

* **Descrição:** Permite ao Validador remover permanentemente um ou mais ambientes que estão aguardando validação ou publicados.
* **Ator Primário:** Validador.
* **Pré-condições:** O Validador está autenticado e possui permissão para deletar ambientes aguardando validação ou publicados. Os ambientes a serem deletados existem e têm `status = AGUARDANDO_VALIDACAO` ou `status = PUBLICADO`.
* **Fluxo Principal:**
    * **FP1 - Ambientes Aguardando Validação:**
        1.  O Validador obtém o(s) ambiente(s) aguardando validação que deseja remover (UC01 ou UC02).
        2.  O Validador preenche o corpo da requisição com a lista de IDs dos ambientes a serem removidos.
        3.  O Validador realiza uma requisição `DELETE` ao endpoint `/api/ambientes/validacao`.
        4.  O Sistema verifica se cada ambiente existe e está com `status = AGUARDANDO_VALIDACAO`.
        5.  Se todos os ambientes forem encontrados e estiverem aguardando validação, o Sistema os remove do banco de dados.
        6.  O Sistema exibe uma mensagem de sucesso.
    * **FP2 - Ambientes Publicados:**
        1.  O Validador obtém o(s) ambiente(s) publicado(s) que deseja remover (UC02 ou UC20).
        2.  O Validador preenche o corpo da requisição com a lista de IDs dos ambientes a serem removidos.
        3.  O Validador realiza uma requisição `DELETE` ao endpoint `/api/ambientes/publicados`.
        4.  O Sistema verifica se cada ambiente existe e está com `status = PUBLICADO`.
        5.  Se todos os ambientes forem encontrados e estiverem publicados, o Sistema os remove do banco de dados.
        6.  O Sistema exibe uma mensagem de sucesso.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente aguardando validação ou publicado, o Sistema exibe uma mensagem de erro.
    * **FA02 - Erro de Exclusão:** Se ocorrer um erro ao remover o ambiente do banco de dados, o Sistema informa o Validador sobre a falha.
* **Pós-condições:** Os ambientes aguardando validação ou publicados especificados são removidos permanentemente do sistema.

---

### Ator: Gestor do Sistema

#### **UC05: Listar Ambientes Não Publicados**

* **Descrição:** Permite ao Gestor visualizar a lista de todos os ambientes que ainda não foram publicados, filtrando por Nome ou Localização.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para visualizar ambientes não publicados. O ambiente a ser consultado existe e tem atributo `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    * **FP1 - Todos os Ambientes Não Publicados:**
        1. O Gestor realiza uma requisição `GET` ao endpoint `/api/ambientes/nao-publicados`.
        2. O Sistema recupera a lista de ambientes do banco de dados.
        3. O Sistema retorna a lista ao Gestor de forma paginada, limitada a 100 registros por página.
        4. A lista retornada conterá os seguintes dados: ID, Nome, Localização, Tipo, Capacidade e Área.
    * **FP2 - Filtrar por Nome:**
        1. O Gestor realiza uma requisição `GET` ao endpoint `/api/ambientes/nao-publicados?nome={nome}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao nome fornecido.
        3. Repete os passos 3 e 4 do FP1.
    * **FP3 - Filtrar por Localização:**
        1. O Gestor realiza uma requisição `GET` ao endpoint `/api/ambientes/nao-publicados?localizacao={localizacao}`.
        2. O Sistema recupera a lista de ambientes que correspondem à localização fornecida.
        3. Repete os passos 3 e 4 do FP1.
    * **FP4 - Filtrar por Tipo:**
        1. O Gestor realiza uma requisição `GET` ao endpoint `/api/ambientes/nao-publicados?tipo={tipo}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao tipo fornecido.
        3. Repete os passos 3 e 4 do FP1.
* **Fluxos Alternativos:**
    * **FA01 - Nenhum ambiente encontrado:** Se não houver ambientes não publicados, o Sistema exibe uma mensagem indicando que a lista está vazia.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

#### **UC06: Obter Detalhes de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor visualizar todas as informações detalhadas de um ambiente específico que ainda não foi publicado, buscando por ID.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para visualizar ambientes não publicados. O ambiente a ser consultado existe e tem atributo `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    * **FP1 - Por ID:**
        1. O Gestor realiza uma requisição `GET` ao endpoint `/api/ambientes/nao-publicados/{id}`.
        2. O Sistema recupera e exibe os detalhes do ambiente solicitado.
        3. Os detalhes incluem as seguintes informações do ambiente:
            * ID;
            * Nome;
            * Localização;
            * Tipo;
            * Capacidade;
            * Geometrias do ambiente e suas áreas;
            * Área total do ambiente;
            * Pés-direitos;
            * Lista de Portas por tipo e suas áreas;
            * Área total das Portas;
            * Lista de Janelas por tipo e suas áreas;
            * Área total das Janelas.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente não encontrado:** Se o ID não corresponder a nenhum ambiente não publicado, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

#### **UC07: Cadastrar um Novo Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor criar um novo registro de ambiente, que iniciará com o status não publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para cadastrar ambientes.
* **Fluxo Principal:**
    1.  O Gestor preenche o corpo da requisição com os seguintes dados: nome, localização, tipo, capacidade, geometrias do ambiente, pés-direitos e pelo menos uma porta.
    2.  O Gestor realiza uma requisição `POST` ao endpoint `/api/ambientes/nao-publicados`.
    3.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6 e RN-1.7).
    4.  Se a validação for bem-sucedida, o Sistema persiste o novo ambiente com o atributo `status = NAO_PUBLICADO`, gera um ID e retorna os dados do ambiente criado.
    5.  O Sistema exibe uma mensagem de sucesso e os detalhes do ambiente recém-criado.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados fornecidos pelo Gestor não passarem na validação, o Sistema exibe mensagens de erro indicando os problemas e não prossegue com o cadastro.
    * **FA02 - Erro de Persistência:** Se ocorrer um erro ao salvar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Um novo ambiente é criado no sistema com o atributo `status = NAO_PUBLICADO` e possui um ID único.

#### **UC08: Atualizar dados básicos de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor modificar os dados básicos de um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém os dados atuais do ambiente (UC06).
    2.  O Gestor preenche o corpo da requisição com os seguintes dados: ID, nome, localização, capacidade.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/dados-basicos`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6 e RN-1.7).
    5.  Se a validação for bem-sucedida, o Sistema atualiza os dados do ambiente no banco de dados e retorna os dados atualizados.
    6.  O Sistema exibe uma mensagem de sucesso e os detalhes atualizados do ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

#### **UC09: Incluir geometrias em um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor incluir uma ou mais geometrias em um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o ID do ambiente (UC05 ou UC06).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e uma lista contendo os dados de cada geometria: tipo de geometria, o comprimento e a largura (ou base e altura) da mesma. A requisição deve ter pelo menos uma geometria.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/geometrias/incluir`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema insere as novas geometrias no ambiente, atualiza-o no banco de dados e retorna os dados atualizados.
    6.  O Sistema exibe uma mensagem de sucesso e os detalhes atualizados do ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados inseridos não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

#### **UC10: Atualizar/ remover as geometrias de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor modificar ou remover as geometrias de um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém os dados atuais do ambiente (UC06).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e altera a lista de geometrias obtidas modificando quaisquer um dos seguintes atributos: tipo de geometria, o comprimento e a largura (ou base e altura) da mesma, ou remove geometrias da lista. A requisição deve ter pelo menos uma geometria.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/geometrias/atualizar`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema atualiza as geometrias do ambiente no banco de dados e retorna os dados atualizados.
    6.  O Sistema exibe uma mensagem de sucesso e os detalhes atualizados do ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

#### **UC11: Incluir pés direitos em um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor incluir um ou mais pés-direitos em um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o ID do ambiente (UC05 ou UC06).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e uma lista contendo os pés-direitos (altura entre o piso e o teto). A requisição deve ter pelo menos um pé-direito.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/pes-direitos/incluir`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema insere os novos pés-direitos no ambiente, atualiza-o no banco de dados e retorna os dados atualizados.
    6.  O Sistema exibe uma mensagem de sucesso e os detalhes atualizados do ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados inseridos não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

#### **UC12: Atualizar/ remover os pés direitos de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor modificar ou remover os pés-direitos de um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém os dados atuais do ambiente (UC06).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e altera ou remove um ou mais pés-direitos obtidos. A requisição deve ter pelo menos um pé-direito.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/pes-direitos/atualizar`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema atualiza os pés-direitos do ambiente no banco de dados e retorna os dados atualizados.
    6.  O Sistema exibe uma mensagem de sucesso e os detalhes atualizados do ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

#### **UC13: Incluir esquadrias em um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor incluir uma ou mais esquadrias em um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o ID do ambiente (UC05 ou UC06).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e uma lista contendo os dados de cada esquadria: o tipo da esquadria (porta, janela, etc.), a largura (base), a altura, o material, a altura do peitoril, se houver, e alguma informação adicional, se houver. A requisição deve ter pelo menos uma esquadria.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/esquadrias/incluir`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema insere as novas esquadrias no ambiente, atualiza-o no banco de dados e retorna os dados atualizados.
    6.  O Sistema exibe uma mensagem de sucesso e os detalhes atualizados do ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados inseridos não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

#### **UC14: Atualizar/ remover as esquadrias de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor modificar ou remover as esquadrias de um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém os dados atuais do ambiente (UC06).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e altera a lista de esquadrias obtidas modificando quaisquer um dos seguintes atributos: o tipo da esquadria (porta, janela, etc.), a largura (base), a altura, o material, a altura do peitoril, se houver, e alguma informação adicional, se houver, ou remove esquadrias da lista. A requisição deve ter pelo menos uma esquadria.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/esquadrias/atualizar`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema atualiza as esquadrias do ambiente no banco de dados e retorna os dados atualizados.
    6.  O Sistema exibe uma mensagem de sucesso e os detalhes atualizados do ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

#### **UC15: Atualizar a informação adicional de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor modificar a informação adicional de um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém os dados atuais do ambiente (UC06).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e altera a informação adicional obtida.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/informacao-adicional`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema atualiza a informação adicional do ambiente no banco de dados e retorna os dados atualizados.
    6.  O Sistema exibe uma mensagem de sucesso e os detalhes atualizados do ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

#### **UC16: Deletar Ambientes Não Publicados**

* **Descrição:** Permite ao Gestor remover permanentemente um ou mais ambientes que ainda não foram publicados.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para deletar ambientes não publicados. Os ambientes a serem deletados existem e têm `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o(s) ambiente(s) não publicado(s) que deseja remover (UC05 ou UC06).
    2.  O Gestor preenche o corpo da requisição com a lista de IDs dos ambientes a serem removidos.
    3.  O Gestor realiza uma requisição `DELETE` ao endpoint `/api/ambientes/nao-publicados`.
    4.  O Sistema verifica se cada ambiente existe e está com `status = NAO_PUBLICADO`.
    5.  Se todos os ambientes forem encontrados e não estiverem publicados, o Sistema os remove do banco de dados.
    6.  O Sistema exibe uma mensagem de sucesso.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA02 - Erro de Exclusão:** Se ocorrer um erro ao remover o ambiente do banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os ambientes não publicados especificados são removidos permanentemente do sistema.

#### **UC17: Alterar tipo e dados de Ambientes Não Publicados**

* **Descrição:** Permite ao Gestor alterar o tipo e os dados de um ambiente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para alterar ambientes não publicados. Os ambientes a serem alterados existem e têm `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o ambiente não publicado que deseja alterar o tipo e os dados (UC06).
    2.  O Gestor preenche o corpo da requisição com o novo tipo e os novos dados do ambiente.
    3.  O Gestor realiza uma requisição `POST` ao endpoint `/api/ambientes/nao-publicados/{id}`.
    4.  O Sistema verifica se cada ambiente existe e está com `status = NAO_PUBLICADO` e se os novos dados estiverem de acordo com as regras de negócio (RN-1.6).
    5.  Se as verificações forem bem-sucedidas, o Sistema cria um novo ambiente do tipo especificado no banco de dados, remove o ambiente antigo e retorna os dados do novo ambiente.
    6.  O Sistema exibe uma mensagem de sucesso e os dados do novo ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** O ambiente especificado é removido do sistema e um novo ambiente é criado com tipo diferente e os novos dados.

#### **UC18: Duplicar Ambiente Não Publicados**

* **Descrição:** Permite ao Gestor duplicar um ambiente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para duplicar ambientes não publicados. Os ambientes a serem duplicados existem e têm `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o ambiente não publicado que deseja duplicar (UC06).
    2.  O Gestor preenche o corpo da requisição com o novo nome e/ou localização do ambiente.
    3.  O Gestor realiza uma requisição `POST` ao endpoint `/api/ambientes/nao-publicados/{id}/duplicar`.
    4.  O Sistema verifica se o ambiente existe e está com `status = NAO_PUBLICADO` e está de acordo com as regras de negócio (RN-1.6 e RN-1.7).
    5.  Se as verificações forem bem-sucedidas, o Sistema cria um novo ambiente no banco de dados com base nos dados do ambiente especificado.
    6.  O Sistema exibe uma mensagem de sucesso e os dados do novo ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe uma mensagem de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Persistência:** Se ocorrer um erro ao salvar o ambiente no banco de dados, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** Um novo ambiente é criado no sistema com o atributo `status = NAO_PUBLICADO` e possui um ID único.

#### **UC19: Enviar Ambientes para Publicação**

* **Descrição:** O Gestor seleciona um ou mais ambientes não publicados e os submete ao processo de validação para torná-los públicos.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para enviar ambientes para publicação. Existem ambientes com `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o(s) ambiente(s) não publicado(s) que deseja enviar para publicação (UC05 ou UC06).
    2.  O Gestor preenche o corpo da requisição com a lista de IDs dos ambientes obtidos.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/validar`.
    4.  O Sistema verifica se cada ambiente existe e está com `status = NAO_PUBLICADO`.
    5.  Se todos os ambientes forem válidos, o Sistema atualiza o status dos ambientes selecionados para `status = AGUARDANDO_VALIDACAO`.
    6.  O Sistema exibe uma mensagem de sucesso.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente Não Encontrado:** Se os IDs fornecidos não corresponderem a ambientes não publicados, o Sistema exibe uma mensagem de erro.
    * **FA02 - Erro no Envio:** Se ocorrer um erro ao atualizar o status dos ambientes, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os ambientes selecionados têm seu status alterado para indicar que estão pendentes de validação.

---

### Ator: Servidor

#### **UC20: Obter Detalhes de um Ambiente Publicado**

* **Descrição:** Permite ao Servidor visualizar todas as informações detalhadas de um ambiente específico publicado, buscando por ID.
* **Ator Primário:** Servidor.
* **Pré-condições:** O Servidor está autenticado e possui permissão para visualizar ambientes publicados. O ambiente a ser consultado existe e tem atributo `status = PUBLICADO`.
* **Fluxo Principal:**
    * **FP1 - Por ID:**
        1. O Servidor realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados/{id}`.
        2. O Sistema recupera e exibe os detalhes do ambiente solicitado.
        3. Os detalhes incluem as seguintes informações do ambiente:
            * ID;
            * Nome;
            * Localização;
            * Tipo;
            * Capacidade;
            * Geometrias do ambiente e suas áreas;
            * Área total do ambiente;
            * Pés-direitos;
            * Lista de Portas por tipo e suas áreas;
            * Área total das Portas;
            * Lista de Janelas por tipo e suas áreas;
            * Área total das Janelas.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente não encontrado:** Se o ID não corresponder a nenhum ambiente publicado, o Sistema exibe uma mensagem de erro.
    * **FA02 - Erro ao obter detalhes:** Se ocorrer um erro ao buscar os detalhes do ambiente, o Sistema informa o Servidor sobre a falha.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

#### **UC21: Obter Detalhes de Esquadrias de uma lista de Ambientes Publicados**

* **Descrição:** Permite ao Servidor visualizar todas as informações detalhadas de esquadrias de um conjunto de ambientes publicados.
* **Ator Primário:** Servidor.
* **Pré-condições:** O Servidor está autenticado e possui permissão para visualizar ambientes publicados. Os ambientes a serem consultados existem e têm atributo `status = PUBLICADO`.
* **Fluxo Principal:**
    * **FP1 - Por ID:**
        1. O servidor preenche o corpo da requisição com a lista de IDs dos ambientes publicados a serem consultados.
        2. O Servidor realiza uma requisição `POST` ao endpoint `/api/ambientes/publicados/esquadrias`.
        3. O Sistema recupera e exibe os detalhes das esquadrias dos ambientes solicitados.
        4. Os detalhes incluem as seguintes informações das esquadrias de cada ambiente:
            * Nomes e Localizações dos ambientes consultados;
            * Lista de Esquadrias com os seguintes dados:
                * Tipo (Porta, Janela, etc.);
                * Largura (base);
                * Altura;
                * Material da esquadria;
                * Altura do peitoril (se houver);
                * Informação adicional (se houver);
                * Área da esquadria;
                * Área total por tipo e material.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente não encontrado:** Se algum dos IDs não corresponderem a um ambiente publicado, o Sistema exibe uma mensagem de erro.
    * **FA02 - Erro ao obter detalhes:** Se ocorrer um erro ao buscar os detalhes das esquadrias, o Sistema informa o Servidor sobre a falha.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

---

### Ator: Público Externo

#### **UC22: Listar Ambientes Publicados**

* **Descrição:** Permite ao Público Externo visualizar a lista de todos os ambientes publicados, filtrando por Nome ou Localização.
* **Ator Primário:** Público Externo.
* **Pré-condições:** O Público Externo está autenticado e possui permissão para visualizar ambientes publicados. O ambiente a ser consultado existe e tem atributo `status = PUBLICADO`.
* **Fluxo Principal:**
    * **FP1 - Todos os Ambientes Publicados:**
        1. O Público Externo realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados`.
        2. O Sistema recupera a lista de ambientes do banco de dados.
        3. O Sistema retorna a lista ao Público Externo de forma paginada, limitada a 100 registros por página.
        4. A lista retornada conterá os seguintes dados: ID, Nome, Localização, Tipo, Capacidade e Área.
    * **FP2 - Filtrar por Nome:**
        1. O Público Externo realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados?nome={nome}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao nome fornecido.
        3. Repete os passos 3 e 4 do FP1.
    * **FP3 - Filtrar por Localização:**
        1. O Público Externo realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados?localizacao={localizacao}`.
        2. O Sistema recupera a lista de ambientes que correspondem à localização fornecida.
        3. Repete os passos 3 e 4 do FP1.
    * **FP4 - Filtrar por Tipo:**
        1. O Público Externo realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados?tipo={tipo}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao tipo fornecido.
        3. Repete os passos 3 e 4 do FP1.
* **Fluxos Alternativos:**
    * **FA01 - Nenhum ambiente encontrado:** Se não houver ambientes publicados, o Sistema exibe uma mensagem indicando que a lista está vazia.
* **Pós-condições:** Nenhuma alteração no estado do sistema.