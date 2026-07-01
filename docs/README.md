# Documentação do Catálogo de Edificações

Este diretório concentra toda a documentação técnica e de negócio do projeto Catálogo de Edificações do IFCE — Campus Limoeiro do Norte.

## Índice

| Documento | Descrição |
|---|---|
| [Arquitetura](./arquitetura.md) | Visão geral da arquitetura modular (multi-módulos Maven) e padrão interno de camadas. |
| [Domínio](./dominio.md) | Conceitos de negócio, subdomínios e atores do sistema. |
| [Segurança](./seguranca.md) | Fluxos de autenticação OAuth2 + JWT, perfis cumulativos, lockout prevention, mapeamento de endpoints, env vars de segurança. |
| [Operação](./operacao.md) | Perfis, comportamento de `data.sql`/`data-dev.sql`, geração de chaves RSA, procedimento de bootstrap em produção. |
| [Execução](./run.md) | Comandos para rodar a aplicação e os testes localmente, smoke test via Postman. |
| [Ambientes Internos](./ambientes-internos/regras-negocio.md) | Regras de negócio (RN-1.x, RN-2.x, RN-3.x, RN-4.x) do subdomínio de ambientes internos. |
| [Casos de Uso — Backend](./ambientes-internos/casos-uso-backend.md) | Especificação dos casos de uso do backend (referência histórica; revisar antes de alterar fluxos). |
| [Casos de Uso — Frontend](./ambientes-internos/casos-uso-frontend.md) | Especificação dos casos de uso do frontend. |

## Estado atual da implementação

- **Módulos Maven:** `main-app` (composition root), `security-module` (identidade e autorização), `ambientes-internos-module` (domínio de ambientes), `common-module` (utilitários compartilhados).
- **Autenticação:** OAuth2 Authorization Code com PKCE + Google como provedor; backend como broker; JWT próprio (RSA) para acesso à API; refresh token em cookie HttpOnly com rotação.
- **Perfis:** `ROLE_COLABORADOR` (mínimo obrigatório), `ROLE_VALIDADOR`, `ROLE_GESTOR_SISTEMA`, `ROLE_ADMINISTRADOR` — cumulativos.
- **Bootstrap do admin:** via `BOOTSTRAP_ADMIN_EMAIL` no `BootstrapAdminRunner`; aborta o boot se a env var não estiver configurada.

## Convenções de nomenclatura adotadas

| Conceito | Nome usado | Observação |
|---|---|---|
| Usuário do sistema | `Usuario` (classe), `usuarios` (tabela) | Singular consistente em código e SQL. |
| Ator de negócio "Servidor" | Renomeado para **Gestor do Sistema** | Alinhado com `ROLE_GESTOR_SISTEMA`. |
| Ator de negócio "Administrador" | `Administrador` (em `dominio.md` e RN-4.x) | Mapeado para `ROLE_ADMINISTRADOR`. |
