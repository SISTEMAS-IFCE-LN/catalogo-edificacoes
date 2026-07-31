import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { useState } from 'react'
import { type Filtros, FILTROS_VAZIOS } from '@/types/ambiente'

export function PesquisaBar({
  initial,
  onChange,
}: {
  initial: Filtros
  onChange: (f: Filtros) => void
}) {
  const [local, setLocal] = useState<Filtros>(initial)
  const [lastInitial, setLastInitial] = useState<Filtros>(initial)

  // Sincronizar local quando initial mudar externamente (back/forward)
  // Padrão React: "adjusting state during rendering"
  if (
    lastInitial.nome !== initial.nome ||
    lastInitial.bloco !== initial.bloco ||
    lastInitial.unidade !== initial.unidade ||
    lastInitial.andar !== initial.andar ||
    lastInitial.tipo !== initial.tipo
  ) {
    setLastInitial(initial)
    setLocal(initial)
  }

  function handleAndarChange(value: string) {
    setLocal({ ...local, andar: value === '' ? null : Number(value) })
  }

  function handleLimpar() {
    setLocal(FILTROS_VAZIOS)
    onChange(FILTROS_VAZIOS)
  }

  const temFiltros =
    initial.nome !== '' ||
    initial.bloco !== '' ||
    initial.unidade !== '' ||
    initial.andar !== null ||
    initial.tipo !== ''

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3">
      <Input
        placeholder="Nome"
        aria-label="Filtrar por nome"
        value={local.nome}
        onChange={(e) => setLocal({ ...local, nome: e.target.value })}
      />
      <Input
        placeholder="Bloco"
        aria-label="Filtrar por bloco"
        value={local.bloco}
        onChange={(e) => setLocal({ ...local, bloco: e.target.value })}
      />
      <Input
        placeholder="Unidade"
        aria-label="Filtrar por unidade"
        value={local.unidade}
        onChange={(e) => setLocal({ ...local, unidade: e.target.value })}
      />
      <Input
        type="number"
        placeholder="Andar"
        aria-label="Filtrar por andar"
        value={local.andar ?? ''}
        onChange={(e) => handleAndarChange(e.target.value)}
      />
      <Input
        placeholder="Tipo"
        aria-label="Filtrar por tipo"
        value={local.tipo}
        onChange={(e) => setLocal({ ...local, tipo: e.target.value })}
      />
      <Button variant="outline" onClick={() => onChange(local)}>
        Aplicar
      </Button>
      {temFiltros && (
        <Button variant="ghost" onClick={handleLimpar}>
          Limpar
        </Button>
      )}
    </div>
  )
}