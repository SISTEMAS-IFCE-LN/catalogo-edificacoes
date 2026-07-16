## Casos de Uso — Backend (Ambientes Internos)

A seguir estão descritos os principais casos de uso relacionados ao gerenciamento e consulta dos ambientes internos das edificações do IFCE.

## Ator: Validador

### **UC01: Listar Ambientes Aguardando Validação**

* **Descrição:** Permite ao Validador visualizar a lista de todos os ambientes que aguardam validação, filtrando por Nome ou Localização.
* **Ator Primário:** Validador.
* **Pré-condições:** O Validador está autenticado e possui permissão para visualizar ambientes aguardando validação. O ambiente a ser consultado existe e tem atributo `status = AGUARDANDO_VALIDACAO`.
* **Fluxo Principal:**
    * **FP1 - Todos os Ambientes Aguardando Validação:**
        1. O Validador realiza uma requisição `GET` ao endpoint `/api/ambientes/validacao`.
        2. O Sistema recupera a lista de ambientes do banco de dados.
        3. O Sistema retorna a lista ao Validador de forma paginada, limitada a 100 registros por página.
        4. A lista retornada conterá os seguintes dados: ID, Nome, Localização (Bloco, Unidade e Andar), Tipo, Capacidade e Área. A resposta também incluirá `areaTotal` (soma das áreas dos ambientes da página) e `dadosPaginacao`.
    * **FP2 - Filtrar por Nome:**
        1. O Validador realiza uma requisição `GET` ao endpoint `/api/ambientes/validacao/nome?nome={nome}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao nome fornecido.
        3. Repete os passos 3 e 4 do FP1.
    * **FP3 - Filtrar por Localização:**
        1. O Validador realiza uma requisição `GET` ao endpoint `/api/ambientes/validacao/localizacao?bloco={bloco}&unidade={unidade}&andar={andar}`.
        2. O Sistema recupera a lista de ambientes que correspondem aos parâmetros de localização fornecidos.
        3. Repete os passos 3 e 4 do FP1.
    * **FP4 - Filtrar por Tipo:**
        1. O Validador realiza uma requisição `GET` ao endpoint `/api/ambientes/validacao/tipo?tipo={tipo}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao tipo fornecido.
        3. Repete os passos 3 e 4 do FP1.
* **Fluxos Alternativos:**
    * **FA01 - Nenhum ambiente encontrado:** Se não houver ambientes aguardando validação, o Sistema exibe uma mensagem indicando que a lista está vazia.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

### **UC02: Obter Detalhes de um Ambiente Aguardando Validação**

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
            * Localização (Bloco, Unidade e Andar);
            * Tipo;
            * Capacidade;
            * Geometrias do ambiente e suas áreas;
            * Área total do ambiente;
            * Pés-direitos;
            * Lista de Esquadrias com os seguintes dados:
                * Tipo (Porta, Janela, etc.);
                * Geometria (base, altura, repetição, área);
                * Material;
                * Altura do peitoril (se houver);
                * Informação adicional (se houver);
            * Resumo de Esquadrias por Tipo e Material (tipo, material, área total);
            * Informação Adicional;
            * Status.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente não encontrado:** Se o ID não corresponder a nenhum ambiente aguardando validação, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

### **UC03: Gerenciar Publicação de Ambientes**

* **Descrição:** O Validador aprova ou reprova a publicação de ambientes submetidos a publicação e gerencia o status dos ambientes no geral.
* **Ator Primário:** Validador.
* **Pré-condições:** O Validador está autenticado e possui as permissões necessárias.
* **Fluxo Principal:**
    * **FP1 - Publicar:**
        1.  Obtém o ambiente aguardando validação (UC02) que deseja tornar público.
        2.  O Validador realiza uma requisição `PATCH` ao endpoint `/api/ambientes/validacao/{id}/publicar`.
        3.  O Sistema atualiza o status do ambiente selecionado para `status = PUBLICADO`.
        4.  O Sistema retorna `204 No Content`.
    * **FP2 - Privar:**
        1.  Obtém o ambiente publicado (UC19) ou aguardando validação (UC02) que deseja privar.
        2.  O Validador realiza uma requisição `PATCH` ao endpoint `/api/ambientes/validacao/{id}/privar`.
        3.  O Sistema atualiza o status do ambiente selecionado para `status = NAO_PUBLICADO`.
        4.  O Sistema retorna `204 No Content`.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente não encontrado ou status incorreto:** Se o ambiente não existir ou não estiver no estado esperado (ex: tentar publicar um ambiente já publicado), o Sistema exibe erro.
    * **FA02 - Erro ao publicar ambiente:** Se ocorrer um erro durante o processo de publicação, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** O status do ambiente é alterado no banco de dados para `status = PUBLICADO` ou `status = NAO_PUBLICADO`.

---

## Ator: Gestor do Sistema

### **UC04: Listar Ambientes Não Publicados**

* **Descrição:** Permite ao Gestor visualizar a lista de todos os ambientes que ainda não foram publicados, filtrando por Nome ou Localização.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para visualizar ambientes não publicados. O ambiente a ser consultado existe e tem atributo `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    * **FP1 - Todos os Ambientes Não Publicados:**
        1. O Gestor realiza uma requisição `GET` ao endpoint `/api/ambientes/nao-publicados`.
        2. O Sistema recupera a lista de ambientes do banco de dados.
        3. O Sistema retorna a lista ao Gestor de forma paginada, limitada a 100 registros por página.
        4. A lista retornada conterá os seguintes dados: ID, Nome, Localização (Bloco, Unidade e Andar), Tipo, Capacidade e Área. A resposta também incluirá `areaTotal` (soma das áreas dos ambientes da página) e `dadosPaginacao`.
    * **FP2 - Filtrar por Nome:**
        1. O Gestor realiza uma requisição `GET` ao endpoint `/api/ambientes/nao-publicados/nome?nome={nome}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao nome fornecido.
        3. Repete os passos 3 e 4 do FP1.
    * **FP3 - Filtrar por Localização:**
        1. O Gestor realiza uma requisição `GET` ao endpoint `/api/ambientes/nao-publicados/localizacao?bloco={bloco}&unidade={unidade}&andar={andar}`.
        2. O Sistema recupera a lista de ambientes que correspondem aos parâmetros de localização fornecidos.
        3. Repete os passos 3 e 4 do FP1.
    * **FP4 - Filtrar por Tipo:**
        1. O Gestor realiza uma requisição `GET` ao endpoint `/api/ambientes/nao-publicados/tipo?tipo={tipo}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao tipo fornecido.
        3. Repete os passos 3 e 4 do FP1.
* **Fluxos Alternativos:**
    * **FA01 - Nenhum ambiente encontrado:** Se não houver ambientes não publicados, o Sistema exibe uma mensagem indicando que a lista está vazia.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

### **UC05: Obter Detalhes de um Ambiente Não Publicado**

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
            * Localização (Bloco, Unidade e Andar);
            * Tipo;
            * Capacidade;
            * Geometrias do ambiente e suas áreas;
            * Área total do ambiente;
            * Pés-direitos;
            * Lista de Esquadrias com os seguintes dados:
                * Tipo (Porta, Janela, etc.);
                * Geometria (base, altura, repetição, área);
                * Material;
                * Altura do peitoril (se houver);
                * Informação adicional (se houver);
            * Resumo de Esquadrias por Tipo e Material (tipo, material, área total);
            * Informação Adicional;
            * Status.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente não encontrado:** Se o ID não corresponder a nenhum ambiente não publicado, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

### **UC06: Cadastrar um Novo Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor criar um novo registro de ambiente, que iniciará com o status não publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para cadastrar ambientes.
* **Fluxo Principal:**
    1.  O Gestor preenche o corpo da requisição com os seguintes dados: nome, localização (Bloco, Unidade e Andar), tipo, capacidade, geometrias do ambiente (tipo de geometria, base, altura e repetição), pés-direitos, esquadrias (tipo, geometria com base/altura/repetição, material, altura do peitoril e informação adicional) e informação adicional.
    2.  O Gestor realiza uma requisição `POST` ao endpoint `/api/ambientes/nao-publicados`.
    3.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6 e RN-1.7).
    4.  Se a validação for bem-sucedida, o Sistema persiste o novo ambiente com o atributo `status = NAO_PUBLICADO`, gera um ID e retorna os dados do ambiente criado.
    5.  O Sistema retorna `201 Created` com os detalhes do ambiente recém-criado (`AmbienteRes`) e o header `Location` apontando para `/api/ambientes/nao-publicados/{id}`.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados fornecidos pelo Gestor não passarem na validação, o Sistema exibe mensagens de erro indicando os problemas e não prossegue com o cadastro.
    * **FA02 - Erro de Persistência:** Se ocorrer um erro ao salvar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Um novo ambiente é criado no sistema com o atributo `status = NAO_PUBLICADO` e possui um ID único.

### **UC07: Atualizar dados básicos de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor modificar os dados básicos de um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém os dados atuais do ambiente (UC05).
    2.  O Gestor preenche o corpo da requisição com os seguintes dados: nome, localização (Bloco, Unidade e Andar) e capacidade.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/dados-basicos`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6 e RN-1.7).
    5.  Se a validação for bem-sucedida, o Sistema atualiza os dados do ambiente no banco de dados e retorna os dados básicos atualizados.
    6.  O Sistema retorna `200 OK` com os dados básicos do ambiente (`AmbienteBasicoRes`): ID, Nome, Tipo, Localização (Bloco, Unidade e Andar), Capacidade e Área.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

### **UC08: Incluir geometrias em um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor incluir uma ou mais geometrias em um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o ID do ambiente (UC04 ou UC05).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e uma lista contendo os dados de cada geometria: tipo de geometria, a base, a altura e a repetição (padrão 1) da mesma. A requisição deve ter pelo menos uma geometria.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/geometrias/incluir`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema insere as novas geometrias no ambiente, atualiza-o no banco de dados e retorna os dados atualizados.
    6.  O Sistema retorna `200 OK` com a lista de geometrias e a área total (`ListaGeometriasAmbienteRes`): lista de geometrias (ID, tipo, base, altura, repetição, área) e área total.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados inseridos não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

### **UC09: Atualizar/ remover as geometrias de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor modificar ou remover as geometrias de um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém os dados atuais do ambiente (UC05).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e altera a lista de geometrias obtidas modificando quaisquer um dos seguintes atributos: tipo de geometria, a base, a altura e a repetição da mesma, ou remove geometrias da lista. A requisição deve ter pelo menos uma geometria.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/geometrias/atualizar`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema atualiza as geometrias do ambiente no banco de dados e retorna os dados atualizados.
    6.  O Sistema retorna `200 OK` com a lista de geometrias e a área total (`ListaGeometriasAmbienteRes`): lista de geometrias (ID, tipo, base, altura, repetição, área) e área total.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

### **UC10: Incluir pés direitos em um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor incluir um ou mais pés-direitos em um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o ID do ambiente (UC04 ou UC05).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e uma lista contendo os pés-direitos (altura entre o piso e o teto). A requisição deve ter pelo menos um pé-direito.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/pes-direitos/incluir`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema insere os novos pés-direitos no ambiente, atualiza-o no banco de dados e retorna os dados atualizados.
    6.  O Sistema retorna `200 OK` com o conjunto de pés-direitos (`Set<BigDecimal>`).
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados inseridos não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

### **UC11: Atualizar/ remover os pés direitos de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor modificar ou remover os pés-direitos de um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém os dados atuais do ambiente (UC05).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e altera ou remove um ou mais pés-direitos obtidos. A requisição deve ter pelo menos um pé-direito.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/pes-direitos/atualizar`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema atualiza os pés-direitos do ambiente no banco de dados e retorna os dados atualizados.
    6.  O Sistema retorna `200 OK` com o conjunto de pés-direitos (`Set<BigDecimal>`).
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

### **UC12: Incluir esquadrias em um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor incluir uma ou mais esquadrias em um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o ID do ambiente (UC04 ou UC05).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e uma lista contendo os dados de cada esquadria: o tipo da esquadria (porta, janela, etc.), a geometria (base, altura e repetição), o material, a altura do peitoril (se houver, padrão 0) e alguma informação adicional (se houver, padrão ""). A requisição deve ter pelo menos uma esquadria.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/esquadrias/incluir`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema insere as novas esquadrias no ambiente, atualiza-o no banco de dados e retorna os dados atualizados.
    6.  O Sistema retorna `200 OK` com os detalhes das esquadrias (`EsquadriasDetalhesRes`): lista de esquadrias (ID, tipo, geometria com base/altura/repetição/área, material, altura do peitoril, informação adicional, área) e resumo por tipo e material (tipo, material, área total).
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados inseridos não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

### **UC13: Atualizar/ remover as esquadrias de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor modificar ou remover as esquadrias de um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém os dados atuais do ambiente (UC05).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e altera a lista de esquadrias obtidas modificando quaisquer um dos seguintes atributos: o tipo da esquadria (porta, janela, etc.), a geometria (base, altura e repetição), o material, a altura do peitoril, se houver, e alguma informação adicional, se houver, ou remove esquadrias da lista. A requisição deve ter pelo menos uma esquadria.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/esquadrias/atualizar`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema atualiza as esquadrias do ambiente no banco de dados e retorna os dados atualizados.
    6.  O Sistema retorna `200 OK` com os detalhes das esquadrias (`EsquadriasDetalhesRes`): lista de esquadrias (ID, tipo, geometria com base/altura/repetição/área, material, altura do peitoril, informação adicional, área) e resumo por tipo e material (tipo, material, área total).
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

### **UC14: Atualizar a informação adicional de um Ambiente Não Publicado**

* **Descrição:** Permite ao Gestor modificar a informação adicional de um ambiente existente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para atualizar ambientes não publicados. O ambiente a ser atualizado existe e tem `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém os dados atuais do ambiente (UC05).
    2.  O Gestor preenche o corpo da requisição com o ID do ambiente e altera a informação adicional obtida.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/{id}/informacao-adicional`.
    4.  O Sistema valida os dados da requisição conforme as regras de negócio (RN-1.6).
    5.  Se a validação for bem-sucedida, o Sistema atualiza a informação adicional do ambiente no banco de dados e retorna os dados atualizados.
    6.  O Sistema exibe um status de sucesso e os detalhes atualizados do ambiente.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os dados do ambiente não publicado especificado são atualizados no sistema.

### **UC15: Deletar Ambientes Não Publicados**

* **Descrição:** Permite ao Gestor remover permanentemente um ou mais ambientes que ainda não foram publicados.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para deletar ambientes não publicados. Os ambientes a serem deletados existem e têm `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o(s) ambiente(s) não publicado(s) que deseja remover (UC04 ou UC05).
    2.  O Gestor preenche o corpo da requisição com a lista de IDs dos ambientes a serem removidos.
    3.  O Gestor realiza uma requisição `DELETE` ao endpoint `/api/ambientes/nao-publicados`.
    4.  O Sistema verifica se cada ambiente existe e está com `status = NAO_PUBLICADO`.
    5.  Se todos os ambientes forem encontrados e não estiverem publicados, o Sistema os remove do banco de dados.
    6.  O Sistema retorna `204 No Content`.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA02 - Erro de Exclusão:** Se ocorrer um erro ao remover o ambiente do banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os ambientes não publicados especificados são removidos permanentemente do sistema.

### **UC16: Alterar tipo e dados de Ambientes Não Publicados**

* **Descrição:** Permite ao Gestor alterar o tipo e os dados de um ambiente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para alterar ambientes não publicados. Os ambientes a serem alterados existem e têm `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o ambiente não publicado que deseja alterar o tipo e os dados (UC05).
    2.  O Gestor preenche o corpo da requisição com o novo tipo e os novos dados do ambiente: nome, localização (Bloco, Unidade e Andar), capacidade, geometrias (tipo de geometria, base, altura e repetição), pés-direitos, esquadrias (tipo, geometria com base/altura/repetição, material, altura do peitoril e informação adicional) e informação adicional.
    3.  O Gestor realiza uma requisição `POST` ao endpoint `/api/ambientes/nao-publicados/{id}`.
    4.  O Sistema verifica se cada ambiente existe e está com `status = NAO_PUBLICADO` e se os novos dados estiverem de acordo com as regras de negócio (RN-1.6).
    5.  Se as verificações forem bem-sucedidas, o Sistema cria um novo ambiente do tipo especificado no banco de dados, remove o ambiente antigo e retorna os dados do novo ambiente.
    6.  O Sistema retorna `201 Created` com os dados do novo ambiente (`AmbienteRes`) e o header `Location` apontando para `/api/ambientes/nao-publicados/{id}`.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe mensagens de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Atualização:** Se ocorrer um erro ao atualizar o ambiente no banco de dados, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** O ambiente especificado é removido do sistema e um novo ambiente é criado com tipo diferente e os novos dados.

### **UC17: Duplicar Ambiente Não Publicados**

* **Descrição:** Permite ao Gestor duplicar um ambiente que ainda não foi publicado.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para duplicar ambientes não publicados. Os ambientes a serem duplicados existem e têm `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o ambiente não publicado que deseja duplicar (UC05).
    2.  O Gestor preenche o corpo da requisição com o novo nome e a nova localização (Bloco, Unidade e Andar) do ambiente.
    3.  O Gestor realiza uma requisição `POST` ao endpoint `/api/ambientes/nao-publicados/{id}/duplicar`.
    4.  O Sistema verifica se o ambiente existe e está com `status = NAO_PUBLICADO` e está de acordo com as regras de negócio (RN-1.6 e RN-1.7).
    5.  Se as verificações forem bem-sucedidas, o Sistema cria um novo ambiente no banco de dados com base nos dados do ambiente especificado.
    6.  O Sistema retorna `201 Created` com os dados do novo ambiente (`AmbienteRes`) e o header `Location` apontando para `/api/ambientes/nao-publicados/{id}`.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados modificados não passarem na validação, o Sistema exibe uma mensagem de erro e não salva as alterações.
    * **FA02 - Ambiente Não Encontrado:** Se o ID fornecido não corresponder a um ambiente não publicado, o Sistema exibe uma mensagem de erro.
    * **FA03 - Erro de Persistência:** Se ocorrer um erro ao salvar o ambiente no banco de dados, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** Um novo ambiente é criado no sistema com o atributo `status = NAO_PUBLICADO` e possui um ID único.

### **UC18: Enviar Ambientes para Publicação**

* **Descrição:** O Gestor seleciona um ou mais ambientes não publicados e os submete ao processo de validação para torná-los públicos.
* **Ator Primário:** Gestor do Sistema.
* **Pré-condições:** O Gestor está autenticado e possui permissão para enviar ambientes para publicação. Existem ambientes com `status = NAO_PUBLICADO`.
* **Fluxo Principal:**
    1.  O Gestor obtém o(s) ambiente(s) não publicado(s) que deseja enviar para publicação (UC04 ou UC05).
    2.  O Gestor preenche o corpo da requisição com a lista de IDs dos ambientes obtidos.
    3.  O Gestor realiza uma requisição `PATCH` ao endpoint `/api/ambientes/nao-publicados/validar`.
    4.  O Sistema verifica se cada ambiente existe e está com `status = NAO_PUBLICADO`.
    5.  Se todos os ambientes forem válidos, o Sistema atualiza o status dos ambientes selecionados para `status = AGUARDANDO_VALIDACAO`.
    6.  O Sistema retorna `204 No Content`.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente Não Encontrado:** Se os IDs fornecidos não corresponderem a ambientes não publicados, o Sistema exibe uma mensagem de erro.
    * **FA02 - Erro no Envio:** Se ocorrer um erro ao atualizar o status dos ambientes, o Sistema informa o Gestor sobre a falha.
* **Pós-condições:** Os ambientes selecionados têm seu status alterado para indicar que estão pendentes de validação.

---

## Ator: Colaborador

### **UC19: Obter Detalhes de um Ambiente Publicado**

* **Descrição:** Permite ao Colaborador visualizar todas as informações detalhadas de um ambiente específico publicado, buscando por ID.
* **Ator Primário:** Colaborador.
* **Pré-condições:** O Colaborador está autenticado e possui o perfil de Colaborador (RN-4.3, RN-4.5). O ambiente a ser consultado existe e tem atributo `status = PUBLICADO`.
* **Fluxo Principal:**
        1. O Colaborador realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados/{id}`.
        2. O Sistema recupera e exibe os detalhes do ambiente solicitado.
        3. Os detalhes incluem as seguintes informações do ambiente:
            * ID;
            * Nome;
            * Localização (Bloco, Unidade e Andar);
            * Tipo;
            * Capacidade;
            * Geometrias do ambiente e suas áreas;
            * Área total do ambiente;
            * Pés-direitos;
            * Lista de Esquadrias com os seguintes dados:
                * Tipo (Porta, Janela, etc.);
                * Geometria (base, altura, repetição, área);
                * Material;
                * Altura do peitoril (se houver);
                * Informação adicional (se houver);
            * Resumo de Esquadrias por Tipo e Material (tipo, material, área total);
            * Informação Adicional;
            * Status.
* **Fluxos Alternativos:**
    * **FA01 - Ambiente não encontrado:** Se o ID não corresponder a nenhum ambiente publicado, o Sistema exibe uma mensagem de erro.
    * **FA02 - Erro ao obter detalhes:** Se ocorrer um erro ao buscar os detalhes do ambiente, o Sistema informa o Colaborador sobre a falha.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

### **UC20: Obter Detalhes de Esquadrias de uma lista de Ambientes Publicados**

* **Descrição:** Permite ao Colaborador visualizar todas as informações detalhadas de esquadrias de um conjunto de ambientes publicados de forma paginada.
* **Ator Primário:** Colaborador.
* **Pré-condições:** O Colaborador está autenticado e possui o perfil de Colaborador (RN-4.3, RN-4.5). Os ambientes a serem consultados existem e têm atributo `status = PUBLICADO`.
* **Fluxo Principal:**
        1. O Colaborador preenche os parâmetros de consulta com a lista de IDs dos ambientes publicados a serem consultados.
        2. O Colaborador realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados/esquadrias?ids=1,2,3`.
        3. O Sistema recupera e exibe, de forma paginada e limitada a 100 registros por página, os detalhes das esquadrias dos ambientes solicitados.
        4. Os detalhes incluem as seguintes informações:
            * Lista de ambientes consultados, cada um com:
                * Dados do ambiente (ID, Nome e Localização com Bloco, Unidade e Andar);
                * Detalhes das Esquadrias com:
                    * Lista de Esquadrias com os seguintes dados:
                        * Tipo (Porta, Janela, etc.);
                        * Geometria (base, altura, repetição, área);
                        * Material;
                        * Altura do peitoril (se houver);
                        * Informação adicional (se houver);
                    * Resumo por Tipo e Material (tipo, material, área total);
            * Resumo global de Esquadrias por Tipo e Material (`totalTipoMaterial`): tipo, material e área total agregada de todos os ambientes da página;
            * Dados de paginação (`dadosPaginacao`).
* **Fluxos Alternativos:**
    * **FA01 - Ambiente não encontrado:** Se algum dos IDs não corresponderem a um ambiente publicado, o Sistema exibe uma mensagem de erro.
    * **FA02 - Erro ao obter detalhes:** Se ocorrer um erro ao buscar os detalhes das esquadrias, o Sistema informa o Colaborador sobre a falha.
    * **FA03 - Nenhuma esquadria encontrada:** Se não houver esquadrias que correspondam aos filtros aplicados, o Sistema exibe uma mensagem indicando que a lista está vazia.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

---

## Ator: Público Externo

### **UC21: Listar Ambientes Publicados**

* **Descrição:** Permite ao Público Externo visualizar a lista de todos os ambientes publicados, filtrando por Nome ou Localização.
* **Ator Primário:** Público Externo.
* **Pré-condições:** O ambiente a ser consultado existe e tem atributo `status = PUBLICADO`. Os endpoints de listagem são públicos e não requerem autenticação.
* **Fluxo Principal:**
    * **FP1 - Todos os Ambientes Publicados:**
        1. O Público Externo realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados`.
        2. O Sistema recupera a lista de ambientes do banco de dados.
        3. O Sistema retorna a lista ao Público Externo de forma paginada, limitada a 100 registros por página.
        4. A lista retornada conterá os seguintes dados: ID, Nome, Localização (Bloco, Unidade e Andar), Tipo, Capacidade e Área. A resposta também incluirá `areaTotal` (soma das áreas dos ambientes da página) e `dadosPaginacao`.
    * **FP2 - Filtrar por Nome:**
        1. O Público Externo realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados/nome?nome={nome}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao nome fornecido.
        3. Repete os passos 3 e 4 do FP1.
    * **FP3 - Filtrar por Localização:**
        1. O Público Externo realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados/localizacao?bloco={bloco}&unidade={unidade}&andar={andar}`.
        2. O Sistema recupera a lista de ambientes que correspondem aos parâmetros de localização fornecidos.
        3. Repete os passos 3 e 4 do FP1.
    * **FP4 - Filtrar por Tipo:**
        1. O Público Externo realiza uma requisição `GET` ao endpoint `/api/ambientes/publicados/tipo?tipo={tipo}`.
        2. O Sistema recupera a lista de ambientes que correspondem ao tipo fornecido.
        3. Repete os passos 3 e 4 do FP1.
* **Fluxos Alternativos:**
    * **FA01 - Nenhum ambiente encontrado:** Se não houver ambientes publicados, o Sistema exibe uma mensagem indicando que a lista está vazia.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

---

## Ator: Administrador

### **UC22: Listar Usuários**

* **Descrição:** Permite ao Administrador visualizar a lista paginada de todos os usuários do sistema.
* **Ator Primário:** Administrador.
* **Pré-condições:** O Administrador está autenticado e possui o perfil `ROLE_ADMINISTRADOR`.
* **Fluxo Principal:**
    1. O Administrador realiza uma requisição `GET` ao endpoint `/api/usuarios`.
    2. O Sistema recupera a lista de usuários do banco de dados de forma paginada.
    3. O Sistema retorna a lista ao Administrador com os dados de paginação.
    4. A lista retornada conterá os seguintes dados de cada usuário: ID, Email, Nome, Ativo, CriadoEm e Perfis (conjunto de roles: ROLE_COLABORADOR, ROLE_VALIDADOR, ROLE_GESTOR_SISTEMA, ROLE_ADMINISTRADOR).
* **Fluxos Alternativos:**
    * **FA01 - Nenhum usuário encontrado:** Se não houver usuários cadastrados, o Sistema retorna uma lista vazia.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

### **UC23: Obter Usuário por Email**

* **Descrição:** Permite ao Administrador visualizar os detalhes de um usuário específico buscando por email.
* **Ator Primário:** Administrador.
* **Pré-condições:** O Administrador está autenticado e possui o perfil `ROLE_ADMINISTRADOR`. O usuário a ser consultado existe no sistema.
* **Fluxo Principal:**
    1. O Administrador realiza uma requisição `GET` ao endpoint `/api/usuarios/email/{email}`.
    2. O Sistema recupera e exibe os detalhes do usuário solicitado.
    3. Os detalhes incluem as seguintes informações do usuário: ID, Email, Nome, Ativo, CriadoEm e Perfis.
* **Fluxos Alternativos:**
    * **FA01 - Usuário não encontrado:** Se o email não corresponder a nenhum usuário, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

### **UC24: Listar Usuários por Nome**

* **Descrição:** Permite ao Administrador visualizar a lista paginada de usuários filtrando por nome.
* **Ator Primário:** Administrador.
* **Pré-condições:** O Administrador está autenticado e possui o perfil `ROLE_ADMINISTRADOR`.
* **Fluxo Principal:**
    1. O Administrador realiza uma requisição `GET` ao endpoint `/api/usuarios/nomes/{nome}`.
    2. O Sistema recupera a lista de usuários que correspondem ao nome fornecido de forma paginada.
    3. O Sistema retorna a lista ao Administrador com os dados de paginação.
    4. A lista retornada conterá os seguintes dados de cada usuário: ID, Email, Nome, Ativo, CriadoEm e Perfis.
* **Fluxos Alternativos:**
    * **FA01 - Nenhum usuário encontrado:** Se não houver usuários que correspondam ao nome fornecido, o Sistema retorna uma lista vazia.
* **Pós-condições:** Nenhuma alteração no estado do sistema.

### **UC25: Atualizar Perfis de um Usuário**

* **Descrição:** Permite ao Administrador atualizar os perfis (roles) de um usuário existente no sistema.
* **Ator Primário:** Administrador.
* **Pré-condições:** O Administrador está autenticado e possui o perfil `ROLE_ADMINISTRADOR`. O usuário a ser atualizado existe no sistema.
* **Fluxo Principal:**
    1. O Administrador obtém os dados atuais do usuário (UC23 ou UC24).
    2. O Administrador preenche o corpo da requisição com o conjunto de perfis desejados (ROLE_COLABORADOR, ROLE_VALIDADOR, ROLE_GESTOR_SISTEMA, ROLE_ADMINISTRADOR).
    3. O Administrador realiza uma requisição `PATCH` ao endpoint `/api/usuarios/{id}/perfis`.
    4. O Sistema valida os dados da requisição.
    5. Se a validação for bem-sucedida, o Sistema atualiza os perfis do usuário no banco de dados.
    6. O Sistema retorna `204 No Content`.
* **Fluxos Alternativos:**
    * **FA01 - Erro de Validação:** Se os dados fornecidos não passarem na validação (ex: conjunto vazio), o Sistema exibe mensagens de erro.
    * **FA02 - Usuário Não Encontrado:** Se o ID fornecido não corresponder a um usuário, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** Os perfis do usuário especificado são atualizados no sistema.

### **UC26: Desativar Usuário**

* **Descrição:** Permite ao Administrador desativar um usuário existente no sistema, impedindo seu acesso.
* **Ator Primário:** Administrador.
* **Pré-condições:** O Administrador está autenticado e possui o perfil de Administrador. O usuário a ser desativado existe no sistema.
* **Fluxo Principal:**
    1. O Administrador obtém o usuário que deseja desativar (UC23 ou UC24).
    2. O Administrador realiza uma requisição `PATCH` ao endpoint `/api/usuarios/{id}/desativar`.
    3. O Sistema verifica se o usuário existe.
    4. Se o usuário for encontrado, o Sistema o desativa no banco de dados (atributo `ativo = false`).
    5. O Sistema retorna `204 No Content`.
* **Fluxos Alternativos:**
    * **FA01 - Usuário Não Encontrado:** Se o ID fornecido não corresponder a um usuário, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** O usuário especificado é desativado no sistema e não poderá mais realizar login.

### **UC27: Ativar Usuário**

* **Descrição:** Permite ao Administrador reativar um usuário desativado no sistema, restaurando seu acesso.
* **Ator Primário:** Administrador.
* **Pré-condições:** O Administrador está autenticado e possui o perfil de Administrador. O usuário a ser ativado existe no sistema e está desativado.
* **Fluxo Principal:**
    1. O Administrador obtém o usuário que deseja ativar (UC23 ou UC24).
    2. O Administrador realiza uma requisição `PATCH` ao endpoint `/api/usuarios/{id}/ativar`.
    3. O Sistema verifica se o usuário existe.
    4. Se o usuário for encontrado, o Sistema o reativa no banco de dados (atributo `ativo = true`).
    5. O Sistema retorna `204 No Content`.
* **Fluxos Alternativos:**
    * **FA01 - Usuário Não Encontrado:** Se o ID fornecido não corresponder a um usuário, o Sistema exibe uma mensagem de erro.
* **Pós-condições:** O usuário especificado é reativado no sistema e poderá realizar login novamente.

---

## Ator: Qualquer Usuário (Autenticação)

### **UC28: Realizar Login via Google OAuth2**

* **Descrição:** Permite ao usuário realizar login no sistema utilizando sua conta Google através do protocolo OAuth2.
* **Ator Primário:** Qualquer Usuário (Colaborador, Validador, Gestor do Sistema ou Administrador).
* **Pré-condições:** O usuário possui uma conta Google válida e está cadastrado no sistema com um perfil ativo.
* **Fluxo Principal:**
    1. O Usuário acessa o endpoint `GET /oauth2/authorization/google`.
    2. O Sistema redireciona o Usuário para a página de autenticação do Google.
    3. O Usuário se autentica no Google e autoriza o acesso às informações básicas (email e nome).
    4. O Sistema valida se o email pertence a um domínio autorizado e se o usuário existe no sistema.
    5. O Sistema verifica se o usuário está ativo.
    6. Se todas as validações forem bem-sucedidas, o Sistema gera um access token (JWT) e um refresh token.
    7. O Sistema define um cookie `refreshToken` com o refresh token.
    8. O Sistema redireciona o Usuário para a URL de sucesso do frontend com o access token na URL (fragmento `#token=...`).
* **Fluxos Alternativos:**
    * **FA01 - Usuário inativo:** Se o usuário existir mas estiver inativo, o acesso é negado e o Usuário é redirecionado para a página de erro.
    * **FA02 - Domínio não autorizado:** Se o email não pertencer a um domínio autorizado (usuário externo não cadastrado), o acesso é negado.
    * **FA03 - Email ou nome não fornecido:** Se o Google não fornecer o email ou o nome do usuário, o acesso é negado.
    * **FA04 - Falha na autenticação:** Se ocorrer qualquer erro durante o processo de autenticação, o Usuário é redirecionado para a página de erro.
* **Pós-condições:** O Usuário está autenticado com um access token (JWT) válido e um refresh token armazenado em cookie.

### **UC29: Renovar Token de Acesso**

* **Descrição:** Permite ao usuário renovar seu access token (JWT) utilizando o refresh token armazenado em cookie, sem precisar realizar login novamente. O refresh token (validade de 1 hora) é renovado apenas quando seu tempo restante de vida é menor que a expiração do access token; caso contrário, o mesmo refresh token é reutilizado.
* **Ator Primário:** Qualquer Usuário autenticado.
* **Pré-condições:** O Usuário possui um refresh token válido armazenado em cookie.
* **Fluxo Principal:**
    1. O Usuário realiza uma requisição `POST` ao endpoint `/auth/refresh`.
    2. O Sistema verifica se o cookie `refreshToken` está presente na requisição.
    3. O Sistema valida o refresh token.
    4. Se o token for válido, o Sistema gera um novo access token (JWT) para o usuário.
    5. O Sistema verifica se o tempo restante do refresh token é menor que a expiração do access token. Se for, gera um novo refresh token (revogando o anterior); caso contrário, mantém o refresh token atual.
    6. Se um novo refresh token foi gerado, o Sistema emite um novo cookie `Set-Cookie` com o token atualizado.
    7. O Sistema retorna `200 OK` com o novo access token (`LoginRes`: accessToken, tokenType "Bearer", expiresIn).
* **Fluxos Alternativos:**
    * **FA01 - Cookie ausente:** Se o cookie `refreshToken` não estiver presente, o Sistema retorna `401 Unauthorized`.
    * **FA02 - Token inválido:** Se o refresh token for inválido ou expirado, o Sistema retorna `401 Unauthorized`.
* **Pós-condições:** O Usuário possui um novo access token válido. O refresh token pode ter sido renovado (se o tempo restante era menor que a expiração do access token) ou mantido.

### **UC30: Encerrar Sessão (Logout)**

* **Descrição:** Permite ao usuário encerrar sua sessão no sistema, invalidando o refresh token e limpando o cookie.
* **Ator Primário:** Qualquer Usuário autenticado.
* **Pré-condições:** O Usuário possui um refresh token armazenado em cookie (pode estar expirado).
* **Fluxo Principal:**
    1. O Usuário realiza uma requisição `POST` ao endpoint `/auth/logout`.
    2. O Sistema verifica se o cookie `refreshToken` está presente na requisição.
    3. O Sistema invalida o refresh token no banco de dados (se existir).
    4. O Sistema limpa o cookie `refreshToken` definindo um novo cookie vazio e expirado.
    5. O Sistema retorna `204 No Content`.
* **Fluxos Alternativos:**
    * **FA01 - Cookie ausente:** Se o cookie `refreshToken` não estiver presente, o Sistema ainda retorna `204 No Content` (operação idempotente).
* **Pós-condições:** O refresh token é invalidado e o cookie é limpo. O Usuário não poderá mais renovar seu access token e precisará realizar login novamente.