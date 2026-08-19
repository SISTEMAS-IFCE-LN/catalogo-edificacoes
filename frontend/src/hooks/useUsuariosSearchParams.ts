import {useMemo} from 'react'
import {useSearchParams} from 'react-router'
import {usePaginationParams} from '@/hooks/usePaginationParams'
import {useFiltroLocal} from '@/hooks/useFiltroLocal'
import {TipoFiltroUsuarios, type FiltrosUsuarios} from '@/types/usuarios/filtros'

export interface UsuariosSearchParams {
    page: number
    size: number
    filtros: FiltrosUsuarios
    filtrosLocal: FiltrosUsuarios
    setFiltrosLocal: (f: FiltrosUsuarios) => void
    handleFiltrosChange: (f: FiltrosUsuarios) => void
    handlePageChange: (page: number) => void
    handleSizeChange: (size: string | null) => void
    tipoFiltro: TipoFiltroUsuarios
}

/**
 * Adaptador fino sobre `usePaginationParams`: mantém `nome`/`email` na URL e
 * delega o rascunho local a `useFiltroLocal` (back/forward). Deriva `tipoFiltro`
 * a partir do filtro ativo (nome → NOME, email → EMAIL, senão NENHUM).
 * Espelha `useAmbientesSearchParams`.
 */
export function useUsuariosSearchParams(): UsuariosSearchParams {
    const [searchParams] = useSearchParams()
    const {page, size, handlePageChange, handleSizeChange, updateSearchParams} = usePaginationParams()

    const filtros = useMemo<FiltrosUsuarios>(() => ({
        nome: searchParams.get('nome') ?? '',
        email: searchParams.get('email') ?? '',
    }), [searchParams])

    // Rascunho local sincronizado com a URL (back/forward) — delegado a useFiltroLocal
    const {local: filtrosLocal, setLocal: setFiltrosLocal} = useFiltroLocal<FiltrosUsuarios>(filtros)

    function handleFiltrosChange(novos: FiltrosUsuarios) {
        setFiltrosLocal(novos)
        // nome e email são mutuamente exclusivos: aplicar um limpa o outro
        updateSearchParams({nome: novos.nome || null, email: novos.email || null, page: 0})
    }

    const tipoFiltro = useMemo<TipoFiltroUsuarios>(() => {
        if (filtros.nome) return TipoFiltroUsuarios.NOME
        if (filtros.email) return TipoFiltroUsuarios.EMAIL
        return TipoFiltroUsuarios.NENHUM
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
