import { useState } from 'react'

/**
 * Compara valores planos (string, número, null ou objetos de primitivas)
 * por valor — evita resetar o rascunho quando a URL muda apenas em `page`
 * (o objeto `initial` ganha nova referência, mas os filtros têm os mesmos valores).
 */
function valoresIguais<T>(a: T, b: T): boolean {
  return JSON.stringify(a) === JSON.stringify(b)
}

/**
 * Rascunho local de filtro sincronizado com um valor externo (a URL).
 *
 * O usuário edita `local` sem disparar consultas; o valor só é "comprometido"
 * quando o componente chama `onChange`. Mudanças externas em `initial`
 * (back/forward, link compartilhado, handlers de paginação) resetam o rascunho
 * via padrão React "adjusting state during rendering" — sem flicker.
 *
 * `aoSincronizar` (opcional) roda quando `initial` muda de fato, para lógica
 * derivada do componente (ex.: re-derivar o tipo de filtro no PesquisaBarAmbientes).
 */
export function useFiltroLocal<T>(initial: T, aoSincronizar?: () => void) {
  const [local, setLocal] = useState<T>(initial)
  const [lastInitial, setLastInitial] = useState<T>(initial)

  if (!valoresIguais(lastInitial, initial)) {
    setLastInitial(initial)
    setLocal(initial)
    aoSincronizar?.()
  }

  return { local, setLocal }
}
