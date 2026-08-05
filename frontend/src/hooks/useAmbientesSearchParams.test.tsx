import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useAmbientesSearchParams } from './useAmbientesSearchParams'
import { MemoryRouter, useSearchParams } from 'react-router'
import { type ReactNode } from 'react'

// Wrapper para fornecer contexto do router
function createWrapper(initialEntries: string[] = ['/']) {
  return function Wrapper({ children }: { children: ReactNode }) {
    return <MemoryRouter initialEntries={initialEntries}>{children}</MemoryRouter>
  }
}

describe('useAmbientesSearchParams', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('retorna valores padrão quando URL está vazia', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/']),
    })

    expect(result.current.page).toBe(0)
    expect(result.current.size).toBe(20)
    expect(result.current.filtros).toEqual({
      nome: '',
      bloco: '',
      unidade: '',
      andar: null,
      tipo: '',
    })
  })

  it('lê parâmetros da URL corretamente', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?nome=sala&bloco=A&andar=2&page=1&size=50']),
    })

    expect(result.current.page).toBe(1)
    expect(result.current.size).toBe(50)
    expect(result.current.filtros.nome).toBe('sala')
    expect(result.current.filtros.bloco).toBe('A')
    expect(result.current.filtros.andar).toBe(2)
  })

  it('handleFiltrosChange atualiza URL e reseta página para 0', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?page=5']),
    })

    act(() => {
      result.current.handleFiltrosChange({
        nome: 'sala',
        bloco: '',
        unidade: '',
        andar: null,
        tipo: '',
      })
    })

    // Verificar se a URL foi atualizada (page deve ser 0)
    expect(result.current.filtros.nome).toBe('sala')
    expect(result.current.page).toBe(0)
  })

  it('handleSizeChange atualiza tamanho e reseta página', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?page=3&size=20']),
    })

    act(() => {
      result.current.handleSizeChange('50')
    })

    expect(result.current.size).toBe(50)
    expect(result.current.page).toBe(0)
  })

  it('handlePageChange atualiza página na URL', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/']),
    })

    act(() => {
      result.current.handlePageChange(5)
    })

    expect(result.current.page).toBe(5)
  })

  it('sincroniza filtrosLocal quando URL muda externamente', () => {
    // Componente auxiliar para modificar a URL
    function UrlModifier({ onReady }: { onReady: (setSearchParams: ReturnType<typeof useSearchParams>[1]) => void }) {
      const [, setSearchParams] = useSearchParams()
      onReady(setSearchParams)
      return null
    }

    let setSearchParamsRef: ReturnType<typeof useSearchParams>[1] | null = null

    const { result } = renderHook(
      () => {
        const hookResult = useAmbientesSearchParams()
        return {
          hookResult,
          setSearchParams: (fn: (params: URLSearchParams) => URLSearchParams) => {
            if (setSearchParamsRef) {
              setSearchParamsRef(fn)
            }
          },
        }
      },
      {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={['/?nome=old']}>
            <UrlModifier onReady={(setSearchParams) => { setSearchParamsRef = setSearchParams }} />
            {children}
          </MemoryRouter>
        ),
      }
    )

    // Estado inicial
    expect(result.current.hookResult.filtros.nome).toBe('old')
    expect(result.current.hookResult.filtrosLocal.nome).toBe('old')

    // Simular mudança externa na URL (back/forward)
    act(() => {
      if (setSearchParamsRef) {
        setSearchParamsRef((params) => {
          params.set('nome', 'new')
          return params
        })
      }
    })

    // filtrosLocal deve ser sincronizado com a URL
    expect(result.current.hookResult.filtros.nome).toBe('new')
    expect(result.current.hookResult.filtrosLocal.nome).toBe('new')
  })

  it('rejeita nome com mais de 50 caracteres e usa fallback', () => {
    const longName = 'a'.repeat(51)
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper([`/?nome=${longName}`]),
    })

    // Deve usar fallback (filtros vazios) quando validação falha
    expect(result.current.filtros.nome).toBe('')
  })

  it('aceita nome com exatamente 50 caracteres', () => {
    const exactName = 'a'.repeat(50)
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper([`/?nome=${exactName}`]),
    })

    expect(result.current.filtros.nome).toBe(exactName)
  })

  it('rejeita andar negativo e usa fallback', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?andar=-5']),
    })

    // Deve usar fallback (null) quando validação falha
    expect(result.current.filtros.andar).toBeNull()
  })

  it('aceita andar igual a 0', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?andar=0']),
    })

    expect(result.current.filtros.andar).toBe(0)
  })

  it('rejeita bloco com mais de 50 caracteres e usa fallback', () => {
    const longBloco = 'b'.repeat(51)
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper([`/?bloco=${longBloco}`]),
    })

    expect(result.current.filtros.bloco).toBe('')
  })

  it('aceita bloco com exatamente 50 caracteres', () => {
    const exactBloco = 'b'.repeat(50)
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper([`/?bloco=${exactBloco}`]),
    })

    expect(result.current.filtros.bloco).toBe(exactBloco)
  })

  it('rejeita tipo com mais de 50 caracteres e usa fallback', () => {
    const longTipo = 't'.repeat(51)
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper([`/?tipo=${longTipo}`]),
    })

    expect(result.current.filtros.tipo).toBe('')
  })

  it('rejeita unidade com mais de 50 caracteres e usa fallback', () => {
    const longUnidade = 'u'.repeat(51)
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper([`/?unidade=${longUnidade}`]),
    })

    expect(result.current.filtros.unidade).toBe('')
  })
})
