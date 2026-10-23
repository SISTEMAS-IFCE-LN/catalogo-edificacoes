# Design system local (`components/ui`)

Estes componentes são **código do projeto** — não são um pacote externo nem arquivos gerados por CLI.

## Base

- **Comportamento e acessibilidade:** primitivas de `@base-ui/react` (`drawer`, `dialog`, `menu`, `select`, `accordion`, `tooltip`, ...).
- **Estilo:** Tailwind CSS 4 + tokens de `src/styles/ui-theme.css` e `src/styles/globals.css`.
- **Classes:** `cn()` em `src/lib/utils/cn.ts` (`clsx` + `tailwind-merge`).
- **Toasts:** `sonner` (`sonner.tsx`).

## Convenções

- Um componente por arquivo; exporte o componente e as `variants` (CVA) quando houver.
- Preserve os atributos `data-slot`: eles são o contrato usado pelos testes (`[data-slot="..."]`).
- Combine classes com `cn()` para permitir override pelo caller.
- Mantenha a semântica/ARIA das primitivas da Base UI; não reimplemente foco, teclado ou dismiss.
- Novos componentes devem reutilizar os tokens existentes — evite cores/espaçamentos literais.

## Atualizações

- `@base-ui/react` é atualizado manualmente: `npm install @base-ui/react@^X.Y.Z`.
- Antes de mesclar, rode: `npm test`, `npm run lint`, `npm run build` e `npm run audit`.
- Não há CLI nem sincronização de arquivos: correções de comportamento/acessibilidade chegam pela dependência.
- `ui-theme.css` é código interno (derivado de shadcn/ui, MIT); ajustes são feitos diretamente nele, sem espelhar upstream.
