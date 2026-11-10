# Migrations de Banco de Dados

O schema do Catálogo de Edificações é versionado com **Flyway**. As migrations ficam em `db/migration/` e são aplicadas por um job dedicado (`migrate`) antes da API. A aplicação não contém Flyway e sobe com `spring.jpa.hibernate.ddl-auto: validate`.

Em desenvolvimento e testes, o H2 é criado com `create-drop`; as migrations são validadas contra PostgreSQL 17.

---

## 1. Visão geral

```
db (healthy)
  └─> backup (one-shot, pg_dump)
        └─> migrate (one-shot, Flyway CLI)
              └─> api (ddl-auto: validate)
                    └─> frontend
```

| Serviço    | Responsabilidade                                                                                              |
|------------|---------------------------------------------------------------------------------------------------------------|
| `db`       | PostgreSQL 17; healthcheck; volume nomeado `catalogo-data`.                                                   |
| `backup`   | Executa `pg_dump` com a imagem `postgres:17-trixie` para `db/backup/`; não reinicia; roda antes da migration. |
| `migrate`  | Executa `flyway migrate` com a imagem oficial do Flyway; aplica as migrations em `db/migration/`.             |
| `api`      | Aplicação Kotlin/Spring; não contém Flyway; apenas valida o schema.                                           |
| `frontend` | Nginx + SPA; depende da API saudável.                                                                         |

As migrations usam SQL versionado e são imutáveis. O schema é propriedade das migrations; o Hibernate apenas verifica se as entidades continuam compatíveis.

---

## 2. Princípios e regras

- **Fonte de verdade:** o schema é definido pelas migrations em `db/migration/`. Não há DDL manual em produção.
- **Imutabilidade:** uma migration já aplicada não é editada. Correções são feitas por uma nova migration (forward-fix).
- **Sem `clean`:** `flyway clean` não é executado em produção. O comando apaga objetos do schema.
- **Compatibilidade:** mudanças de schema devem ser compatíveis com a versão anterior da API durante o deploy. Mudanças destrutivas seguem expand/contract.
- **Backup antes da migration:** o serviço `backup` roda antes do `migrate` e grava um dump lógico.
- **Revisão:** toda migration passa por revisão de código e checklist; mudanças destrutivas exigem atenção redobrada.
- **Dev/test:** o H2 usa `create-drop` e não executa as migrations. A validação de SQL é feita em PostgreSQL 17.
- **Uma mudança por migration:** evite misturar DDL, backfill e alterações não relacionadas no mesmo arquivo.

---

## 3. Estrutura e convenções

```
db/
├── migration/
│   └── V1__baseline.sql
├── backup/
│   ├── rollover/     # 5 backups mais recentes de deploy
│   ├── daily/        # uma cópia por dia, fora da rotação
│   └── weekly/       # uma cópia por semana ISO, fora da rotação
└── backup.sh         # script executado pelo serviço backup
```

### 3.1. Nomes e conteúdo

- Formato: `V<versão>__<descricao>.sql`, por exemplo `V2__adiciona_acessivel_ambiente.sql`.
- Versões sequenciais (`V1`, `V2`, `V3`...) ou por timestamp; a convenção deve ser mantida no repositório.
- Codificação UTF-8 e line endings LF.
- Comentários no topo explicando o objetivo e o impacto esperado.
- Sem `BEGIN`/`COMMIT` explícitos: o Flyway controla a transação.
- Sem comandos específicos de outro banco; o alvo é PostgreSQL 17.
- Não incluir seeds de desenvolvimento. O bootstrap do admin é feito pelo `BootstrapAdminRunner`.
- Dados de referência necessários em produção entram como migration própria, idempotente e revisada.

### 3.2. Imagem do Flyway

O serviço `migrate` usa a imagem oficial:

```yaml
migrate:
  image: flyway/flyway:<tag-fixa>
  command: ["migrate"]
  volumes:
    - ./db/migration:/flyway/sql:ro
```

- A tag é fixada no `docker-compose.yml`; não usar `latest`.
- A imagem oficial inclui o driver PostgreSQL.
- O bind é read-only (`:ro`): o container não altera os scripts.
- O caminho `./db/migration` é relativo ao `docker-compose.yml`.
- Em produção, o deploy deve partir de um checkout limpo da tag/release, pois as migrations não estão embutidas na imagem da API.

### 3.3. Variáveis do job

| Variável                     | Uso                                                                                                        |
|------------------------------|------------------------------------------------------------------------------------------------------------|
| `FLYWAY_URL`                 | JDBC do PostgreSQL (`jdbc:postgresql://db:5432/<DB_NAME>` no Compose; pode apontar para um banco externo). |
| `FLYWAY_USER`                | Usuário de banco; é o mesmo `DB_USERNAME` da API.                                                          |
| `FLYWAY_PASSWORD`            | Senha do usuário; é a mesma `DB_PASSWORD` da API.                                                          |
| `FLYWAY_LOCATIONS`           | `filesystem:/flyway/sql`.                                                                                  |
| `FLYWAY_CLEAN_DISABLED`      | `true`.                                                                                                    |
| `FLYWAY_BASELINE_ON_MIGRATE` | `false` (o banco de produção é criado do zero).                                                            |
| `FLYWAY_OUT_OF_ORDER`        | `false`.                                                                                                   |
| `FLYWAY_CONNECT_RETRIES`     | Opcional; útil quando o banco ainda está iniciando.                                                        |

O serviço `backup` usa as variáveis do PostgreSQL (`PGPASSWORD`, `DB_NAME`, `DB_USERNAME`) para executar o `pg_dump`.

### 3.4. Transações

- Por padrão, cada migration roda em uma transação. No PostgreSQL, o DDL é transacional: uma falha desfaz a migration.
- Comandos que não podem rodar em transação — por exemplo, `CREATE INDEX CONCURRENTLY` — precisam ser configurados como não transacionais (`executeInTransaction=false`) em um arquivo de configuração da migration.
- Migrations não transacionais exigem limpeza manual em caso de falha; não há rollback automático.

---

## 4. Adicionar uma migration (forward-fix)

1. Crie o próximo arquivo em `db/migration/`, por exemplo `V2__adiciona_acessivel_ambiente.sql`.
2. Escreva SQL compatível com PostgreSQL 17. Prefira mudanças aditivas.
3. Se a mudança for destrutiva, aplique expand/contract em releases separados (ver seção 7).
4. Teste em um PostgreSQL 17 limpo:
   - aplique todas as migrations;
   - suba a API com `ddl-auto: validate`;
   - execute o smoke test.
5. Atualize entidades/repositórios se necessário, mantendo a versão anterior da API compatível.
6. Revise o checklist abaixo e abra o PR.
7. Faça o deploy pelo comando padrão (seção 8).

### 4.1. Checklist de PR

- [ ] A migration tem nome/versão corretos e é a próxima da sequência.
- [ ] O arquivo é imutável após o merge.
- [ ] A mudança é aditiva ou está acompanhada de plano expand/contract.
- [ ] Não há `DROP`, `TRUNCATE`, `DELETE` em massa ou alteração de tipo sem análise de impacto.
- [ ] Índices/constraints em tabelas grandes usam `CONCURRENTLY`, `NOT VALID`/`VALIDATE CONSTRAINT` ou backfill em lotes.
- [ ] A versão anterior da API continua funcionando durante o deploy.
- [ ] A migration foi testada em PostgreSQL 17 limpo.
- [ ] A API sobe com `ddl-auto: validate`.
- [ ] Há plano de backup/restore para mudanças destrutivas.
- [ ] A documentação relevante foi atualizada.

### 4.2. Falha e correção

- Consulte os logs:

  ```bash
  docker compose logs migrate
  ```

- Consulte o estado das migrations:

  ```bash
  docker compose run --rm --no-deps migrate info
  ```

- Em falha transacional, o banco permanece na versão anterior. Corrija com uma nova migration (forward-fix).
- Se houver registro `success=false` na `flyway_schema_history`, limpe os objetos parciais (quando aplicável), rode `repair` e tente novamente:

  ```bash
  docker compose run --rm --no-deps migrate repair
  ```

- Nunca edite a migration que falhou. O checksum é validado.
- Nunca use `clean` em produção.

---

## 5. Primeiro deploy (banco greenfield)

O banco de produção é criado do zero; não há `baseline-on-migrate`.

### 5.1. Gerar o `V1`

1. Suba um PostgreSQL 17 descartável.
2. Rode a aplicação com `ddl-auto: create` contra esse banco.
3. Capture o schema:

   ```bash
   pg_dump --schema-only --no-owner --no-privileges \
     -h <host> -U <usuario> -d <banco> > V1__baseline.sql
   ```

4. Revise o dump: uniques, CHECKs, defaults, sequences, element collections e a herança `SINGLE_TABLE`.
5. Coloque o arquivo em `db/migration/V1__baseline.sql`.
6. Teste em um banco vazio: aplique `V1`, suba a API com `validate` e rode o smoke test.

### 5.2. Executar o deploy

1. Preencha o `.env` com as variáveis de produção.
2. Garanta que `db/backup/` existe (crie se necessário) e tem permissão de escrita.
3. Execute:

   ```bash
   docker compose rm -f
   docker compose up -d --build
   ```

4. Verifique:
   - `docker compose ps` — `backup` e `migrate` com exit 0;
   - `docker compose logs migrate` — `V1` aplicada;
   - `docker compose logs api` — aplicação saudável;
   - `flyway_schema_history` — `V1` com `success=true`;
   - bootstrap do admin e login via Google;
   - smoke test.

O `backup` roda antes do `migrate`; em um banco vazio, ele gera um dump vazio válido. O `V1` é aplicado na sequência.

Se o `V1` falhar, o banco ainda está vazio: corrija o arquivo, recrie o banco e repita. Não há dados a preservar nesse primeiro momento.

---

## 6. Backup e restore

### 6.1. Política

O backup é lógico (`pg_dump -Fc`) e fica em `db/backup/`:

| Diretório   | Conteúdo                   | Retenção        |
|-------------|----------------------------|-----------------|
| `rollover/` | Backups recentes de deploy | 5 mais recentes |
| `daily/`    | Uma cópia por dia          | 7 mais recentes |
| `weekly/`   | Uma cópia por semana ISO   | 4 mais recentes |

O serviço `backup` roda antes do `migrate`; no Compose, o `migrate` depende de `backup` com `condition: service_completed_successfully`. A rotação só ocorre depois que o `pg_dump` termina com sucesso; um dump parcial não é contado.

O script `db/backup.sh`:

1. grava em arquivo temporário;
2. renomeia para `.dump` somente em caso de sucesso;
3. cópia para `daily/` e `weekly/` se ainda não existir a cópia do dia/semana;
4. aplica a retenção em `rollover/`, `daily/` e `weekly/`.

Mantenha `db/backup/` no `.gitignore`; os dumps não são versionados.

### 6.2. Limitações

- A pasta fica no mesmo host/disco do banco. Se o host for perdido, banco e backup são perdidos juntos.
- Não é offsite nem imutável.
- Não há PITR (point-in-time recovery).
- As cópias `daily/`/`weekly/` dependem da execução do backup; se não houver deploy, agende `docker compose run --rm backup` no host (cron/Task Scheduler) para garantir a cópia diária.
- Evite `docker compose down -v`: ele remove o volume `catalogo-data` e apaga o banco. `docker compose rm -f` não remove volumes.

A evolução prevista é copiar `daily/`/`weekly/` para storage externo e, se necessário, adotar PITR.

### 6.3. Restore

1. Escolha o dump adequado:
   - `rollover/` para o ponto imediatamente anterior a um deploy;
   - `daily/`/`weekly/` para pontos mais antigos.
2. Prefira testar o restore em um banco/container descartável:

   ```bash
   docker run --rm -e POSTGRES_PASSWORD=... -p 5433:5432 postgres:17-trixie
   pg_restore -h localhost -p 5433 -U <usuario> -d <banco> --clean --if-exists caminho.dump
   ```

3. Valide o schema e rode o smoke test.
4. Em produção: pare a API, restaure o dump, suba a versão correspondente da API e valide.
5. Registre o dump usado, o horário e o resultado.

Defina RPO/RTO aceitáveis. Enquanto o backup for local, o RPO é o intervalo entre execuções e o RTO depende do tempo de restore.

---

## 7. Expand/contract

Mudanças incompatíveis são feitas em fases, com releases separados.

### 7.1. Fases

1. **Expand:** adicione a estrutura nova (coluna nullable, tabela, índice) sem remover a antiga. O código passa a escrever nos dois lugares.
2. **Migrate:** faça backfill em lotes e mude o código para ler/gravar na estrutura nova. Mantenha a antiga para compatibilidade.
3. **Contract:** depois que todas as instâncias estiverem na versão nova e não houver rollback previsto, remova a estrutura antiga. Faça backup antes.

### 7.2. Exemplos

- **Nova coluna obrigatória:** adicione `ambiente.acessivel` nullable ou com default; o código novo grava o valor; backfill; em um release posterior, aplique `SET NOT NULL`.
- **Renomear `usuarios.nome` para `nome_completo`:** adicione a coluna nova; o código escreve nas duas; backfill; mude a leitura; só remova `nome` depois de um release sem rollback.
- **Nova unique constraint** (por exemplo, `uk_ambiente_nome_localizacao`): limpe duplicatas com migration de dados; crie índice único `CONCURRENTLY`; anexe a constraint com `UNIQUE USING INDEX`.
- **Novo valor de enum/discriminador** (`TipoAmbiente`, `StatusAmbiente`): se houver CHECK constraint, recrie-a incluindo o novo valor; o código deve aceitar valores antigos e novos durante a transição.
- **Mudança de precisão** em `geometria.base`/`altura`: para tabelas grandes, crie coluna nova com a precisão desejada, faça backfill, troque o código e remova a antiga; evite `ALTER TYPE` com lock longo.

### 7.3. Cuidados no PostgreSQL

- `CREATE INDEX CONCURRENTLY` não roda em transação; configure a migration como não transacional.
- `ALTER TABLE ... ADD CONSTRAINT ... NOT VALID` + `VALIDATE CONSTRAINT` reduz o lock.
- Backfill em lotes para não manter transações longas.
- Monitore locks e tempo de execução em tabelas grandes.

---

## 8. Deploy e runbook

### 8.1. Comando padrão

```bash
docker compose rm -f
docker compose up -d --build
```

O que acontece:

1. `rm -f` remove containers **parados** de todos os serviços. `db`, `api` e `frontend`, se estiverem rodando, continuam no ar.
2. `up -d --build` recria `backup` e `migrate`; eles executam na ordem `db → backup → migrate`.
3. Se a imagem/config da API mudou, ela é recriada e aguarda o `migrate` concluir.
4. Se a API não mudou, ela continua no ar durante a migration. Isso exige migrations backward-compatible.
5. O `frontend` sobe após a API ficar saudável.

Em PowerShell 5.1, `&&` não funciona:

```powershell
docker compose rm -f
if ($?) { docker compose up -d --build }
```

### 8.2. Verificação

```bash
docker compose ps
docker compose logs --tail=100 backup
docker compose logs --tail=100 migrate
docker compose logs --tail=100 api
```

Histórico de migrations (com as variáveis do `.env` carregadas no shell):

```bash
docker compose exec db psql -U "$DB_USERNAME" -d "$DB_NAME" \
  -c "select installed_rank, version, description, success from flyway_schema_history order by installed_rank desc;"
```

Depois: `GET /actuator/health` e smoke test (`docs/run.md §3.6`).

### 8.3. Falhas

| Cenário                    | Detecção                                  | Ação                                                                                                                                                                     |
|----------------------------|-------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `backup` falhou            | `docker compose ps`/logs                  | Corrigir disco/permissão/conexão; o `migrate` e a API nova não sobem; reexecutar.                                                                                        |
| `migrate` falhou           | logs e `flyway_schema_history`            | Corrigir com forward-fix; se houver `success=false`, limpar objetos parciais e rodar `repair`; a API nova não sobe; a antiga, se estiver rodando, permanece; reexecutar. |
| API falhou no `validate`   | logs da API                               | Comparar entidades e schema; corrigir migration ou mapeamento; reexecutar.                                                                                               |
| Migration não foi aplicada | `flyway_schema_history` sem a nova versão | Verificar logs; rodar `docker compose run --rm --no-deps migrate`; confirmar.                                                                                            |
| Dados corrompidos          | validação/smoke test                      | Parar a API, restaurar o dump escolhido, reconciliar escritas e reexecutar.                                                                                              |

Se o último `migrate` falhou, inspecione `docker compose logs migrate` **antes** de rodar `rm -f`; o comando remove o container e seus logs. O histórico de falha permanece no banco.

### 8.4. Rollback

- **Código:** redeploy da tag anterior. Só funciona se o schema for compatível com a versão antiga.
- **Schema:** forward-fix. O Flyway OSS não tem `undo`.
- **Dados:** restore do dump (`rollover/`, `daily/` ou `weekly/`). Há perda das escritas posteriores ao backup (RPO).

Mudanças destrutivas/incompatíveis exigem janela de manutenção:

```bash
docker compose rm -sf
docker compose up -d --build
```

Ou `docker compose down && docker compose up -d --build`. Ambos param a stack; `down -v` também apaga o volume e **não** deve ser usado em produção.

`--remove-orphans` limpa containers de serviços removidos/renomeados; não força a reexecução de `backup`/`migrate`.

Para ordem estrita ou diagnóstico, o comando alternativo é:

```bash
docker compose run --rm backup
docker compose run --rm --no-deps migrate
docker compose up -d --build api frontend
```

---

## 9. Troubleshooting

| Sintoma                                       | Causa provável                            | Solução                                                                                                      |
|-----------------------------------------------|-------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `FlywayValidateException` / checksum mismatch | Migration aplicada foi editada            | Restaurar o conteúdo original; nunca editar migration aplicada. Se necessário, avaliar `repair` com cuidado. |
| `success=false` na `flyway_schema_history`    | Migration falhou                          | Ler logs; limpar objetos parciais; `docker compose run --rm --no-deps migrate repair`; reexecutar.           |
| `FlywayLockException`                         | Outra execução em andamento ou lock preso | Verificar processos concorrentes; aguardar; se necessário, liberar o lock com segurança.                     |
| Objetos parciais após falha                   | Migration não transacional                | Limpar manualmente os objetos; corrigir a migration; reexecutar.                                             |
| API falha com `Schema-validation`             | Entidades divergem do schema              | Ajustar migration/entidades; validar em PostgreSQL 17.                                                       |
| `backup` falha com permissão                  | `db/backup/` sem escrita para o container | Ajustar permissões do diretório; verificar usuário do container.                                             |
| `migrate` não reexecutou                      | Container one-shot já concluído           | Usar `rm -f` antes do `up` ou `docker compose run --rm --no-deps migrate`.                                   |
| Volume apagado                                | `docker compose down -v`                  | Restaurar o último dump; recriar o volume.                                                                   |

---

## 10. Usuário de banco

A API, o Flyway e o backup usam o mesmo usuário (`catalogo`). Isso simplifica a configuração, mas significa que a API tem privilégios de DDL.

A separação futura recomendada é:

- `catalogo_migrator`: owner do schema/tabelas, usado pelo Flyway.
- `catalogo_app`: `CONNECT`, `USAGE` no schema, `SELECT/INSERT/UPDATE/DELETE` nas tabelas e `USAGE/SELECT` nas sequences.
- `ALTER DEFAULT PRIVILEGES` para que novas tabelas criadas pelo migrator já concedam os privilégios ao app.
- O backup precisa de leitura (`pg_read_all_data` ou grants equivalentes).

A mudança exige script de inicialização de roles e ajuste das variáveis `FLYWAY_USER`/`DB_USERNAME`. Em um banco já existente, os roles e grants devem ser criados manualmente.

---

## 11. Referências

- Flyway — CLI, Docker, `migrate`, `info`, `validate`, `repair`, transações, baselines.
- Spring Boot 4.1 — Database Initialization.
- PostgreSQL — `pg_dump`, `pg_restore`, backup e PITR.
- [`docs/operacao.md`](./operacao.md) — profiles, bootstrap, Docker Compose e troubleshooting.
- [`docs/run.md`](./run.md) — execução local, smoke test e workflow de migrations.
- [`docs/arquitetura-backend.md`](./arquitetura-backend.md) — módulos e camadas.
- [`docs/seguranca.md`](./seguranca.md) — bootstrap do administrador.
