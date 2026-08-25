import { useCallback, useState } from 'react'

interface UseSelecaoAmbientesOptions {
    /** Quando false, a seleção fica inerte (modo anônimo da lista pública). */
    ativa?: boolean
}

export interface SelecaoAmbientes {
    selectedIds: number[]
    limparSelecao: () => void
    toggleSelect: (id: number) => void
    toggleSelectAll: () => void
    allSelected: boolean
    someSelected: boolean
}

/**
 * Estado de seleção múltipla por página visível, compartilhado pelas listas
 * de ambientes (publicados, validação e não-publicados).
 *
 * A seleção é limpa pelas próprias páginas ao mudar página/filtros/size
 * (chamam `limparSelecao()` antes do handler de URL) — sem useEffect/refs.
 */
export function useSelecaoAmbientes(
    idsDaPagina: number[],
    { ativa = true }: UseSelecaoAmbientesOptions = {},
): SelecaoAmbientes {
    const [selectedIds, setSelectedIds] = useState<number[]>([])

    const limparSelecao = useCallback(() => setSelectedIds([]), [])

    const toggleSelect = useCallback((id: number) => {
        setSelectedIds((prev) =>
            prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
        )
    }, [])

    const toggleSelectAll = useCallback(() => {
        setSelectedIds((prev) => {
            const todosSelecionados =
                idsDaPagina.length > 0 && idsDaPagina.every((id) => prev.includes(id))
            return todosSelecionados ? [] : idsDaPagina
        })
    }, [idsDaPagina])

    const allSelected =
        ativa && idsDaPagina.length > 0 && idsDaPagina.every((id) => selectedIds.includes(id))
    const someSelected = ativa && selectedIds.length > 0 && !allSelected

    return {
        selectedIds,
        limparSelecao,
        toggleSelect,
        toggleSelectAll,
        allSelected,
        someSelected,
    }
}
