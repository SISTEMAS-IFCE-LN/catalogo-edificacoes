# Domínio do Catálogo de Edificações do IFCE

## Visão Geral do Domínio

O Catálogo de Edificações é um sistema de gestão e documentação das edificações do Instituto Federal de Educação, Ciência e Tecnologia do Ceará (IFCE), Campus de Limoeiro do Norte. O propósito do sistema é organizar, validar e disponibilizar informações técnicas e arquitetônicas das edificações do campus para diferentes públicos.

O domínio central gira em torno da **catalogação de ambientes**, onde cada ambiente representa um espaço físico com características construtivas específicas, passando por um ciclo de vida controlado desde sua criação até sua publicação oficial.

---

## Subdomínios

O domínio é dividido em subdomínios core (negócio principal) e de suporte (funcionalidades auxiliares):

### Subdomínios Core

#### Ambientes Internos

Representa espaços fechados utilizados para atividades acadêmicas e administrativas. Inclui salas de aula, laboratórios, bibliotecas, auditórios, salas administrativas e outros espaços cobertos.

Ambientes internos possuem elementos construtivos essenciais (pisos, paredes, tetos, portas) que são **obrigatórios** para sua existência. Podem conter componentes opcionais (equipamentos, luminárias, extintores) e suportam múltiplas geometrias para representação precisa de sua forma física.

#### Ambientes Externos

Representa áreas abertas que compõem a infraestrutura do campus. Inclui pátios, jardins, estacionamentos, ruas internas e outras áreas descobertas.

Ambientes externos não possuem elementos construtivos tradicionais e possuem forma de delimitação ainda em definição (épico em evolução). São separados dos ambientes internos por diferenças estruturais fundamentais.

### Subdomínios de Suporte

#### Identidade e Controle de Acesso

Responsável pela gestão de usuários, autenticação e autorização. Controla quem pode acessar o sistema e quais operações cada usuário pode realizar.

Este subdomínio gerencia o provisionamento de usuários, atribuição de papéis (Gestor, Validador, Colaborador, Administrador) e as regras de acesso aos ambientes conforme seu estado de publicação.

---

## Entidades e Agregados

### Ambiente (Agregado Raiz)

Entidade central do domínio. Representa um espaço físico catalogado com todas as suas características construtivas e funcionais. Cada ambiente possui um ciclo de vida controlado (criação, validação, publicação) e é composto por elementos construtivos, esquadrias e componentes.

### Elementos Construtivos

Componentes estruturais essenciais para a existência de um ambiente interno. Inclui pisos, paredes e tetos. São **obrigatórios** — um ambiente interno não existe sem eles.

### Esquadrias

Aberturas nos elementos construtivos, especificamente em paredes. Inclui portas (acesso ao ambiente) e janelas (aberturas para iluminação/ventilação). São **obrigatórias**.

### Componentes

Itens adicionais que podem ou não existir em um ambiente. Inclui equipamentos, luminárias, instalações (elétrica, lógica, hidrossanitária) e extintores. São **opcionais**.

### Usuário

Entidade do subdomínio de Identidade e Controle de Acesso. Representa uma pessoa com acesso ao sistema, possuindo um ou mais papéis que determinam suas permissões.

---

## Ciclo de Vida do Ambiente

Um ambiente passa por três estados ao longo de seu ciclo de vida:

### Estados

1. **Não Publicado**: Estado inicial após criação. Ambiente acessível apenas para edição pelo Gestor do Sistema.
2. **Aguardando Validação**: Ambiente submetido para revisão. Acessível ao Validador para análise.
3. **Publicado**: Ambiente oficial e imutável. Acessível para consulta por Colaboradores e Público Externo.

### Transições

```
┌─────────────────┐
│                 │
│  Não Publicado  │◄─────────────────────────────────┐
│                 │                                  │
└────────┬────────┘                                  │
         │                                           │
         │ [Gestor submete para validação]           │
         │                                           │
         ▼                                           │
┌─────────────────────────┐                          │
│                         │    [Validador priva]     │
│  Aguardando Validação   │__________________________|
│                         │                          │
└────────┬────────────────┘                          │
         │                                           │
         │                                           │
         │ [Validador publica]                       │
         |                                           │
         │                                           │
         ▼                                           │
┌─────────────────────────┐                          │
│                         │     [Validador priva]    │
│        Publicado        │__________________________│
│                         │                          
└─────────────────────────┘ 

```

### Invariantes de Estado

- Um ambiente publicado é **imutável** — para ser editado, deve retornar ao estado "Não Publicado"
- Um ambiente só pode ser criado no estado "Não Publicado"
- Apenas o Validador pode publicar ou privar ambientes

---

## Atores e Papéis

### Gestor do Sistema

Responsável pela criação e manutenção dos ambientes antes da publicação. Cria, edita e submete ambientes para validação.

### Validador

Responsável pela revisão e aprovação dos ambientes antes da publicação oficial. Analisa, publica ou priva ambientes.

### Colaborador

Usuário institucional que consulta informações completas dos ambientes publicados.

### Público Externo

Usuário não autenticado que consulta informações básicas dos ambientes publicados. Não requer autenticação.

### Administrador

Responsável pela gestão de usuários e delegação de papéis. Garante que sempre exista pelo menos um Administrador ativo no sistema (prevenção de lockout).

### Regras Gerais de Papéis

- Papéis podem ser acumulados (um usuário pode possuir múltiplos papéis simultaneamente)
- Papéis são independentes (não há hierarquia implícita entre eles)
- Todo usuário possui automaticamente o papel de Colaborador

---

## Eventos de Domínio

Eventos significativos que ocorrem no domínio e podem ser de interesse para outros contextos:

- **AmbienteCriado**: Um novo ambiente foi criado (estado: Não Publicado)
- **AmbienteEditado**: Um ambiente não publicado foi modificado
- **AmbienteExcluido**: Um ambiente foi removido do sistema
- **AmbienteSubmetidoParaValidacao**: Um ambiente foi enviado para validação (estado: Aguardando Validação)
- **AmbientePublicado**: Um ambiente foi aprovado e publicado (estado: Publicado)
- **AmbientePrivado**: Um ambiente foi rejeitado ou retornado para edição (estado: Não Publicado)
- **UsuarioCriado**: Um novo usuário foi provisionado no sistema
- **PapeisUsuarioAtualizados**: Os papéis de um usuário foram modificados
- **UsuarioDesativado**: Uma conta de usuário foi desativada

---

## Glossário de Termos de Negócio

- **Ambiente**: Espaço físico catalogado com características construtivas específicas
- **Elemento Construtivo**: Componente estrutural essencial (piso, parede, teto)
- **Esquadria**: Abertura em elemento construtivo (porta, janela)
- **Componente**: Item adicional opcional (equipamento, luminária, extintor)
- **Geometria**: Representação da forma física (retangular, triangular, etc.)
- **Pé-direito**: Altura do piso ao teto
- **Capacidade**: Número máximo de pessoas que o ambiente pode acomodar
- **Publicar**: Aprovar um ambiente, tornando-o oficial e imutável
- **Privar**: Rejeitar ou retornar um ambiente para edição
- **Submeter**: Enviar um ambiente para validação
- **Colaborador**: Usuário institucional com acesso completo a ambientes publicados
- **Gestor do Sistema**: Usuário responsável por criar e editar ambientes antes da publicação
- **Validador**: Usuário responsável por revisar e aprovar ambientes
- **Administrador**: Usuário responsável pela gestão de papéis e contas
