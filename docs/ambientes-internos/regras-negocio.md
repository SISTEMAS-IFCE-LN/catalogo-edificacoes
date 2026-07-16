# Contexto dos Ambientes Internos

## Regras de Negócio

As regras a seguir detalham os invariantes e comportamentos do subdomínio de Ambientes Internos.

### 1. Regras Gerais do Ambiente

* **RN-1.1:** Cada ambiente possui um identificador único no sistema para garantir sua integridade e facilitar a gestão.
* **RN-1.2:** Todo ambiente deve ter um nome descritivo e uma localização associada a uma edificação ou bloco do campus.
* **RN-1.3:** O sistema deve permitir a geração de um identificador físico (QR Code) para cada ambiente, que ao ser lido direcionará o usuário para a tela de consulta com as informações daquele local.
* **RN-1.4:** Cada ambiente deve ser classificado em um tipo específico (sala de aula, auditório, sala administrativa, etc.).
* **RN-1.5:** Um ambiente deve conter listas de pisos, paredes (podendo ser externas ou internas), tetos, esquadrias e componentes, a fim de detalhar suas características construtivas e funcionais.
* **RN-1.6:** Para ser cadastrado, um ambiente deve ter no mínimo: nome, localização, capacidade, tipo, pelo menos uma geometria, um pé-direito e uma porta. Nenhum desses atributos pode ser nulo, vazio ou em branco.
* **RN-1.7:** Não pode haver dois ambientes com o mesmo nome na mesma localização.
* **RN-1.8:** Todo ambiente possui um estado que controla sua visibilidade e as operações permitidas.
* **RN-1.9:** O sistema deve permitir a associação de múltiplas geometrias a um ambiente para representar sua forma física com precisão (retangular, triangular, etc.).
* **RN-1.10:** A capacidade de um ambiente deve ser expressa em número inteiro, representando a quantidade máxima de pessoas que o ambiente pode acomodar confortavelmente.
* **RN-1.11:** O pé-direito de um ambiente refere-se à altura do piso ao teto e deve ser um valor positivo.
* **RN-1.12:** Todas as dimensões que representam medidas lineares devem ser expressas em metros (m) e as áreas em metros quadrados (m²), respeitando a precisão de duas casas decimais.
* **RN-1.13:** O sistema deve fornecer mecanismos para calcular automaticamente a área total de um ou mais ambientes, seus elementos construtivos e esquadrias, com base nas geometrias associadas. No caso dos elementos construtivos e esquadrias, deve ser possível calcular a área por tipo.
* **RN-1.14:** O sistema deve fornecer mecanismos para calcular a potência total instalada em um ou mais ambientes, considerando os componentes que consomem energia.

### 2. Regras sobre Elementos Construtivos e Esquadrias

* **RN-2.1:** Cada elemento construtivo (piso, parede e teto) é definido por uma quantidade ou repetição, um tipo ou material, uma ou mais geometrias e atributos adicionais específicos.
* **RN-2.2:** A área total de um elemento construtivo deve ser calculada a partir da soma das áreas de uma ou mais geometrias, multiplicada pela quantidade do elemento.
* **RN-2.3:** A área de uma parede deve descontar a área total das esquadrias nela contidas.
* **RN-2.4:** Uma esquadria (porta ou janela) possui uma geometria principal que define sua área total e uma lista de componentes próprios.
* **RN-2.5:** Cada componente da esquadria possui sua própria geometria, material e atributos específicos.
* **RN-2.6:** Uma janela deve conter um peitoril, que possuirá altura do piso, material e geometria próprios.
* **RN-2.7:** Parede e teto devem obrigatoriamente possuir um revestimento associado.

### 3. Regras sobre Componentes

* **RN-3.1:** Um componente representará todos os itens adicionais em um ambiente, possuindo pelo menos um tipo e uma quantidade ou repetição.
* **RN-3.2:** Componentes que consomem energia devem permitir o cálculo de sua potência em Watts.
* **RN-3.3:** Um equipamento é definido por seu tipo e potência individual em Watts.
* **RN-3.4:** Uma luminária é definida pelo seu material predominante, tipo de fixação, quantidade de lâmpadas e se possui ou não aletas.
* **RN-3.5:** A lâmpada deve compor uma luminária e possuir as seguintes informações: tipo, formato e potência em Watts.
* **RN-3.6:** Os componentes de instalação elétrica, lógica e hidrossanitária envolvem itens como tomadas, interruptores, pias, sanitários, etc. Cada componente deste possui atributos específicos.
* **RN-3.7:** Um extintor é um componente definido pelos seguintes atributos: capacidade, unidade (referente à capacidade), tipo e a lista de classes de incêndio que ele combate.

### 4. Regras de Acesso e Gestão

* **RN-4.1:** Um **Validador** é responsável por gerenciar o estado de publicação dos ambientes. Suas ações incluem:
    * Listar todos os ambientes submetidos a validação, podendo pesquisá-los por nome, localização ou tipo.
    * Obter detalhes de um ambiente submetido a validação.
    * Publicar um ambiente que foi submetido a validação.
    * Tornar um ambiente submetido a validação ou publicado como não publicado (privar).

* **RN-4.2:** Usuários com perfil de **Gestor do Sistema** podem gerenciar ambientes que ainda não foram publicados. Suas ações incluem:
    * Listar todos os ambientes não publicados, podendo pesquisá-los por nome, localização ou tipo.
    * Obter detalhes de um ambiente não publicado.
    * Cadastrar um novo ambiente como não publicado.
    * Atualizar os dados de um ambiente não publicado.
    * Excluir um ambiente não publicado.
    * Enviar um ou mais ambientes não publicados para o fluxo de validação e publicação.

* **RN-4.3:** Usuários com perfil de **Colaborador** podem apenas consultar informações completas dos ambientes que estão publicados. Suas ações incluem:
    * Obter detalhes completos de um ambiente publicado.
    * Consultar, de forma paginada, as esquadrias de um conjunto de ambientes publicados.

* **RN-4.4:** Usuários identificados como **Público Externo** podem apenas consultar informações simplificadas dos ambientes que estão publicados. Suas ações incluem:
    * Listar informações básicas de todos os ambientes publicados, podendo pesquisá-los por nome, localização ou tipo.
    * O acesso a esta listagem e aos respectivos detalhes ocorre sem autenticação (rota pública), não exigindo login.

* **RN-4.5:** E-mails com domínio institucional configuram o acesso institucional: no primeiro login, o usuário é provisionado automaticamente com o perfil de Colaborador, desde que a conta esteja válida.

* **RN-4.6:** O nome do usuário é sincronizado com o provedor de identidade em cada login, garantindo que o nome armazenado no sistema reflita sempre o nome atualizado no provedor.

* **RN-4.7:** Usuários externos (e-mail fora do domínio institucional) só obtêm acesso se estiverem pré-cadastrados por um Administrador. Caso contrário, o sistema recusa o acesso.

* **RN-4.8:** Um mesmo usuário pode acumular múltiplos perfis simultâneos. Todo usuário deve manter, no mínimo, o perfil de Colaborador (regra universal cumulativa).

* **RN-4.9:** **Lockout prevention:** é vedado remover o perfil de Administrador ou desativar o usuário quando existir apenas um Administrador ativo no sistema. Nessas condições, a operação deve ser rejeitada, preservando ao menos um Administrador ativo.

* **RN-4.10:** A exclusão de um ambiente é física e permanente, removendo todos os dados associados ao ambiente do sistema.
