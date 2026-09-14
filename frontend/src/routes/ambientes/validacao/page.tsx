import {useQuery} from '@tanstack/react-query'
import {useEffect} from 'react'
import {fetchValidacao} from '@/lib/api/api-validacao'
import type {AmbientesQuery} from '@/types/ambientes/query'
import {PesquisaBarAmbientes} from '@/components/ambientes/PesquisaBarAmbientes'
import {TabelaPadrao} from '@/components/ambientes/TabelaPadrao'
import {ErrorLista} from '@/components/ambientes/ErrorLista'
import {PaginacaoFooter} from '@/components/ambientes/PaginacaoFooter'
import {toast} from 'sonner'
import {useAmbientesSearchParams} from '@/hooks/useAmbientesSearchParams'
import {PAGES_ROUTES} from '@/constants/routes'

export function ValidacaoPage() {
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

    const {data, isLoading, error, refetch} = useQuery({
        queryKey: ['ambientes', 'validacao', query],
        queryFn: ({signal}) => fetchValidacao(query, signal),
    })

    useEffect(() => {
        if (error) {
            toast.error('Erro ao carregar ambientes em validação. Tente novamente.')
        }
    }, [error])

    if (error) {
        return (
            <div className="space-y-4">
                <h1 className="text-2xl font-bold">Aguardando Validação</h1>
                <ErrorLista onTentarNovamente={() => refetch()}/>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            <h1 className="text-2xl font-bold">Aguardando Validação</h1>
            <PesquisaBarAmbientes
                initial={filtrosLocal}
                onChange={handleFiltrosChange}
            />
            {isLoading ? (
                <p>Carregando…</p>
            ) : data && data.ambientes.length > 0 ? (
                <>
                    <TabelaPadrao
                        itens={data.ambientes}
                        detalheBasePath={PAGES_ROUTES.VALIDACAO}
                    />
                    <PaginacaoFooter
                        page={page}
                        size={size}
                        areaTotal={data.areaTotal}
                        hasPrevious={data.dadosPaginacao.hasPrevious}
                        hasNext={data.dadosPaginacao.hasNext}
                        currentPage={data.dadosPaginacao.currentPage}
                        totalPages={data.dadosPaginacao.totalPages}
                        onPageChange={handlePageChange}
                        onSizeChange={handleSizeChange}
                    />
                </>
            ) : (
                <p className="text-muted-foreground">Nenhum ambiente aguardando validação.</p>
            )}
        </div>
    )
}
