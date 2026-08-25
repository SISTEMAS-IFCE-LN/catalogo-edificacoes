import { renderHook, act } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { useSelecaoAmbientes } from './useSelecaoAmbientes'

describe('useSelecaoAmbientes', () => {
    it('seleciona e desmarca um id', () => {
        const { result } = renderHook(() => useSelecaoAmbientes([1, 2, 3]))

        act(() => result.current.toggleSelect(2))
        expect(result.current.selectedIds).toEqual([2])
        expect(result.current.allSelected).toBe(false)
        expect(result.current.someSelected).toBe(true)

        act(() => result.current.toggleSelect(2))
        expect(result.current.selectedIds).toEqual([])
        expect(result.current.someSelected).toBe(false)
    })

    it('seleciona todos e limpa ao alternar novamente', () => {
        const { result } = renderHook(() => useSelecaoAmbientes([1, 2, 3]))

        act(() => result.current.toggleSelectAll())
        expect(result.current.selectedIds).toEqual([1, 2, 3])
        expect(result.current.allSelected).toBe(true)
        expect(result.current.someSelected).toBe(false)

        act(() => result.current.toggleSelectAll())
        expect(result.current.selectedIds).toEqual([])
    })

    it('limparSelecao esvazia a seleção', () => {
        const { result } = renderHook(() => useSelecaoAmbientes([1, 2]))

        act(() => {
            result.current.toggleSelect(1)
            result.current.toggleSelect(2)
        })
        expect(result.current.selectedIds).toEqual([1, 2])

        act(() => result.current.limparSelecao())
        expect(result.current.selectedIds).toEqual([])
        expect(result.current.someSelected).toBe(false)
    })

    it('não marca allSelected com página vazia', () => {
        const { result } = renderHook(() => useSelecaoAmbientes([]))
        expect(result.current.allSelected).toBe(false)
        expect(result.current.someSelected).toBe(false)
    })

    it('com ativa=false mantém os flags inativos (modo anônimo)', () => {
        const { result } = renderHook(() => useSelecaoAmbientes([1, 2], { ativa: false }))

        act(() => result.current.toggleSelect(1))
        expect(result.current.allSelected).toBe(false)
        expect(result.current.someSelected).toBe(false)
    })

    it('reage à troca de página sem arrastar ids antigos', () => {
        const { result, rerender } = renderHook(({ ids }) => useSelecaoAmbientes(ids), {
            initialProps: { ids: [1, 2] },
        })

        act(() => result.current.toggleSelectAll())
        expect(result.current.selectedIds).toEqual([1, 2])

        rerender({ ids: [3, 4] })
        expect(result.current.allSelected).toBe(false)

        act(() => result.current.toggleSelectAll())
        expect(result.current.selectedIds).toEqual([3, 4])
    })
})
