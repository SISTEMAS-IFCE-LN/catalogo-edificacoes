import { useSearchParams } from 'react-router'
import { useQuery } from '@tanstack/react-query'
import { fetchEsquadrias } from '@/lib/api/api-ambientes'
import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { toast } from 'sonner'

export function EsquadriasPage() {
  const [searchParams] = useSearchParams()
  const ids = searchParams.get('ids')?.split(',').map(Number).filter(Boolean) ?? []

  const [page, setPage] = useState(0)

  const { data, isLoading, error } = useQuery({
    queryKey: ['esquadrias', ids, page],
    queryFn: () => fetchEsquadrias({
      ids,
      page,
      size: 100,
    }),
    enabled: ids.length > 0,
  })

  useEffect(() => {
    if (error) toast.error('Erro ao carregar esquadrias.')
  }, [error])

  if (ids.length === 0) {
    return <p>Nenhum ambiente selecionado.</p>
  }

  if (isLoading) return <p>Carregando…</p>

  if (error) {
    return <p>Erro ao carregar dados.</p>
  }

  if (!data || data.ambientes.length === 0) {
    return <p>Nenhuma esquadria encontrada.</p>
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Detalhes de Esquadrias</h1>

      {/* Lista por ambiente */}
      {data.ambientes.map((amb) => (
        <div key={amb.dadosAmbiente.id} className="border rounded-lg p-4 space-y-3">
          <h2 className="text-lg font-semibold">{amb.dadosAmbiente.nome}</h2>
          <p className="text-sm text-muted-foreground">
            Bloco {amb.dadosAmbiente.localizacao.bloco} • Unidade {amb.dadosAmbiente.localizacao.unidade} • Andar {amb.dadosAmbiente.localizacao.andar}
          </p>

          <ul className="text-sm list-disc pl-6">
            {amb.detalhesEsquadrias.esquadrias.map((e) => (
              <li key={e.id}>
                {e.tipo} {e.geometria.base}x{e.geometria.altura}m — {e.material}
                {(e.alturaPeitoril > 0) && <span> peitoril: {e.alturaPeitoril}m</span>}
                {e.informacaoAdicional && <span> — {e.informacaoAdicional}</span>}
                {' '}— área {e.area.toFixed(2)} m²
              </li>
            ))}
          </ul>

          {/* Resumo por tipo/material */}
          {amb.detalhesEsquadrias.esquadriasTipoMaterial.length > 0 && (
            <div className="text-sm">
              <strong>Resumo:</strong>
              <ul className="list-disc pl-6">
                {amb.detalhesEsquadrias.esquadriasTipoMaterial.map((r) => (
                  <li key={`${r.tipo}-${r.material}`}>{r.tipo} / {r.material}: {r.area.toFixed(2)} m²</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      ))}

      {/* Resumo global */}
      {data.totalTipoMaterial.length > 0 && (
        <div className="border-t pt-4 space-y-2">
          <h2 className="text-lg font-semibold">Resumo Global</h2>
          <ul className="text-sm list-disc pl-6">
            {data.totalTipoMaterial.map((r) => (
              <li key={`${r.tipo}-${r.material}`}>{r.tipo} / {r.material}: {r.area.toFixed(2)} m²</li>
            ))}
          </ul>
        </div>
      )}

      {/* Paginação */}
      <div className="flex gap-2 items-center">
        <Button
          variant="outline"
          size="sm"
          disabled={!data.dadosPaginacao.hasPrevious}
          onClick={() => setPage((p) => p - 1)}
        >
          Anterior
        </Button>
        <span>Página {data.dadosPaginacao.currentPage + 1} de {data.dadosPaginacao.totalPages}</span>
        <Button
          variant="outline"
          size="sm"
          disabled={!data.dadosPaginacao.hasNext}
          onClick={() => setPage((p) => p + 1)}
        >
          Próximo
        </Button>
      </div>
    </div>
  )
}
