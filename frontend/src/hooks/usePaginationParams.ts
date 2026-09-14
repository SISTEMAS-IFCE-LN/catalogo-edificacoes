import { useSearchParams } from 'react-router'

const SIZE_PADRAO = 20

export interface PaginationParams {
    page: number
    size: number
    handlePageChange: (page: number) => void
    handleSizeChange: (size: string | null) => void
    updateSearchParams: (updates: Record<string, string | number | null>) => void
}

/**
 * Núcleo genérico de paginação baseado na URL. Compartilhado por
 * `useAmbientesSearchParams` e `useUsuariosSearchParams`.
 */
export function usePaginationParams(): PaginationParams {
    const [searchParams, setSearchParams] = useSearchParams()

    // NaN-safe: `Number.isInteger` descarta valores inválidos ou não inteiros
    const rawPage = Number(searchParams.get('page') ?? '0')
    const page = Number.isInteger(rawPage) && rawPage >= 0 ? rawPage : 0
    const rawSize = Number(searchParams.get('size') ?? String(SIZE_PADRAO))
    const size = Number.isInteger(rawSize) && rawSize > 0 ? rawSize : SIZE_PADRAO

    function updateSearchParams(updates: Record<string, string | number | null>) {
        setSearchParams((params) => {
            for (const [key, value] of Object.entries(updates)) {
                if (value === null || value === '' || value === undefined) params.delete(key)
                else params.set(key, String(value))
            }
            return params
        })
    }

    function handlePageChange(newPage: number) {
        updateSearchParams({ page: newPage })
    }

    function handleSizeChange(novoSize: string | null) {
        if (novoSize) {
            updateSearchParams({ size: Number(novoSize), page: 0 })
        }
    }

    return { page, size, handlePageChange, handleSizeChange, updateSearchParams }
}
