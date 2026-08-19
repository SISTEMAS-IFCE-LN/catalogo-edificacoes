import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useUsuariosSearchParams } from './useUsuariosSearchParams'
import { MemoryRouter } from 'react-router'
import { type ReactNode } from 'react'
import { TipoFiltroUsuarios } from '@/types/usuarios/filtros'

// Wrapper para fornecer contexto do router
function createWrapper(initialEntries: string[] = ['/']) {
  return function Wrapper({ children }: { children: ReactNode }) {
    return <MemoryRouter initialEntries={initialEntries}>{children}</MemoryRouter>
  }
}

describe('useUsuariosSearchParams', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('retorna valores padrão quando URL está vazia', () => {
    const { result } = renderHook(() => useUsuariosSearchParams(), {
      wrapper: createWrapper(['/']),
    })

    expect(result.current.page).toBe(0)
    expect(result.current.size).toBe(20)
    expect(result.current.filtros).toEqual({ nome: '', email: '' })
    expect(result.current.tipoFiltro).toBe(TipoFiltroUsuarios.NENHUM)
  })

  it('lê nome da URL e deriva tipoFiltro NOME', () => {
    const { result } = renderHook(() => useUsuariosSearchParams(), {
      wrapper: createWrapper(['/?nome=joao']),
    })

    expect(result.current.filtros.nome).toBe('joao')
    expect(result.current.tipoFiltro).toBe(TipoFiltroUsuarios.NOME)
  })

  it('lê email da URL e deriva tipoFiltro EMAIL', () => {
    const { result } = renderHook(() => useUsuariosSearchParams(), {
      wrapper: createWrapper(['/?email=joao%40ifce.edu.br']),
    })

    expect(result.current.filtros.email).toBe('joao@ifce.edu.br')
    expect(result.current.tipoFiltro).toBe(TipoFiltroUsuarios.EMAIL)
  })

  it('handleFiltrosChange com nome limpa email e reseta página', () => {
    const { result } = renderHook(() => useUsuariosSearchParams(), {
      wrapper: createWrapper(['/?email=joao%40ifce.edu.br&page=5']),
    })

    act(() => {
      result.current.handleFiltrosChange({ nome: 'joao', email: '' })
    })

    expect(result.current.filtros.nome).toBe('joao')
    expect(result.current.filtros.email).toBe('')
    expect(result.current.page).toBe(0)
    expect(result.current.tipoFiltro).toBe(TipoFiltroUsuarios.NOME)
  })

  it('handleFiltrosChange com email limpa nome', () => {
    const { result } = renderHook(() => useUsuariosSearchParams(), {
      wrapper: createWrapper(['/?nome=joao']),
    })

    act(() => {
      result.current.handleFiltrosChange({ nome: '', email: 'joao@ifce.edu.br' })
    })

    expect(result.current.filtros.email).toBe('joao@ifce.edu.br')
    expect(result.current.filtros.nome).toBe('')
    expect(result.current.tipoFiltro).toBe(TipoFiltroUsuarios.EMAIL)
  })

  it('handlePageChange atualiza página na URL', () => {
    const { result } = renderHook(() => useUsuariosSearchParams(), {
      wrapper: createWrapper(['/']),
    })

    act(() => {
      result.current.handlePageChange(3)
    })

    expect(result.current.page).toBe(3)
  })

  it('handleSizeChange atualiza tamanho e reseta página', () => {
    const { result } = renderHook(() => useUsuariosSearchParams(), {
      wrapper: createWrapper(['/?page=2&size=20']),
    })

    act(() => {
      result.current.handleSizeChange('50')
    })

    expect(result.current.size).toBe(50)
    expect(result.current.page).toBe(0)
  })
})
