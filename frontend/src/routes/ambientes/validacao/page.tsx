import {useQuery} from '@tanstack/react-query'
import {useEffect, useMemo} from 'react'
import {useNavigate} from 'react-router'
import {fetchValidacao} from '@/lib/api/api-validacao'
import type {AmbientesQuery} from '@/types/ambientes/ambiente'
import {PesquisaBarAmbientes} from '@/components/ambientes/PesquisaBarAmbientes'
import {TabelaPadrao} from '@/components/ambientes/TabelaPadrao'
import {AcoesLote, type AcaoLote} from '@/components/ambientes/AcoesLote'
import {ErrorLista} from '@/components/ambientes/ErrorLista'
import {PaginacaoFooter} from '@/components/ambientes/PaginacaoFooter'
import {toast} from 'sonner'
import {useAmbientesSearchParams} from '@/hooks/useAmbientesSearchParams'
import {useSelecaoAmbientes} from '@/hooks/useSelecaoAmbientes'
import {ROUTES} from '@/constants/routes'

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

    const {data, isLoading, error, refetch} = useQuery({
        queryKey: ['ambientes', 'validacao', query],
        queryFn: ({signal}) => fetchValidacao(query, signal),
    })

    // Seleção múltipla (UC01-FE). A rota é RequireRole VALIDADOR → sempre
    // autenticado, então não há gate por useAuth como no PublicadosPage.
    const idsDaPagina = useMemo(
        () => (data ? data.ambientes.map((a) => a.id) : []),
        [data],
    )
    const selecao = useSelecaoAmbientes(idsDaPagina)

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
                <ErrorLista onTentarNovamente={() => refetch()}/>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            <h1 className="text-2xl font-bold">Aguardando Validação</h1>
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
                    <AcoesLote
                        selectedIds={selecao.selectedIds}
                        onClear={selecao.limparSelecao}
                        acoes={acoesValidacao}
                    />
                    <TabelaPadrao
                        itens={data.ambientes}
                        detalheBasePath={ROUTES.VALIDACAO}
                        selectedIds={selecao.selectedIds}
                        onToggleSelect={selecao.toggleSelect}
                        onToggleSelectAll={selecao.toggleSelectAll}
                        allSelected={selecao.allSelected}
                        someSelected={selecao.someSelected}
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
                <p className="text-muted-foreground">Nenhum ambiente aguardando validação.</p>
            )}
        </div>
    )
}
