import { renderHook, act } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { useFiltroLocal } from './useFiltroLocal'

describe('useFiltroLocal', () => {
  it('mantém o rascunho local ao editar', () => {
    const { result } = renderHook(() => useFiltroLocal(''))
    act(() => result.current.setLocal('Ana'))
    expect(result.current.local).toBe('Ana')
  })

  it('sincroniza quando o valor externo muda (back/forward)', () => {
    const aoSincronizar = vi.fn()
    const { result, rerender } = renderHook(
      ({ initial }) => useFiltroLocal(initial, aoSincronizar),
      { initialProps: { initial: '' } },
    )
    act(() => result.current.setLocal('rascunho'))
    rerender({ initial: 'Ana' })
    expect(result.current.local).toBe('Ana')
    expect(aoSincronizar).toHaveBeenCalledTimes(1)
  })

  it('não reseta o rascunho quando o valor externo é igual por valor', () => {
    const { result, rerender } = renderHook(
      ({ initial }) => useFiltroLocal<{ nome: string }>(initial),
      { initialProps: { initial: { nome: '' } } },
    )
    act(() => result.current.setLocal({ nome: 'Sala 202' }))
    // Nova referência, mesmos valores (ex.: mudança de page na URL)
    rerender({ initial: { nome: '' } })
    expect(result.current.local).toEqual({ nome: 'Sala 202' })
  })

  it('aceita objeto plano como valor externo', () => {
    const { result, rerender } = renderHook(
      ({ initial }) => useFiltroLocal<{ nome: string }>(initial),
      { initialProps: { initial: { nome: '' } } },
    )
    rerender({ initial: { nome: 'Sala 101' } })
    expect(result.current.local).toEqual({ nome: 'Sala 101' })
  })
})
