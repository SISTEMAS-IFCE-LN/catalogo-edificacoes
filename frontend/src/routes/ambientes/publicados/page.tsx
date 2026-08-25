import {useQuery} from '@tanstack/react-query'
import {useEffect, useMemo} from 'react'
import {fetchPublicados} from '@/lib/api/api-publicados'
import type {AmbientesQuery} from '@/types/ambientes/ambiente'
import {PesquisaBarAmbientes} from '@/components/ambientes/PesquisaBarAmbientes'
import {TabelaPadrao} from '@/components/ambientes/TabelaPadrao'
import {AcoesLote} from '@/components/ambientes/AcoesLote'
import {ErrorLista} from '@/components/ambientes/ErrorLista'
import {PaginacaoFooter} from '@/components/ambientes/PaginacaoFooter'
import {toast} from 'sonner'
import {useAmbientesSearchParams} from '@/hooks/useAmbientesSearchParams'
import {useSelecaoAmbientes} from '@/hooks/useSelecaoAmbientes'
import {useAuth} from '@/hooks/useAuth'

export function PublicadosPage() {
    const {user} = useAuth()
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

    const {data, isLoading, error, refetch} = useQuery({
        queryKey: ['ambientes', 'publicados', query],
        queryFn: ({signal}) => fetchPublicados(query, signal),
    })

    // Seleção múltipla por página visível (UC20-FE). Só para usuários autenticados:
    // anônimos veem a lista sem checkboxes (gate via `ativa` do hook).
    const idsDaPagina = useMemo(
        () => (data ? data.ambientes.map((a) => a.id) : []),
        [data],
    )
    const selecao = useSelecaoAmbientes(idsDaPagina, {ativa: autenticado})

    useEffect(() => {
        if (error) {
            toast.error('Erro ao carregar ambientes. Tente novamente.')
        }
    }, [error])

    if (error) {
        return (
            <div className="space-y-4">
                <h1 className="text-2xl font-bold">Ambientes Publicados</h1>
                <ErrorLista onTentarNovamente={() => refetch()}/>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            <h1 className="text-2xl font-bold">Ambientes Publicados</h1>
            <PesquisaBarAmbientes
                initial={filtrosLocal}
                onChange={(novosFiltros) => {
                    selecao.limparSelecao()
                    handleFiltrosChange(novosFiltros)
                }}
            />
            {isLoading ? (
                <p>Carregando…</p>
            ) : data && data.ambientes.length > 0 ? (
                <>
                    {autenticado && (
                        <AcoesLote
                            selectedIds={selecao.selectedIds}
                            onClear={selecao.limparSelecao}
                        />
                    )}
                    <TabelaPadrao
                        itens={data.ambientes}
                        selectedIds={autenticado ? selecao.selectedIds : undefined}
                        onToggleSelect={autenticado ? selecao.toggleSelect : undefined}
                        onToggleSelectAll={autenticado ? selecao.toggleSelectAll : undefined}
                        allSelected={autenticado ? selecao.allSelected : undefined}
                        someSelected={autenticado ? selecao.someSelected : undefined}
                    />
                    <PaginacaoFooter
                        page={page}
                        size={size}
                        areaTotal={data.areaTotal}
                        hasPrevious={data.dadosPaginacao.hasPrevious}
                        hasNext={data.dadosPaginacao.hasNext}
                        currentPage={data.dadosPaginacao.currentPage}
                        totalPages={data.dadosPaginacao.totalPages}
                        onPageChange={(novaPagina) => {
                            selecao.limparSelecao()
                            handlePageChange(novaPagina)
                        }}
                        onSizeChange={(novoSize) => {
                            selecao.limparSelecao()
                            handleSizeChange(novoSize)
                        }}
                    />
                </>
            ) : (
                <p className="text-muted-foreground">Nenhum ambiente encontrado.</p>
            )}
        </div>
    )
}
