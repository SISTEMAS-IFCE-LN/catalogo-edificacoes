import { useSearchParams } from 'react-router'
import { usePaginationParams } from '@/hooks/usePaginationParams'
import { useFiltroLocal } from '@/hooks/useFiltroLocal'

export interface UsuariosSearchParams {
    nome: string
    page: number
    size: number
    nomeLocal: string
    setNomeLocal: (nome: string) => void
    handleNomeChange: (nome: string) => void
    handlePageChange: (page: number) => void
    handleSizeChange: (size: string | null) => void
}

/**
 * Adaptador fino sobre `usePaginationParams`: mantém `nome` na URL e delega
 * o rascunho local a `useFiltroLocal` (back/forward).
 */
export function useUsuariosSearchParams(): UsuariosSearchParams {
    const [searchParams] = useSearchParams()
    const { page, size, handlePageChange, handleSizeChange, updateSearchParams } = usePaginationParams()

    const nome = searchParams.get('nome') ?? ''

    // Rascunho local sincronizado com a URL (back/forward) — delegado a useFiltroLocal
    const { local: nomeLocal, setLocal: setNomeLocal } = useFiltroLocal<string>(nome)

    function handleNomeChange(novoNome: string) {
        setNomeLocal(novoNome)
        updateSearchParams({ nome: novoNome || null, page: 0 }) // resetar página ao aplicar filtro
    }

    return {
        nome,
        page,
        size,
        nomeLocal,
        setNomeLocal,
        handleNomeChange,
        handlePageChange,
        handleSizeChange,
    }
}
