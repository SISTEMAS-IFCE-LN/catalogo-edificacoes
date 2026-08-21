import { describe, it, expect, vi, beforeEach } from 'vitest'
import { fetchPublicados, fetchDetalhePublicados, fetchEsquadriasPublicados } from './api-publicados'
import { api } from '@/lib/api/api'
import { TipoFiltro } from '@/types/ambientes/enums'

vi.mock('@/lib/api/api', () => ({
  api: { get: vi.fn() },
}))

describe('fetchPublicados', () => {
  beforeEach(() => {
    vi.mocked(api.get).mockClear()
  })

  it('chama /api/ambientes/publicados com params', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        areaTotal: 0,
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 20,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await fetchPublicados({ page: 1, size: 20, tipoFiltro: TipoFiltro.NENHUM })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados', expect.objectContaining({
      params: expect.objectContaining({ page: 1, size: 20 }),
    }))
  })

  it('omite params vazios', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        areaTotal: 0,
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 20,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await fetchPublicados({ page: 0, size: 20, tipoFiltro: TipoFiltro.NENHUM })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados', expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20 }),
    }))
    const callParams = vi.mocked(api.get).mock.calls[0][1]?.params
    expect(callParams).not.toHaveProperty('nome')
    expect(callParams).not.toHaveProperty('bloco')
    expect(callParams).not.toHaveProperty('unidade')
    expect(callParams).not.toHaveProperty('andar')
    expect(callParams).not.toHaveProperty('tipo')
  })

  it('inclui andar quando fornecido', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        areaTotal: 0,
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 20,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await fetchPublicados({ page: 0, size: 20, andar: 2, tipoFiltro: TipoFiltro.LOCALIZACAO })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados/localizacao', expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, andar: 2 }),
    }))
  })

  it('repassa signal ao axios', async () => {
    const controller = new AbortController()
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        areaTotal: 0,
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 20,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await fetchPublicados({ page: 0, size: 20, tipoFiltro: TipoFiltro.NENHUM }, controller.signal)
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados', expect.objectContaining({
      signal: controller.signal,
    }))
  })

  it('valida resposta com Zod', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        areaTotal: 0,
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 20,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    const result = await fetchPublicados({ page: 0, size: 20, tipoFiltro: TipoFiltro.NENHUM })
    expect(result).toHaveProperty('ambientes')
    expect(result).toHaveProperty('areaTotal')
    expect(result).toHaveProperty('dadosPaginacao')
  })

  it('lança erro Zod quando resposta é inválida', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: 'invalid', // deveria ser array
        areaTotal: 0,
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 20,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await expect(fetchPublicados({ page: 0, size: 20, tipoFiltro: TipoFiltro.NENHUM })).rejects.toThrow()
  })

  it('chama /api/ambientes/publicados/nome quando tipoFiltro é NOME', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        areaTotal: 0,
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 20,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await fetchPublicados({ page: 0, size: 20, nome: 'sala', tipoFiltro: TipoFiltro.NOME })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados/nome', expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, nome: 'sala' }),
    }))
  })

  it('chama /api/ambientes/publicados/tipo quando tipoFiltro é TIPO', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        areaTotal: 0,
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 20,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await fetchPublicados({ page: 0, size: 20, tipo: 'SALA_AULA', tipoFiltro: TipoFiltro.TIPO })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados/tipo', expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, tipo: 'SALA_AULA' }),
    }))
  })

  it('chama /api/ambientes/publicados/localizacao quando tipoFiltro é LOCALIZACAO', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        areaTotal: 0,
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 20,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await fetchPublicados({ page: 0, size: 20, bloco: 'Bloco 1', unidade: 'Sede', andar: 2, tipoFiltro: TipoFiltro.LOCALIZACAO })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados/localizacao', expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, bloco: 'Bloco 1', unidade: 'Sede', andar: 2 }),
    }))
  })
})

describe('fetchDetalheAmbiente', () => {
  beforeEach(() => {
    vi.mocked(api.get).mockClear()
  })

  it('chama /api/ambientes/publicados/{id}', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        id: 1,
        nome: 'Sala 1',
        localizacao: { id: 1, bloco: 'Bloco 1', unidade: 'Sede', andar: 1 },
        tipo: 'Sala de Aula',
        capacidade: 30,
        geometrias: [{ id: 1, tipo: 'RETANGULAR', base: 5, altura: 10, repeticao: 1, area: 50 }],
        areaAmbiente: 50,
        pesDireitos: [3.5, 2.8],
        esquadriasDetalhes: {
          esquadrias: [],
          esquadriasTipoMaterial: [],
        },
        informacaoAdicional: '',
        status: 'PUBLICADO',
      },
    })
    await fetchDetalhePublicados(1)
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados/1', expect.any(Object))
  })

  it('repassa signal ao axios', async () => {
    const controller = new AbortController()
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        id: 1,
        nome: 'Sala 1',
        localizacao: { id: 1, bloco: 'Bloco 1', unidade: 'Sede', andar: 1 },
        tipo: 'Sala de Aula',
        capacidade: 30,
        geometrias: [],
        areaAmbiente: 50,
        pesDireitos: [],
        esquadriasDetalhes: {
          esquadrias: [],
          esquadriasTipoMaterial: [],
        },
        informacaoAdicional: '',
        status: 'PUBLICADO',
      },
    })
    await fetchDetalhePublicados(1, controller.signal)
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados/1', expect.objectContaining({
      signal: controller.signal,
    }))
  })

  it('valida resposta com Zod', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        id: 1,
        nome: 'Sala 1',
        localizacao: { id: 1, bloco: 'Bloco 1', unidade: 'Sede', andar: 1 },
        tipo: 'Sala de Aula',
        capacidade: 30,
        geometrias: [{ id: 1, tipo: 'RETANGULAR', base: 5, altura: 10, repeticao: 1, area: 50 }],
        areaAmbiente: 50,
        pesDireitos: [3.5, 2.8],
        esquadriasDetalhes: {
          esquadrias: [],
          esquadriasTipoMaterial: [],
        },
        informacaoAdicional: '',
        status: 'PUBLICADO',
      },
    })
    const result = await fetchDetalhePublicados(1)
    expect(result).toHaveProperty('id')
    expect(result).toHaveProperty('nome')
    expect(result).toHaveProperty('geometrias')
    expect(result).toHaveProperty('esquadriasDetalhes')
    expect(result).toHaveProperty('areaAmbiente', 50)
    expect(result).not.toHaveProperty('area')
    expect(result.geometrias[0].tipo).toBe('Retangular')
    expect(result.status).toBe('Publicado')
  })

  it('lança erro Zod quando resposta é inválida', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        id: 'invalid', // deveria ser number
        nome: 'Sala 1',
        localizacao: { id: 1, bloco: 'Bloco 1', unidade: 'Sede', andar: 1 },
        tipo: 'Sala de Aula',
        capacidade: 30,
        area: 50,
        geometrias: [],
        areaAmbiente: 50,
        pesDireitos: [],
        esquadriasDetalhes: {
          esquadrias: [],
          esquadriasTipoMaterial: [],
        },
        informacaoAdicional: '',
        status: 'Publicado',
      },
    })
    await expect(fetchDetalhePublicados(1)).rejects.toThrow()
  })
})

describe('fetchEsquadrias', () => {
  beforeEach(() => {
    vi.mocked(api.get).mockClear()
  })

  it('chama /api/ambientes/publicados/esquadrias com ids', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        totalTipoMaterial: [],
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 100,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await fetchEsquadriasPublicados({ ids: [1, 2, 3], page: 0, size: 100 })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados/esquadrias', expect.objectContaining({
      params: expect.objectContaining({ ids: '1,2,3', page: 0, size: 100 }),
    }))
  })

  it('repassa signal ao axios', async () => {
    const controller = new AbortController()
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        totalTipoMaterial: [],
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 100,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await fetchEsquadriasPublicados({ ids: [1], page: 0, size: 100 }, controller.signal)
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados/esquadrias', expect.objectContaining({
      params: expect.objectContaining({ ids: '1', page: 0, size: 100 }),
      signal: controller.signal,
    }))
  })

  it('valida resposta com Zod', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: [],
        totalTipoMaterial: [],
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 100,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    const result = await fetchEsquadriasPublicados({ ids: [1], page: 0, size: 100 })
    expect(result).toHaveProperty('ambientes')
    expect(result).toHaveProperty('totalTipoMaterial')
    expect(result).toHaveProperty('dadosPaginacao')
  })

  it('lança erro Zod quando resposta é inválida', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: 'invalid', // deveria ser array
        totalTipoMaterial: [],
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 100,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await expect(fetchEsquadriasPublicados({ ids: [1], page: 0, size: 100 })).rejects.toThrow()
  })
})
