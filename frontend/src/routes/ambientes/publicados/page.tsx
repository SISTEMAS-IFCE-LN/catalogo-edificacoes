import { useQuery } from '@tanstack/react-query'
import { useEffect, useMemo, useState } from 'react'
import { fetchAmbientes } from '@/lib/api/api-ambientes'
import type { AmbientesQuery } from '@/types/ambientes/ambiente'
import { PesquisaBar } from '@/components/ambientes/PesquisaBar'
import { TabelaPadrao } from '@/components/ambientes/TabelaPadrao'
import { AcoesLote } from '@/components/ambientes/AcoesLote'
import { Button } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { toast } from 'sonner'
import { useAmbientesSearchParams } from '@/hooks/useAmbientesSearchParams'
import { useAuth } from '@/hooks/useAuth'

const TAMANHOS_PAGINA = [10, 20, 50, 100]

export function PublicadosPage() {
  const { user } = useAuth()
  const autenticado = user !== null

  const {
    page,
    size,
    filtros,
    filtrosLocal,
    handleFiltrosChange,
    handlePageChange,
    handleSizeChange,
    tipoFiltro,
  } = useAmbientesSearchParams()

  const query: AmbientesQuery = {
    page,
    size,
    nome: filtros.nome || undefined,
    bloco: filtros.bloco || undefined,
    unidade: filtros.unidade || undefined,
    andar: filtros.andar ?? undefined,
    tipo: filtros.tipo || undefined,
    tipoFiltro,
  }

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['ambientes', 'publicados', query],
    queryFn: ({ signal }) => fetchAmbientes(query, signal),
  })

  // Seleção múltipla (só para usuários autenticados — UC20-FE).
  // Seleção é por página visível: trocar de página/filtros limpa a seleção
  // nos próprios handlers que disparam a mudança (sem useEffect/refs).
  const [selectedIds, setSelectedIds] = useState<number[]>([])

  function limparSelecao() {
    setSelectedIds([])
  }

  function handlePageChangeComLimpeza(newPage: number) {
    limparSelecao()
    handlePageChange(newPage)
  }

  function handleSizeChangeComLimpeza(newSize: string | null) {
    limparSelecao()
    handleSizeChange(newSize)
  }

  function handleFiltrosChangeComLimpeza(novosFiltros: typeof filtros) {
    limparSelecao()
    handleFiltrosChange(novosFiltros)
  }

  const idsDaPagina = useMemo(
    () => (data ? data.ambientes.map((a) => a.id) : []),
    [data],
  )

  const allSelected = autenticado && idsDaPagina.length > 0 && idsDaPagina.every((id) => selectedIds.includes(id))
  const someSelected = autenticado && selectedIds.length > 0 && !allSelected

  function toggleSelect(id: number) {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
    )
  }

  function toggleSelectAll() {
    if (allSelected) {
      setSelectedIds([])
    } else {
      setSelectedIds(idsDaPagina)
    }
  }

  useEffect(() => {
    if (error) {
      toast.error('Erro ao carregar ambientes. Tente novamente.')
    }
  }, [error])

  if (error) {
    return (
      <div className="space-y-4">
        <h1 className="text-2xl font-bold">Ambientes Publicados</h1>
        <div role="alert" className="border border-destructive rounded-lg p-4">
          <p className="text-destructive font-medium">Erro ao carregar ambientes.</p>
          <Button variant="outline" onClick={() => refetch()} className="mt-2">
            Tentar novamente
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Ambientes Publicados</h1>
      <PesquisaBar initial={filtrosLocal} onChange={handleFiltrosChangeComLimpeza} />
      {isLoading ? (
        <p>Carregando…</p>
      ) : data && data.ambientes.length > 0 ? (
        <>
          {autenticado && <AcoesLote selectedIds={selectedIds} onClear={limparSelecao} />}
          <TabelaPadrao
            itens={data.ambientes}
            selectedIds={autenticado ? selectedIds : undefined}
            onToggleSelect={autenticado ? toggleSelect : undefined}
            onToggleSelectAll={autenticado ? toggleSelectAll : undefined}
            allSelected={autenticado ? allSelected : undefined}
            someSelected={autenticado ? someSelected : undefined}
          />
          <div className="flex items-center justify-between gap-4">
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted-foreground">Itens por página:</span>
              <Select value={String(size)} onValueChange={handleSizeChangeComLimpeza}>
                <SelectTrigger className="w-[70px]" aria-label="Itens por página">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {TAMANHOS_PAGINA.map((t) => (
                    <SelectItem key={t} value={String(t)}>
                      {t}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={!data.dadosPaginacao.hasPrevious}
                onClick={() => handlePageChangeComLimpeza(page - 1)}
              >
                Anterior
              </Button>
              <span className="text-sm flex items-center">
                Página {data.dadosPaginacao.currentPage + 1} de {data.dadosPaginacao.totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={!data.dadosPaginacao.hasNext}
                onClick={() => handlePageChangeComLimpeza(page + 1)}
              >
                Próximo
              </Button>
            </div>
          </div>
        </>
      ) : (
        <p className="text-muted-foreground">Nenhum ambiente encontrado.</p>
      )}
    </div>
  )
}