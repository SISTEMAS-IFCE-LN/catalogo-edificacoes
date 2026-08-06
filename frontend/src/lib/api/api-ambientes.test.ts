import { describe, it, expect, vi, beforeEach } from 'vitest'
import { fetchAmbientes, fetchDetalheAmbiente, fetchEsquadrias } from './api-ambientes'
import { api } from '@/lib/api/api'

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
    await fetchAmbientes({ page: 1, size: 20, nome: 'sala' })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados', expect.objectContaining({
      params: expect.objectContaining({ page: 1, size: 20, nome: 'sala' }),
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
    await fetchAmbientes({ page: 0, size: 20 })
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
    await fetchAmbientes({ page: 0, size: 20, andar: 2 })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados', expect.objectContaining({
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
    await fetchAmbientes({ page: 0, size: 20 }, controller.signal)
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
    const result = await fetchAmbientes({ page: 0, size: 20 })
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
    await expect(fetchAmbientes({ page: 0, size: 20 })).rejects.toThrow()
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
        area: 50,
        geometrias: [{ id: 1, tipo: 'Retangular', base: 5, altura: 10, repeticao: 1, area: 50 }],
        areaAmbiente: 50,
        pesDireitos: [3.5, 2.8],
        esquadriasDetalhes: {
          esquadrias: [],
          esquadriasTipoMaterial: [],
        },
        informacaoAdicional: '',
        status: 'Publicado',
      },
    })
    await fetchDetalheAmbiente(1)
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados/1')
  })

  it('valida resposta com Zod', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        id: 1,
        nome: 'Sala 1',
        localizacao: { id: 1, bloco: 'Bloco 1', unidade: 'Sede', andar: 1 },
        tipo: 'Sala de Aula',
        capacidade: 30,
        area: 50,
        geometrias: [{ id: 1, tipo: 'Retangular', base: 5, altura: 10, repeticao: 1, area: 50 }],
        areaAmbiente: 50,
        pesDireitos: [3.5, 2.8],
        esquadriasDetalhes: {
          esquadrias: [],
          esquadriasTipoMaterial: [],
        },
        informacaoAdicional: '',
        status: 'Publicado',
      },
    })
    const result = await fetchDetalheAmbiente(1)
    expect(result).toHaveProperty('id')
    expect(result).toHaveProperty('nome')
    expect(result).toHaveProperty('geometrias')
    expect(result).toHaveProperty('esquadriasDetalhes')
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
    await expect(fetchDetalheAmbiente(1)).rejects.toThrow()
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
    await fetchEsquadrias({ ids: [1, 2, 3], page: 0, size: 100 })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados/esquadrias', expect.objectContaining({
      params: expect.objectContaining({ ids: '1,2,3', page: 0, size: 100 }),
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
    const result = await fetchEsquadrias({ ids: [1], page: 0, size: 100 })
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
    await expect(fetchEsquadrias({ ids: [1], page: 0, size: 100 })).rejects.toThrow()
  })
})
