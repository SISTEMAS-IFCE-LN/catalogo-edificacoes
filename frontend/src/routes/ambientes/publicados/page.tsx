import { useQuery } from '@tanstack/react-query'
import { useEffect } from 'react'
import { fetchPublicados, type PublicadosQuery } from '@/lib/api/api-ambientes'
import { PesquisaBar } from '@/components/ambientes/PesquisaBar'
import { TabelaPadrao } from '@/components/ambientes/TabelaPadrao'
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

const TAMANHOS_PAGINA = [10, 20, 50, 100]

export function PublicadosPage() {
  const {
    page,
    size,
    filtros,
    filtrosLocal,
    handleFiltrosChange,
    handlePageChange,
    handleSizeChange,
  } = useAmbientesSearchParams()

  const query: PublicadosQuery = {
    page,
    size,
    nome: filtros.nome || undefined,
    bloco: filtros.bloco || undefined,
    unidade: filtros.unidade || undefined,
    andar: filtros.andar ?? undefined,
    tipo: filtros.tipo || undefined,
  }

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['ambientes', 'publicados', query],
    queryFn: ({ signal }) => fetchPublicados(query, signal),
  })

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
      <PesquisaBar initial={filtrosLocal} onChange={handleFiltrosChange} />
      {isLoading ? (
        <p>Carregando…</p>
      ) : data && data.ambientes.length > 0 ? (
        <>
          <TabelaPadrao itens={data.ambientes} />
          <div className="flex items-center justify-between gap-4">
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted-foreground">Itens por página:</span>
              <Select value={String(size)} onValueChange={handleSizeChange}>
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
                onClick={() => handlePageChange(page - 1)}
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
                onClick={() => handlePageChange(page + 1)}
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