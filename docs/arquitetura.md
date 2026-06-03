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
             ┌─────────────────┴───────────────────────┐
             ▼                                         ▼
┌─────────────────────────┐         ┌────────────────────────────────────┐
│     security-module     │         │     ambientes-internos-module      │
│  (Identidade e Acesso)  │         │   (Domínio de Ambientes Internos)  │
└─────────────────────────┘         └────────────────────────────────────┘

```

### 1.1. Módulo de Inicialização e Orquestração (`main-app`)

* **Papel:** Atua como a raiz de composição (*Composition Root*) do sistema — o único módulo executável.
* **Responsabilidade:** Este módulo não possui regras de negócio ou de segurança corporativa. Sua função é:
    * Consolidar `security-module` e `ambientes-internos-module` em uma única unidade executável final.
    * Conter a classe `AmbientesInternosApp` anotada com
      `@SpringBootApplication(scanBasePackages = ["br.edu.ifce.ambientes_internos", "br.edu.ifce.security"])`.
    * Centralizar `application.yml`, `application-dev.yml` e `data.sql` (incluindo o *seed* idempotente do
      Administrador padrão).
    * Definir as propriedades globais de OAuth2 (Google), das chaves RSA para JWT, das configurações de cookie
      (`HttpOnly`, `Secure`, `SameSite`) e das regras de CORS para SPA/mobile.
    * Expor o pacote executável de entrada do sistema (ponto único de inicialização).

### 1.2. Módulo de Segurança (`security-module`)

* **Papel:** Centraliza o controle de acessos, autenticação de identidades e gestão de perfis.
* **Responsabilidade:** É o encarregado de interceptar as requisições HTTP recebidas, verificar as permissões
  declaradas e isolar toda a infraestrutura de identidade. Concentra, em especial:
    * Autenticação **OAuth2 (Google)** via Authorization Code com PKCE, com o backend atuando como *broker*.
    * Emissão e validação de **JWT próprio** (assinatura RSA) para acesso à API.
    * Gestão de **refresh tokens** persistidos, com rotação a cada uso, entregues em cookie `HttpOnly`, `Secure` e
      `SameSite=Strict`.
    * Entidade `Utilizador` e enum `Perfil` (`ROLE_COLABORADOR`, `ROLE_VALIDADOR`, `ROLE_GESTOR_SISTEMA`,
      `ROLE_ADMINISTRADOR`), com suporte a **múltiplos perfis cumulativos** e regra de **lockout prevention**
      (RN-4.9).
    * Subpacotes internos: `domain`, `repository`, `service`, `config`, `controller`.
    * Configuração do `SecurityFilterChain` com `oauth2Login`, `oauth2ResourceServer` (JWT) e endpoints públicos
      pré-declarados (ex.: listagens de ambientes publicados).
* Os demais módulos de domínio recorrem a este componente apenas para validar as credenciais anexadas às requisições
  e para aplicar anotações `@PreAuthorize` declarativas, mantendo seus códigos limpos de lógicas de infraestrutura
  de segurança.

### 1.3. Módulo de Domínio de Ambientes Internos (`ambientes-internos-module`)

* **Papel:** Encapsula as regras *core* e as operações de negócio do subdomínio de ambientes internos.
* **Responsabilidade:** Contém as entidades de domínio, os casos de uso e as interfaces de persistência do
  subdomínio de ambientes. Este módulo:
    * Concentra **apenas** o domínio de ambientes internos — sem conter lógica de autenticação ou autorização.
    * **Não** declara dependência de `spring-security` em seu `pom.xml`, preservando coesão e baixo acoplamento.
    * Recebe a segurança de forma transversal, por composição em `main-app` e por anotações `@PreAuthorize`
      declarativas aplicadas nos controllers, sem acoplar a este módulo a infraestrutura de identidade.
* A arquitetura multi-módulos permite que novos subdomínios (áreas externas, relatórios avançados, etc.) sejam
  acoplados como novos módulos irmãos, compartilhando o mesmo ecossistema de segurança sem impactar os recursos já
  existentes.

### 1.4. Impacto na suíte de testes com a introdução da segurança

A transição para a arquitetura modular e a introdução do `security-module` produzem efeitos diretos na estratégia
de testes do projeto.

* **Testes unitários e JPA do domínio** permanecem válidos após a separação em módulos, exigindo apenas ajustes
  mínimos de caminho/imports quando a nova estrutura assim o requerer.
* **Testes de integração de controllers** passam a considerar a segurança adicionada: chamadas hoje anônimas para
  endpoints protegidos exigirão um utilizador autenticado, simulado preferencialmente com `@WithMockUser` ou
  configuração equivalente em `MockMvc`.
* **Testes que validam erros de validação por parâmetros** continuam cobrindo `400 Bad Request`, mas apenas depois
  de satisfazer os requisitos de autenticação quando o endpoint estiver protegido.
* **Endpoints públicos** (notadamente as listagens e detalhes de ambientes publicados — ver RN-4.4) continuam
  sendo testados sem autenticação, para garantir que a regra de acesso público não se torne regressiva.
* Sempre que um teste atual passar a falhar por `401/403` após a introdução da segurança, o teste correspondente
  deve ser atualizado **no mesmo ciclo do commit** que introduz a segurança, preservando a cobertura.

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