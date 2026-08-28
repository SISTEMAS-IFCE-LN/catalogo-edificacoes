import { useSearchParams, useNavigate } from 'react-router'
import { useQuery } from '@tanstack/react-query'
import { fetchEsquadriasPublicados } from '@/lib/api/api-publicados'
import { fetchEsquadriasValidacao } from '@/lib/api/api-validacao'
import { useEffect, useMemo, useState } from 'react'
import { Button } from '@/components/ui/button'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import { toast } from 'sonner'
import { DetalheEsquadrias } from '@/components/ambientes/DetalheEsquadrias'
import {
    filtrarEsquadrias,
    obterIdsInvalidos,
    temEsquadriasVisiveis,
    type FiltroEsquadrias,
} from '@/lib/ambientes/esquadrias'
import { MaterialEsquadria, TipoEsquadria } from '@/types/ambientes/enums'
import { ROUTES } from '@/constants/routes'

const OPCAO_TODOS = 'TODOS'

export type ContextoEsquadrias = 'publicados' | 'validacao'

interface EsquadriasPageProps {
    contexto: ContextoEsquadrias
}

function parseIds(idsParam: string | null): number[] {
    if (!idsParam) return []
    return idsParam
        .split(',')
        .map((s) => Number(s.trim()))
        .filter((n) => Number.isInteger(n) && n > 0)
}

function atualizarSearchParams(
    setSearchParams: ReturnType<typeof useSearchParams>[1],
    updates: Record<string, string | null>,
) {
    setSearchParams((params) => {
        for (const [key, value] of Object.entries(updates)) {
            if (value === null || value === '' || value === OPCAO_TODOS) {
                params.delete(key)
            } else {
                params.set(key, value)
            }
        }
        return params
    })
}

export function EsquadriasPage({ contexto }: EsquadriasPageProps) {
    const [searchParams, setSearchParams] = useSearchParams()
    const navigate = useNavigate()
    const ids = useMemo(() => parseIds(searchParams.get('ids')), [searchParams])

    // Pontos contextuais (antes hardcoded para publicados):
    const fetchEsquadrias =
        contexto === 'publicados' ? fetchEsquadriasPublicados : fetchEsquadriasValidacao
    const rotaVoltar = contexto === 'publicados' ? ROUTES.PUBLICADOS : ROUTES.VALIDACAO

    const filtroTipo = searchParams.get('tipo') ?? ''
    const filtroMaterial = searchParams.get('material') ?? ''
    const filtro: FiltroEsquadrias = useMemo(
        () => ({ tipo: filtroTipo, material: filtroMaterial }),
        [filtroTipo, filtroMaterial],
    )

    const [page, setPage] = useState(0)

    const { data, isLoading, error } = useQuery({
        // Namespaced por contexto: evita colisão de cache publicados × validação
        queryKey: ['esquadrias', contexto, ids, page],
        queryFn: ({ signal }) => fetchEsquadrias({
            ids,
            page,
            size: 100,
        }, signal),
        enabled: ids.length > 0,
    })

    useEffect(() => {
        if (error) toast.error('Erro ao carregar esquadrias.')
    }, [error])

    const idsInvalidos = useMemo(
        () => (data ? obterIdsInvalidos(ids, data) : []),
        [data, ids],
    )

    const responseFiltrada = useMemo(
        () => (data ? filtrarEsquadrias(data, filtro) : null),
        [data, filtro],
    )

    function handleFiltroTipo(value: string | null) {
        atualizarSearchParams(setSearchParams, {
            tipo: value === OPCAO_TODOS || value === null ? null : value,
        })
    }

    function handleFiltroMaterial(value: string | null) {
        atualizarSearchParams(setSearchParams, {
            material: value === OPCAO_TODOS || value === null ? null : value,
        })
    }

    function handleRemoverInvalidos() {
        const idsValidos = ids.filter((id) => !idsInvalidos.includes(id))
        atualizarSearchParams(setSearchParams, { ids: idsValidos.join(',') || null })
    }

    if (ids.length === 0) {
        return <p>Nenhum ambiente selecionado.</p>
    }

    if (isLoading) return <p>Carregando…</p>

    if (error) {
        return (
            <div className="space-y-4">
                <p>Erro ao carregar dados.</p>
                <Button variant="outline" onClick={() => window.location.reload()}>
                    Tentar novamente
                </Button>
            </div>
        )
    }

    if (!data || data.ambientes.length === 0) {
        return <p>Nenhuma esquadria encontrada.</p>
    }

    return (
        <div className="space-y-6">
            <Button variant="outline" onClick={() => navigate(rotaVoltar)}>
                Voltar
            </Button>
            {/* Filtros client-side por tipo e material (UC20-FE) */}
            <div className="flex flex-wrap items-end gap-3">
                <div className="space-y-1">
                    <label htmlFor="filtro-tipo" className="text-sm font-medium">
                        Tipo
                    </label>
                    <Select value={filtroTipo || OPCAO_TODOS} onValueChange={handleFiltroTipo}>
                        <SelectTrigger id="filtro-tipo" className="w-[180px]" aria-label="Filtrar por tipo">
                            <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value={OPCAO_TODOS}>Todos</SelectItem>
                            {Object.values(TipoEsquadria).map((t) => (
                                <SelectItem key={t} value={t}>{t}</SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>

                <div className="space-y-1">
                    <label htmlFor="filtro-material" className="text-sm font-medium">
                        Material
                    </label>
                    <Select value={filtroMaterial || OPCAO_TODOS} onValueChange={handleFiltroMaterial}>
                        <SelectTrigger id="filtro-material" className="w-[200px]" aria-label="Filtrar por material">
                            <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value={OPCAO_TODOS}>Todos</SelectItem>
                            {Object.values(MaterialEsquadria).map((m) => (
                                <SelectItem key={m} value={m}>{m}</SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>
            </div>

            {/* Aviso de IDs inválidos (UC20-FE) */}
            {idsInvalidos.length > 0 && (
                <div role="alert" className="border border-destructive rounded-lg p-4 space-y-2">
                    <p className="text-destructive font-medium">
                        IDs inválidos: {idsInvalidos.join(', ')}
                    </p>
                    <p className="text-sm text-muted-foreground">
                        Alguns ambientes solicitados não foram encontrados ou não possuem esquadrias publicadas.
                    </p>
                    <Button variant="outline" size="sm" onClick={handleRemoverInvalidos}>
                        Remover inválidos e tentar novamente
                    </Button>
                </div>
            )}

            {/* Callout de vazio pós-filtro (UC20-FE) */}
            {responseFiltrada && !temEsquadriasVisiveis(responseFiltrada.ambientes) ? (
                <div className="border rounded-lg p-4">
                    <p className="text-muted-foreground">
                        Nenhuma esquadria encontrada para os filtros aplicados. Tente remover ou ajustar os filtros.
                    </p>
                </div>
            ) : responseFiltrada ? (
                <DetalheEsquadrias response={responseFiltrada} />
            ) : null}

            {/* Paginação */}
            {responseFiltrada && (
                <div className="flex gap-2 items-center">
                    <Button
                        variant="outline"
                        size="sm"
                        disabled={!responseFiltrada.dadosPaginacao.hasPrevious}
                        onClick={() => setPage((p) => p - 1)}
                    >
                        Anterior
                    </Button>
                    <span>
                        Página {responseFiltrada.dadosPaginacao.currentPage + 1} de{' '}
                        {responseFiltrada.dadosPaginacao.totalPages}
                    </span>
                    <Button
                        variant="outline"
                        size="sm"
                        disabled={!responseFiltrada.dadosPaginacao.hasNext}
                        onClick={() => setPage((p) => p + 1)}
                    >
                        Próximo
                    </Button>
                </div>
            )}
        </div>
    )
}