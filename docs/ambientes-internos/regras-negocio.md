# Contexto dos Ambientes Internos

## Regras de Negócio

As regras a seguir complementam e detalham as regras iniciais, incorporando a estrutura definida no diagrama de classes e o contexto do domínio do IFCE.

1. Regras Gerais do Ambiente

   *RN01*: Cada ambiente deve possuir um identificador único (ID) no sistema para garantir a integridade e facilitar a gestão.

   *RN02*: Todo ambiente deve ter um nome descritivo (ex: "Laboratório de Química") e uma localização associada a uma edificação ou bloco do campus (ex: "Bloco A, Sala 102").

   *RN03*: O sistema deve permitir a geração de um identificador físico (QR Code) para cada ambiente, que, ao ser lido, direcionará o usuário para a tela de consulta com as informações daquele local.

   *RN04*: Cada ambiente deve ser classificado em um tipo específico, como "Sala de Aula", "Laboratório", "Auditório", "Banheiro", entre outros.

   *RN05*: Um ambiente genérico deve possuir como informações mínimas:
      - Nome;
      - Localização: pertence a um bloco ou edificação específica;
      - Características do Piso: Área, Tipo de Piso;
      - Características das Paredes: Área, Tipo da Parede e Revestimentos;
      - Características do Teto: Área, Tipo do Teto, Revestimentos;
      - Características das Janelas: Área, Material, Tipo de Abertura, Folhas, Peitoril e Bandeirola;
      - Características das Portas: Área, Material, Tipo de Abertura, Folhas, Bandeirola e Visor;
      - Componentes Eletricos: Tomadas, Interruptores e Iluminação;
      - Componentes de TI: Pontos de Rede, Racks, etc.;
      - Componentes Hidrosanitários: Pias, Vasos Sanitários, Chuveiros, etc.;
      - Equipamentos: Computadores, Projetores, Extintores, Ar Condicionado, etc.

   *RN06*: Para ser cadastrado, um ambiente deve ter no mínimo um Piso, um Teto e pelo menos três Paredes cadastradas.

2. Regras sobre Calculo de Áreas

   *RN07*: A área total de um `ElementoConstrutivo` (Piso, Parede, Teto) deve ser calculada a partir da soma das áreas de uma ou mais `FormasGeometricas`, permitindo o cadastro de superfícies com formatos irregulares.

   *RN08*: A área de uma `Esquadria` (Porta, Janela) deve ser calculada a partir de uma única `FormaGeometrica`.

   *RN09*: A área total de um tipo específico de `ElementoConstrutivo` (Piso, Parede, Teto) ou `Esquadria` (Porta, Janela) em um ambiente deve ser a soma das áreas de todas as instâncias deste presentes no ambiente. No caso de `Paredes`, a área total deve considerar a subtração das áreas ocupadas por `Esquadrias` (Portas, Janelas) para refletir a área real da superfície.

   *RN10*: Todo `ElementoConstrutivoAcabado` (Parede, Teto) deve obrigatoriamente possuir um tipo de Revestimento associado.

   *RN11*: Toda `Esquadria` (Porta, Janela) deve ser especificada com um `MaterialEsquadria` e um tipo de `Abertura`.

3. Regras sobre Componentes e Equipamentos

   *RN12*: Todo `Equipamento` que consome energia (como Ar Condicionado, Computador, Projetor) deve ter sua potência em Watts registrada para possibilitar o cálculo da carga elétrica do ambiente.

   *RN13*: Toda `Luminaria` deve ser especificada com `TipoLampada`, `FormatoLampada`, `Instalacao` e `MaterialPredominante`.

   *RN14*: Todos os `Componentes` (`CompEletrico`, `CompLogico`, `CompHidrossanitario`) e `Equipamentos` devem ter sua quantidade registrada.

4. Regras de Acesso e Gestão

    RN14: Apenas usuários com o perfil de Gestor podem criar, atualizar e remover registros de ambientes e seus componentes no catálogo.

    *RN15*: Usuários com o perfil de `Servidor` e `Publico Externo` podem apenas consultar as informações do catálogo.

    *RN16*: As informações disponíveis para o `Publico Externo` podem ser uma versão simplificada do catálogo, ocultando detalhes técnicos específicos de infraestrutura interna.

