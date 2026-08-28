import {useNavigate, useParams} from 'react-router'
import {useQuery} from '@tanstack/react-query'
import {fetchDetalhePublicados} from '@/lib/api/api-publicados'
import {DetalheAmbiente} from '@/components/ambientes/DetalheAmbiente'
import {Button} from '@/components/ui/button'
import {ROUTES} from '@/constants/routes'

export function PublicadoDetalhePage() {
    const {id} = useParams<{ id: string }>()
    const navigate = useNavigate()

    const {data: ambiente, isLoading, error} = useQuery({
        queryKey: ['ambientes', 'publicados', 'detalhe', id],
        queryFn: ({signal}) => fetchDetalhePublicados(Number(id), signal),
        enabled: !!id,
    })

    if (isLoading) return <p>Carregando…</p>
    if (error || !ambiente) {
        return (
            <div className="space-y-4">
                <p>Ambiente não encontrado.</p>
                <Button variant="outline" onClick={() => navigate(ROUTES.PUBLICADOS)}>
                    Voltar à lista
                </Button>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            <Button variant="outline" onClick={() => navigate(ROUTES.PUBLICADOS)}>
                Voltar
            </Button>
            <DetalheAmbiente ambiente={ambiente}/>
        </div>
    )
}
