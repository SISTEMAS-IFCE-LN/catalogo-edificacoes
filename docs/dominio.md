# Domínio do Catálogo de Edificações do IFCE

## Descrição do Domínio
O objeto de estudo é a catalogação das edificações do Instituto Federal de Educação, Ciência e Tecnologia do Ceará (IFCE), Campus de Limoeiro do Norte. O objetivo é organizar, documentar e facilitar o acesso às informações técnicas e arquitetônicas das edificações pertencentes ao IFCE.

Inicialmente, o domínio principal do sistema será dividido em dois subdomínios: Ambientes Internos e Ambientes Externos. Cada subdomínio conterá informações específicas sobre os ambientes, como características, área, localização e outros dados relevantes.

O subdomínio de Ambientes Internos abrangerá salas de aula, laboratórios, bibliotecas, auditórios, salas administrativas e outros espaços fechados utilizados para atividades acadêmicas e administrativas.

O subdomínio de Ambientes Externos incluirá áreas como pátios, jardins, ruas, estacionamentos e outras áreas abertas que compõem a infraestrutura do campus.

## Atores Envolvidos

- **Validador:** Responsável por revisar e aprovar as informações cadastradas no catálogo, garantindo a precisão e conformidade com os padrões estabelecidos. Mapeado para `ROLE_VALIDADOR` (RN-4.1).
- **Gestor do Sistema:** Responsável por gerenciar as informações do catálogo, incluindo a criação, atualização e exclusão de registros de ambientes não publicados. Mapeado para `ROLE_GESTOR_SISTEMA` (RN-4.2).
- **Colaborador:** Usuário que consulta e utiliza as informações do catálogo para suas atividades diárias, autenticado via Google OAuth 2.0 e detentor, no mínimo, do perfil `ROLE_COLABORADOR` (RN-4.3 e RN-4.6). Este perfil é cumulativo e obrigatório para todos os usuários autenticados (RN-4.8).
- **Público Externo:** Usuário que acessa o catálogo para obter informações sobre as edificações do IFCE, como empresas prestadoras de serviços, órgãos de controle e a comunidade acadêmica, sem necessidade de autenticação (RN-4.4).
- **Administrador:** Responsável pela gestão de perfis de outros usuários e pela desativação de contas. Único ator com permissão para `PATCH /api/usuarios/{id}/perfis` e `PATCH /api/usuarios/{id}/desativar`. Mapeado para `ROLE_ADMINISTRADOR` (RN-4.8 e RN-4.9). O sistema garante que sempre exista pelo menos um Administrador ativo (lockout prevention).

## Autenticação e Identidade

- O login é **exclusivo** via Google, utilizando o fluxo OAuth 2.0 — Authorization Code com PKCE, com o backend Kotlin/Spring atuando como broker (RN-4.5).
- E-mails com domínio `@ifce.edu.br` configuram o acesso **institucional** e são provisionados automaticamente com `ROLE_COLABORADOR` no primeiro login (RN-4.6).
- E-mails externos só acessam se **pré-cadastrados** por um Administrador (RN-4.7).
- Múltiplos perfis podem coexistir no mesmo usuário, mas `ROLE_COLABORADOR` é universal e cumulativo (RN-4.8).
- O `BootstrapAdminRunner` provisiona um administrador institucional conhecido no boot, lendo a env var `BOOTSTRAP_ADMIN_EMAIL` (ver [`docs/operacao.md`](./operacao.md)).
