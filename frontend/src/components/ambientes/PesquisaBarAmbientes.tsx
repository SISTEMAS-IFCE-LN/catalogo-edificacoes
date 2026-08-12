import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { useState } from 'react'
import { type Filtros, FILTROS_VAZIOS } from '@/types/ambientes/filtros'
import { TipoAmbiente, Bloco, Unidade, TipoFiltro } from '@/types/ambientes/enums'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useFiltroLocal } from '@/hooks/useFiltroLocal'

// Converte o nome do enum (ex.: "SALA_AULA") no rótulo exibido (ex.: "Sala de Aula")
function rotuloTipo(tipo: string): string {
  return TipoAmbiente[tipo as keyof typeof TipoAmbiente] ?? tipo
}

export function PesquisaBarAmbientes({
  initial,
  onChange,
}: {
  initial: Filtros
  onChange: (f: Filtros) => void
}) {
  // Detectar tipo de filtro inicial baseado nos valores iniciais
  const [tipoFiltro, setTipoFiltro] = useState<TipoFiltro>(() => {
    if (initial.nome) return TipoFiltro.NOME
    if (initial.tipo) return TipoFiltro.TIPO
    if (initial.bloco || initial.unidade || initial.andar !== null) return TipoFiltro.LOCALIZACAO
    return TipoFiltro.NENHUM
  })

  const { local, setLocal } = useFiltroLocal<Filtros>(initial, () => {
    // Re-derivar tipo de filtro quando initial muda externamente (back/forward)
    if (initial.nome) setTipoFiltro(TipoFiltro.NOME)
    else if (initial.tipo) setTipoFiltro(TipoFiltro.TIPO)
    else if (initial.bloco || initial.unidade || initial.andar !== null) setTipoFiltro(TipoFiltro.LOCALIZACAO)
    else setTipoFiltro(TipoFiltro.NENHUM)
  })

  function handleTipoFiltroChange(value: string | null) {
    setTipoFiltro((value ?? '') as TipoFiltro)
    // Limpar filtros ao trocar de tipo
    setLocal(FILTROS_VAZIOS)
  }

  function handleAndarChange(value: string) {
    const numValue = value === '' ? null : Number(value)
    if (numValue !== null && numValue < 0) return // Validação frontend
    setLocal({ ...local, andar: numValue })
  }

  function handleLimpar() {
    setLocal(FILTROS_VAZIOS)
    setTipoFiltro(TipoFiltro.NENHUM)
    onChange(FILTROS_VAZIOS)
  }

  function handleAplicar() {
    // Validar antes de aplicar
    if (tipoFiltro === TipoFiltro.NOME && !local.nome.trim()) return
    if (tipoFiltro === TipoFiltro.TIPO && !local.tipo) return
    onChange(local)
  }

  const temFiltros =
    initial.nome !== '' ||
    initial.bloco !== '' ||
    initial.unidade !== '' ||
    initial.andar !== null ||
    initial.tipo !== ''

  return (
    <div className="space-y-4">
      {/* Seletor de Tipo de Filtro */}
      <div className="max-w-xs">
        <Select value={tipoFiltro} onValueChange={handleTipoFiltroChange}>
          <SelectTrigger aria-label="Tipo de filtro">
            <SelectValue placeholder="Selecione o tipo de filtro" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={TipoFiltro.NENHUM}>Nenhum</SelectItem>
            <SelectItem value={TipoFiltro.NOME}>Nome</SelectItem>
            <SelectItem value={TipoFiltro.TIPO}>Tipo</SelectItem>
            <SelectItem value={TipoFiltro.LOCALIZACAO}>Localização</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Inputs Dinâmicos */}
      {tipoFiltro === TipoFiltro.NOME && (
        <div className="max-w-md">
          <Input
            placeholder="Nome do ambiente"
            aria-label="Filtrar por nome"
            value={local.nome}
            onChange={(e) => setLocal({ ...local, nome: e.target.value })}
            maxLength={50}
          />
        </div>
      )}

      {tipoFiltro === TipoFiltro.TIPO && (
        <div className="max-w-md">
          <Select
            value={local.tipo}
            onValueChange={(value) => setLocal({ ...local, tipo: value ?? '' })}
          >
            <SelectTrigger aria-label="Filtrar por tipo">
              <SelectValue placeholder="Selecione o tipo">
                {local.tipo ? rotuloTipo(local.tipo) : undefined}
              </SelectValue>
            </SelectTrigger>
            <SelectContent>
              {Object.entries(TipoAmbiente).map(([key, value]) => (
                <SelectItem key={key} value={key}>
                  {value}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      )}

      {tipoFiltro === TipoFiltro.LOCALIZACAO && (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <Select
            value={local.bloco}
            onValueChange={(value) => setLocal({ ...local, bloco: value ?? '' })}
          >
            <SelectTrigger aria-label="Filtrar por bloco">
              <SelectValue placeholder="Bloco" />
            </SelectTrigger>
            <SelectContent>
              {Object.entries(Bloco).map(([key, value]) => (
                <SelectItem key={key} value={value}>
                  {value}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select
            value={local.unidade}
            onValueChange={(value) => setLocal({ ...local, unidade: value ?? '' })}
          >
            <SelectTrigger aria-label="Filtrar por unidade">
              <SelectValue placeholder="Unidade" />
            </SelectTrigger>
            <SelectContent>
              {Object.entries(Unidade).map(([key, value]) => (
                <SelectItem key={key} value={value}>
                  {value}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Input
            type="number"
            placeholder="Andar"
            aria-label="Filtrar por andar"
            value={local.andar ?? ''}
            onChange={(e) => handleAndarChange(e.target.value)}
            min={0}
          />
        </div>
      )}

      {/* Botões */}
      <div className="flex gap-2">
        <Button
          variant="outline"
          onClick={handleAplicar}
          disabled={tipoFiltro === TipoFiltro.NENHUM}
        >
          Aplicar
        </Button>
        {temFiltros && (
          <Button variant="ghost" onClick={handleLimpar}>
            Limpar
          </Button>
        )}
      </div>
    </div>
  )
}
