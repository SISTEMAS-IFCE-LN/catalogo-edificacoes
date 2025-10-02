# Contexto dos Ambientes Internos

## Casos de Uso

A seguir estão descritos os principais casos de uso relacionados ao gerenciamento e consulta dos ambientes internos das edificações do IFCE.

### Ator: Gestor

#### UC01: Gerenciar Catálogo de Ambientes

Descrição: Este é o principal caso de uso para o gestor, englobando todas as operações de criação, leitura, atualização e exclusão (CRUD) de ambientes.

   - Fluxo Principal:

      1. O gestor acessa a área de gerenciamento do catálogo.

      2. O sistema exibe uma lista de ambientes já cadastrados.

      3. O gestor pode optar por:

         a) Adicionar um novo ambiente (inicia UC02).

         b) Editar um ambiente existente.

         c) Consultar os detalhes de um ambiente.

         d) Remover um ambiente.

#### UC02: Cadastrar Novo Ambiente Interno

Descrição: O gestor preenche todas as informações necessárias para catalogar um novo ambiente, de acordo com as regras de negócio.

   - Fluxo Principal:

      1. O gestor inicia o cadastro a partir do UC01.

      2. Informa os dados básicos: Nome e Localização.

      3. Adiciona os ElementosConstrutivos: cadastra o Piso, as Paredes e o Teto, especificando suas formas geométricas, tipos e revestimentos.

      4. Adiciona as Esquadrias: cadastra Portas e Janelas com suas características.

      5. Adiciona os Componentes e Equipamentos presentes no ambiente.

      6. O gestor salva o novo ambiente. O sistema valida as informações e gera um ID único.

#### UC03: Gerar QR Code para Ambiente

Descrição: Após cadastrar ou ao consultar um ambiente, o gestor pode gerar e imprimir um QR Code.

   - Fluxo Principal:

      1. O gestor seleciona um ambiente.

      2. Clica na opção "Gerar QR Code".

      3. O sistema exibe o QR Code em um formato para impressão.

#### UC04: Gerar Relatórios de Infraestrutura

Descrição: O gestor pode extrair dados consolidados do catálogo para fins de planejamento e manutenção.

Exemplos: "Listar todos os ambientes com piso do tipo Porcelanato", "Calcular a carga elétrica total do Bloco B", "Relatório de todos os extintores de incêndio e suas localizações".

### Atores: Servidor e Publico Externo

#### UC05: Consultar Informações de Ambiente

Descrição: O usuário busca e visualiza os detalhes de um ambiente específico.

   - Fluxo Principal:

      1. O usuário acessa a área de consulta do catálogo.

      2. Pode buscar um ambiente pelo nome, localização ou ID.

      3. O sistema exibe a página de detalhes do ambiente com todas as suas características catalogadas.

      Nota: O nível de detalhe exibido pode variar entre o Servidor e o Publico Externo, conforme a RN13.

#### UC06: Consultar Ambiente via QR Code

Descrição: O usuário utiliza um dispositivo móvel para ler o QR Code fixado em um ambiente e acessar suas informações diretamente.

   - Fluxo Principal:

      1. O usuário lê o QR Code com a câmera do celular.

      2. O dispositivo abre o navegador e carrega a página de consulta (UC05) para o ambiente correspondente.