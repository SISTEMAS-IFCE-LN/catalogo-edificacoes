import { useQuery } from '@tanstack/react-query'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router'
import { fetchValidacao } from '@/lib/api/api-validacao'
import type { AmbientesQuery } from '@/types/ambientes/ambiente'
import { PesquisaBarAmbientes } from '@/components/ambientes/PesquisaBarAmbientes'
import { TabelaPadrao } from '@/components/ambientes/TabelaPadrao'
import { AcoesLote, type AcaoLote } from '@/components/ambientes/AcoesLote'
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
import { ROUTES } from '@/constants/routes'

const TAMANHOS_PAGINA = [10, 20, 50, 100]

export function ValidacaoPage() {
  const navigate = useNavigate()

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
    queryKey: ['ambientes', 'validacao', query],
    queryFn: ({ signal }) => fetchValidacao(query, signal),
  })

  // Seleção múltipla (UC01-FE). A rota é RequireRole VALIDADOR → sempre autenticado,
  // então não há gate por useAuth como no PublicadosPage.
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

  const allSelected = idsDaPagina.length > 0 && idsDaPagina.every((id) => selectedIds.includes(id))
  const someSelected = selectedIds.length > 0 && !allSelected

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

  // Ação em lote da validação (análoga ao UC20-FE). A rota de destino é a
  // EsquadriasPage compartilhada (parte 10 §8).
  const acoesValidacao: AcaoLote[] = [
    {
      value: 'Detalhar Esquadrias',
      onRun: (ids) => navigate(`${ROUTES.VALIDACAO}/esquadrias?ids=${ids.join(',')}`),
    },
  ]

  useEffect(() => {
    if (error) {
      toast.error('Erro ao carregar ambientes em validação. Tente novamente.')
    }
  }, [error])

  if (error) {
    return (
      <div className="space-y-4">
        <h1 className="text-2xl font-bold">Aguardando Validação</h1>
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
      <h1 className="text-2xl font-bold">Aguardando Validação</h1>
      <PesquisaBarAmbientes initial={filtrosLocal} onChange={handleFiltrosChangeComLimpeza} />
      {isLoading ? (
        <p>Carregando…</p>
      ) : data && data.ambientes.length > 0 ? (
        <>
          <AcoesLote selectedIds={selectedIds} onClear={limparSelecao} acoes={acoesValidacao} />
          <TabelaPadrao
            itens={data.ambientes}
            detalheBasePath={ROUTES.VALIDACAO}
            selectedIds={selectedIds}
            onToggleSelect={toggleSelect}
            onToggleSelectAll={toggleSelectAll}
            allSelected={allSelected}
            someSelected={someSelected}
          />
          <div className="flex items-center justify-between gap-4">
            <span className="text-sm text-muted-foreground">
              Área Total: {data.areaTotal.toFixed(2)} m²
            </span>
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
        <p className="text-muted-foreground">Nenhum ambiente aguardando validação.</p>
      )}
    </div>
  )
}
