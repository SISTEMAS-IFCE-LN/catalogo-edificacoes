import { useParams, useNavigate } from 'react-router'
import { useQuery } from '@tanstack/react-query'
import { fetchDetalheAmbiente } from '@/lib/api/api-ambientes'
import { DetalheAmbiente } from '@/components/ambientes/DetalheAmbiente'
import { Button } from '@/components/ui/button'

export function PublicadoDetalhePage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: ambiente, isLoading, error } = useQuery({
    queryKey: ['publicados', 'detalhe', id],
    queryFn: () => fetchDetalheAmbiente(Number(id)),
    enabled: !!id,
  })

  if (isLoading) return <p>Carregando…</p>
  if (error || !ambiente) {
    return (
      <div className="space-y-4">
        <p>Ambiente não encontrado.</p>
        <Button variant="outline" onClick={() => navigate('/ambientes/publicados')}>
          Voltar à lista
        </Button>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <Button variant="outline" onClick={() => navigate('/ambientes/publicados')}>
        Voltar
      </Button>
      <DetalheAmbiente ambiente={ambiente} />
    </div>
  )
}
