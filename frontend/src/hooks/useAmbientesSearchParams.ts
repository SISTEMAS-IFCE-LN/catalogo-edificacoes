import { useMemo } from 'react'
import { useSearchParams } from 'react-router'
import { type Filtros, FiltrosUrlSchema, FILTROS_VAZIOS } from '@/types/ambientes/filtros'
import { TipoFiltro } from '@/types/ambientes/enums'
import { usePaginationParams } from '@/hooks/usePaginationParams'
import { useFiltroLocal } from '@/hooks/useFiltroLocal'

export interface AmbientesSearchParams {
    page: number
    size: number
    filtros: Filtros
    filtrosLocal: Filtros
    setFiltrosLocal: (f: Filtros) => void
    handleFiltrosChange: (f: Filtros) => void
    handlePageChange: (page: number) => void
    handleSizeChange: (size: string | null) => void
    tipoFiltro: TipoFiltro
}

export function useAmbientesSearchParams(): AmbientesSearchParams {
    const [searchParams] = useSearchParams()
    const { page, size, handlePageChange, handleSizeChange, updateSearchParams } = usePaginationParams()

    // Validar filtros da URL com Zod (page/size já são de usePaginationParams)
    const filtros = useMemo<Filtros>(() => {
        const result = FiltrosUrlSchema.safeParse({
            nome: searchParams.get('nome'),
            bloco: searchParams.get('bloco'),
            unidade: searchParams.get('unidade'),
            andar: searchParams.get('andar'),
            tipo: searchParams.get('tipo'),
        })
        return result.success ? result.data : FILTROS_VAZIOS
    }, [searchParams])

    // Rascunho local sincronizado com a URL (back/forward) — delegado a useFiltroLocal
    const { local: filtrosLocal, setLocal: setFiltrosLocal } = useFiltroLocal<Filtros>(filtros)

    function handleFiltrosChange(novosFiltros: Filtros) {
        setFiltrosLocal(novosFiltros)
        updateSearchParams({
            nome: novosFiltros.nome || null,
            bloco: novosFiltros.bloco || null,
            unidade: novosFiltros.unidade || null,
            andar: novosFiltros.andar,
            tipo: novosFiltros.tipo || null,
            page: 0, // resetar página ao aplicar filtros
        })
    }

    // Derivar tipoFiltro a partir dos filtros ativos
    const tipoFiltro = useMemo<TipoFiltro>(() => {
        if (filtros.nome) return TipoFiltro.NOME
        if (filtros.tipo) return TipoFiltro.TIPO
        if (filtros.bloco || filtros.unidade || filtros.andar !== null) return TipoFiltro.LOCALIZACAO
        return TipoFiltro.NENHUM
    }, [filtros])

    return {
        page,
        size,
        filtros,
        filtrosLocal,
        setFiltrosLocal,
        handleFiltrosChange,
        handlePageChange,
        handleSizeChange,
        tipoFiltro,
    }
}
