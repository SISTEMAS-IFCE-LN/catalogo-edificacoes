import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useAmbientesSearchParams } from './useAmbientesSearchParams'
import { MemoryRouter, useSearchParams } from 'react-router'
import { type ReactNode } from 'react'
import { TipoFiltro } from '@/types/ambientes/enums'

const FILTROS_VAZIOS = {
  nome: '',
  campusId: null,
  unidadeId: null,
  blocoId: null,
  andar: null,
  tipo: '',
}

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
    expect(result.current.filtros).toEqual(FILTROS_VAZIOS)
  })

  it('lê parâmetros da URL corretamente', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper([
        '/?nome=sala&campusId=1&unidadeId=2&blocoId=3&andar=2&page=1&size=50',
      ]),
    })

    expect(result.current.page).toBe(1)
    expect(result.current.size).toBe(50)
    expect(result.current.filtros.nome).toBe('sala')
    expect(result.current.filtros.campusId).toBe(1)
    expect(result.current.filtros.unidadeId).toBe(2)
    expect(result.current.filtros.blocoId).toBe(3)
    expect(result.current.filtros.andar).toBe(2)
  })

  it('handleFiltrosChange atualiza URL e reseta página para 0', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?page=5']),
    })

    act(() => {
      result.current.handleFiltrosChange({
        nome: 'sala',
        campusId: 1,
        unidadeId: 2,
        blocoId: 3,
        andar: null,
        tipo: '',
      })
    })

    expect(result.current.filtros.nome).toBe('sala')
    expect(result.current.filtros.campusId).toBe(1)
    expect(result.current.filtros.unidadeId).toBe(2)
    expect(result.current.filtros.blocoId).toBe(3)
    expect(result.current.page).toBe(0)
  })

  it('handleFiltrosChange descarta unidade sem campus', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/']),
    })

    act(() => {
      result.current.handleFiltrosChange({
        ...FILTROS_VAZIOS,
        unidadeId: 2,
      })
    })

    expect(result.current.filtros.unidadeId).toBeNull()
  })

  it('handleFiltrosChange descarta bloco sem unidade', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/']),
    })

    act(() => {
      result.current.handleFiltrosChange({
        ...FILTROS_VAZIOS,
        campusId: 1,
        blocoId: 3,
      })
    })

    expect(result.current.filtros.campusId).toBe(1)
    expect(result.current.filtros.blocoId).toBeNull()
  })

  it('descarta unidadeId da URL quando não há campusId', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?nome=sala&unidadeId=2']),
    })

    expect(result.current.filtros.nome).toBe('sala')
    expect(result.current.filtros.unidadeId).toBeNull()
  })

  it('descarta blocoId da URL quando não há unidadeId', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?campusId=1&blocoId=3']),
    })

    expect(result.current.filtros.campusId).toBe(1)
    expect(result.current.filtros.blocoId).toBeNull()
  })

  it('descarta blocoId da URL quando o pai também foi descartado', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?unidadeId=2&blocoId=3']),
    })

    expect(result.current.filtros.unidadeId).toBeNull()
    expect(result.current.filtros.blocoId).toBeNull()
  })

  it.each([
    ['campusId', '/?campusId=0'],
    ['campusId', '/?campusId=-1'],
    ['campusId', '/?campusId=abc'],
    ['unidadeId', '/?campusId=1&unidadeId=0'],
    ['unidadeId', '/?campusId=1&unidadeId=abc'],
    ['blocoId', '/?campusId=1&unidadeId=2&blocoId=-3'],
    ['blocoId', '/?campusId=1&unidadeId=2&blocoId=1.5'],
  ])('rejeita %s inválido e usa fallback', (campo, url) => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper([url]),
    })

    expect(result.current.filtros[campo as 'campusId' | 'unidadeId' | 'blocoId']).toBeNull()
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

  it('rejeita tipo com mais de 50 caracteres e usa fallback', () => {
    const longTipo = 't'.repeat(51)
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper([`/?tipo=${longTipo}`]),
    })

    expect(result.current.filtros.tipo).toBe('')
  })

  it('deriva tipoFiltro como NENHUM quando filtros estão vazios', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/']),
    })

    expect(result.current.tipoFiltro).toBe(TipoFiltro.NENHUM)
  })

  it('deriva tipoFiltro como NOME quando nome está presente', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?nome=sala']),
    })

    expect(result.current.tipoFiltro).toBe(TipoFiltro.NOME)
  })

  it('deriva tipoFiltro como TIPO quando tipo está presente', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?tipo=SALA_AULA']),
    })

    expect(result.current.tipoFiltro).toBe(TipoFiltro.TIPO)
  })

  it('deriva tipoFiltro como LOCALIZACAO quando campusId está presente', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?campusId=1']),
    })

    expect(result.current.tipoFiltro).toBe(TipoFiltro.LOCALIZACAO)
  })

  it('deriva tipoFiltro como LOCALIZACAO quando unidadeId está presente', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?campusId=1&unidadeId=2']),
    })

    expect(result.current.tipoFiltro).toBe(TipoFiltro.LOCALIZACAO)
  })

  it('deriva tipoFiltro como LOCALIZACAO quando blocoId está presente', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?campusId=1&unidadeId=2&blocoId=3']),
    })

    expect(result.current.tipoFiltro).toBe(TipoFiltro.LOCALIZACAO)
  })

  it('deriva tipoFiltro como LOCALIZACAO quando andar está presente', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?andar=0']),
    })

    expect(result.current.tipoFiltro).toBe(TipoFiltro.LOCALIZACAO)
  })

  it('blocoId órfão na URL não ativa filtro de localização', () => {
    const { result } = renderHook(() => useAmbientesSearchParams(), {
      wrapper: createWrapper(['/?blocoId=3']),
    })

    expect(result.current.tipoFiltro).toBe(TipoFiltro.NENHUM)
  })
})
