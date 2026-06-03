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
             ┌─────────────────┴─────────────────┐
             ▼                                   ▼
┌─────────────────────────┐         ┌─────────────────────────┐
│     security-module     │         │    módulos de negócio   │
│  (Identidade e Acesso)  │         │ (Ex: ambientes-internos)│
└─────────────────────────┘         └─────────────────────────┘

```

### 1.1. Módulo de Inicialização e Orquestração (`main-app`)

* **Papel:** Atua como a raiz de composição (Composition Root) do sistema.
* **Responsabilidade:** Este módulo não possui regras de negócio ou de segurança corporativa. Sua única função é
  consolidar os demais submódulos em uma única unidade executável final, gerenciar as propriedades de configuração
  global do ambiente e carregar os dados essenciais para o funcionamento inicial do sistema.

### 1.2. Módulo de Segurança (`security-module`)

* **Papel:** Centraliza o controle de acessos, autenticação de identidades e auditoria de usuários.
* **Responsabilidade:** É o encarregado de interceptar as requisições HTTP recebidas e verificar as permissões
  declaradas. Isola toda a infraestrutura de login, gestão de contas e atribuição horizontal de perfis. Os demais
  módulos de negócio recorrem a este componente apenas para validar as credenciais anexadas às requisições, mantendo
  seus códigos limpos de lógicas de infraestrutura de segurança.

### 1.3. Módulos de Domínio e Negócio (Ex: `ambientes-internos-module`)

* **Papel:** Encapsulam as regras core e as operações de negócio específicas de cada segmento do sistema.
* **Responsabilidade:** Contêm as entidades de domínio, os casos de uso e as interfaces de persistência. A arquitetura
  multi-módulos permite que novos subdomínios (como módulos para áreas externas ou relatórios avançados) sejam acoplados
  como novos módulos irmãos, compartilhando o mesmo ecossistema de segurança sem impactar os recursos já existentes.

---

## 2. Padrão Arquitetural Interno das Camadas

Dentro de cada módulo de negócio, adota-se uma divisão interna por camadas funcionais para garantir a testabilidade e a
evolução independente do código:

1. **Camada de Exposição (Controladores REST):** Responsável por receber as requisições externas, realizar validações
   sintáticas primárias de entrada e formatar os dados de resposta para o cliente.
2. **Camada de Aplicação (Casos de Uso / Serviços):** Onde reside a lógica de negócio e a coordenação das operações. É
   nesta camada que as regras operacionais são processadas de forma isolada de preocupações de rede ou banco de dados.
3. **Camada de Acesso a Dados (Repositórios):** Interfaces responsáveis pela abstração dos mecanismos de persistência e
   consultas ao banco de dados.
4. **Camada de Domínio (Entidades):** Representação pura dos modelos conceituais do sistema, contendo seus atributos e
   comportamentos fundamentais.